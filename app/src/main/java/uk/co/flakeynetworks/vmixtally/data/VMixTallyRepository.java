package uk.co.flakeynetworks.vmixtally.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.net.MalformedURLException;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.api.TCPAPIListener;
import uk.co.flakeynetworks.vmix.status.HostStatusChangeListener;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmixtally.R;
import uk.co.flakeynetworks.vmixtally.model.ErrorMessage;
import uk.co.flakeynetworks.vmixtally.model.NullInput;

public class VMixTallyRepository implements TallyRepository {

    private final Application application;
    private final BehaviorSubject<Input> currentInput = BehaviorSubject.create();
    private final MutableLiveData<VMixHost> currentHost = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAttemptingReconnect = new MutableLiveData<>(false);
    private final MutableLiveData<TCPAPI> tcpConnection = new MutableLiveData<>(null);
    private final MutableLiveData<ErrorMessage> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> inputsChanged = new MutableLiveData<>(false);

    private final HostStatusChangeListener hostListener = new HostStatusChangeListener() {

        @Override
        public void inputRemoved(Input input) {

            inputsChanged.postValue(true);

            // Check if the input was the currently set input. If so then remove it
            if(currentInput.getValue() == input)
                setCurrentInput(NullInput.getInstance());
        } // end of inputRemoved

        @Override
        public void inputAdded(Input input) { inputsChanged.postValue(true); } // end of inputAdded
    };

    private final TCPAPIListener tcpapiListener = new TCPAPIListener() {
        @Override
        public void disconnected() { tcpConnectionUnexpectedlyClosed(); } // end of disconnected
    };


    public VMixTallyRepository(Application application) { this.application = application; } // end of constructor


    @Override
    public Observable<Input> getCurrentInput() { return currentInput; } // end of getCurrentInput


    @Override
    public void setCurrentInput(Input input) {

        // Update the current input
        currentInput.onNext(input);
    } // end of setCurrentInput


    public LiveData<Boolean> getInputsChanged() { return inputsChanged; } // end of getInputsChanged


    @Override
    public void saveHost(String address) {

        // Save to shared preferences
        SharedPreferences sharedPref = application.getSharedPreferences(application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(application.getString(R.string.saved_host), address);

        editor.apply();
    } // end of saveHost


    @Override
    public void savePort(int port) {

        // Save to shared preferences
        SharedPreferences sharedPref = application.getSharedPreferences(application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(application.getString(R.string.saved_port), port);

        editor.apply();
    } // end of saveHost


    @Override
    public LiveData<ErrorMessage> getErrorMessages() { return errorMessage; } // end of getErrorMessages


    @Override
    public String getSavedHost() {

        // Get the value from shared preferences
        SharedPreferences sharedPref = application.getSharedPreferences(application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        return sharedPref.getString(application.getString(R.string.saved_host), "");
    } // end of getSavedHost


    @Override
    public int getSavedPort() {

        // Get the value from shared preferences
        SharedPreferences sharedPref = application.getSharedPreferences(application.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        return sharedPref.getInt(application.getString(R.string.saved_port), application.getResources().getInteger(R.integer.default_port));
    } // end of getSavedPort



    public LiveData<VMixHost> getHost() { return currentHost; } // end of getHost
    public void setHost(VMixHost host) {

        // Remove any old listeners
        VMixHost oldHost = currentHost.getValue();
        if(oldHost != null)
            oldHost.removeListener(hostListener);


        // Add the listener to the new host and save
        if(host != null) {

            host.addListener(hostListener);
            saveHost(host.getAddress());
            savePort(host.getPort());
        } // end of if

        // Update the current host
        currentHost.postValue(host);
    } // end of setHost


    public LiveData<TCPAPI> getTcpConnection() { return tcpConnection; } // end of getTCPConnection
    private void setTcpConnection(TCPAPI tcpConnection) {

        // Get the old connection and remove the listener to it
        TCPAPI oldConnection = this.tcpConnection.getValue();
        if(oldConnection != null)
            oldConnection.removeListener(tcpapiListener);


        // Add the new connection
        this.tcpConnection.postValue(tcpConnection);

        if(tcpConnection != null) {
            tcpConnection.addListener(tcpapiListener);

            // Subscribe for tally updates
            tcpConnection.getProtocol().subscribeTally();
        } // end of if
    } // end of setTcpConnection


    private void tcpConnectionUnexpectedlyClosed() {

        setTcpConnection(null);

        isAttemptingReconnect.postValue(true);

        // Start attempting to connect to the server.
        if(reconnectingThread != null)
            reconnectingThread.interrupt();

        reconnectingThread = new ReconnectThread();
        reconnectingThread.start();
    } // end of tcpConnectionClosed


    private void reconnectCompleted() {

        isAttemptingReconnect.postValue(false);
    } // end of reconnectCompleted


    public void cancelReconnectAttempt() {

        if(reconnectingThread != null) {
            reconnectingThread.interrupt();
            reconnectingThread = null;
        } // end of if

        isAttemptingReconnect.postValue(false);

        setTcpConnection(null);
        setHost(null);
    } // end of cancelReconnect


    @Override
    public void disconnectFromHost() {

        // Check If we were connected to another host before then disconnect.
        // Close down the previous TCP connection is any
        TCPAPI previousTCPConnection = tcpConnection.getValue();

        if (previousTCPConnection != null) {

            previousTCPConnection.removeAllListeners();
            previousTCPConnection.close();
        } // end of if

        setTcpConnection(null);
        setHost(null);
    } // end of disconnectFromHost


    public LiveData<Boolean> isAttemptingReconnect() { return isAttemptingReconnect; } // end of isAttempingReconnect


    @Override
    public void connectToHost(String address, int port) {

        new Thread(() -> {

            // Make sure we can connect to the web api
            try {

                disconnectFromHost();

                // Try connecting to the new host
                VMixHost host;
                try {
                    host = new VMixHost(address, port);
                } catch (NumberFormatException e) {

                    showError(application.getString(R.string.error_invalid_port));
                    return;
                } // end of catch


                // Make sure we can connect to the tcp api
                TCPAPI tcpConnection = new TCPAPI(host);
                tcpConnection.setTimeout(application.getResources().getInteger(R.integer.timeout_value));

                // Connect tp the tcp api
                if (!tcpConnection.connect()) {

                    //Could not connect. Check address is correct and port 8099 is open
                    int defaultTCPPort = application.getResources().getInteger(R.integer.default_tcp_port);

                    showError(application.getString(R.string.error_tcp_connection_failed, String.valueOf(defaultTCPPort)));
                    tcpConnection.close();
                    return;
                } // end of if


                // Get an update via the web api
                if (!host.update()) {

                    showError(application.getString(R.string.error_host_connection, host.getPort()));
                    tcpConnection.close();
                    return;
                } // end of if

                setHost(host);
                setTcpConnection(tcpConnection);
            } catch (MalformedURLException e) {

                showError(application.getString(R.string.error_invalid_address));
            } // end of catch
        }).start();
    } // end of connectToHost


    private void showError(String message) {

        if(message == null) return;

        errorMessage.postValue(new ErrorMessage(message));
    } // end of showError


    // Reconnecting to host stuff
    private Thread reconnectingThread;
    private class ReconnectThread extends Thread {

        @Override
        public void run() {

            while (true) {

                if(isInterrupted()) return;

                try {
                    Thread.sleep(application.getResources().getInteger(R.integer.reconnectWaitPeriod));
                } catch (InterruptedException e) {
                    return;
                } // end of catch

                if(currentHost.getValue() == null || tcpConnection.getValue() != null) return;

                // Make sure we can connect to the tcp api
                TCPAPI tcpConnection = new TCPAPI(currentHost.getValue());
                tcpConnection.setTimeout(application.getResources().getInteger(R.integer.timeout_value));

                // Connect tp the tcp api
                if(tcpConnection.connect()) {

                    if(isInterrupted()) {
                        tcpConnection.close();
                        return;
                    } // end of if

                    setTcpConnection(tcpConnection);
                    reconnectCompleted();

                    return;
                } // end of if

                if(isInterrupted()) return;
            } // end of while
        } // end of run
    } // end of ReconnectingThread
} // end of VMixTallyRepository
