package uk.co.flakeynetworks.vmixtally;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.api.TCPAPIListener;
import uk.co.flakeynetworks.vmix.status.Input;


public class TallyActivity extends AppCompatActivity {

    private static VMixHost host;
    private static TCPAPI tcpConnection;
    private static Input input;
    private static boolean attemptingReconnect = false;

    // Reconnecting to host stuff
    private Thread reconnectingThread;
    private class ReconnectThread extends Thread {

        @Override
        public void run() {

            while (true) {

                if(isInterrupted()) return;

                try {
                    Thread.sleep(getResources().getInteger(R.integer.reconnectWaitPeriod));
                } catch (InterruptedException e) {
                    return;
                } // end of catch

                if(host == null || tcpConnection != null) return;

                // Make sure we can connect to the tcp api
                TCPAPI tcpConnection = new TCPAPI(host);
                tcpConnection.setTimeout(getResources().getInteger(R.integer.timeout_value));

                // Connect tp the tcp api
                if(tcpConnection.connect()) {

                    // Subscribe for tally updates
                    tcpConnection.getProtocol().subscribeTally();

                    if(isInterrupted()) {
                        tcpConnection.close();
                        return;
                    } // end of if

                    TallyActivity.tcpConnection = tcpConnection;

                    runOnUiThread(TallyActivity.this::reconnected);
                    return;
                } // end of if

                if(isInterrupted()) return;
            } // end of while
        } // end of run
    } // end of ReconnectingThread


    private ReconnectingDialog reconnectingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        if(savedInstanceState == null) {

            // Load the settings fragment
            loadSettingsFragment();
        } // end of if


        // Initialise Fabrio.io with Crashlytics
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
    } // end of onCreate


    private void loadFragment(Fragment fragment) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentPlaceHolder, fragment);

        transaction.commitAllowingStateLoss();
    } // end of loadFragment


    public void loadSettingsFragment() {

        input = null;

        SettingsFragment fragment = new SettingsFragment();
        loadFragment(fragment);
    } // end of loadSettingsFragment


    public void loadTallyFragment() {

        TallyFragment fragment = new TallyFragment();
        loadFragment(fragment);
    } // end of loadTallyFragment


    public VMixHost getHost() { return host; } // end of getHost
    public void setHost(VMixHost host) {

        this.host = host;

        if(host != null)
            saveHost();
    } // end of setHost


    public TCPAPI getTcpConnection() { return tcpConnection; } // end of getTCPConnection
    public void setTcpConnection(TCPAPI tcpConnection) {

        this.tcpConnection = tcpConnection;

        if(tcpConnection != null) {
            tcpConnection.addListener(new TCPAPIListener() {
                @Override
                public void disconnected() {

                    runOnUiThread(() -> tcpConnectionClosed());
                } // end of disconnected
            });
        } // end of if
    } // end of setTcpConnection


    public void setInput(Input input) {

        this.input = input;
    } // end of setInput


    public Input getInput() { return input; } // end of getInput


    private void saveHost() {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_host), host.getAddress());

        editor.commit();
    } // end of saveHost


    public String getLastSavedHost() {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        return sharedPref.getString(getString(R.string.saved_host), "");
    } // end of getLastSavedHost


    public void inputWasRemoved() {

        Input oldInput = input;
        input = null;

        loadSettingsFragment();

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        // end of onClick
        builder.setTitle("Input removed")
                .setMessage(oldInput.getName() + " was removed.")
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    } // end of inputWasRemoved


    public void showReconnectingDialog(Context context) {

        // Means there is already one showing
        if(reconnectingDialog != null) return;

        DialogInterface.OnCancelListener listener = dialog -> {

            Log.v("Reconnecting Dialog", "Cancelled");
            reconnectingDialog.dismiss();
            reconnectingDialog = null;
        };

        reconnectingDialog = new ReconnectingDialog(context, false, listener);
        reconnectingDialog.setCancelAction(new ReconnectingDialog.CancelAction() {
            @Override
            public void execute() {

                cancelReconnect();
            } // end of execute
        });

        reconnectingDialog.show();
    } // end of showReconnectingDialog


    public void tcpConnectionClosed() {

        tcpConnection = null;

        attemptingReconnect = true;
        loadSettingsFragment();

        // Start attempting to connect to the server.
        if(reconnectingThread != null)
            reconnectingThread.interrupt();

        reconnectingThread = new ReconnectThread();
        reconnectingThread.start();
    } // end of tcpConnectionClosed


    public void reconnected() {

        attemptingReconnect = false;
        reconnectingThread = null;

        if(reconnectingDialog != null)
            reconnectingDialog.cancel();


        // Update the host
        Thread thread = new Thread() {
            public void run() {
                host.update();

                if(input != null)
                    runOnUiThread(TallyActivity.this::loadTallyFragment);
                else
                    runOnUiThread(TallyActivity.this::loadSettingsFragment);
            } // end of run
        };

        thread.start();
    } // end of reconnected


    public void cancelReconnect() {

        if(reconnectingThread != null) {
            reconnectingThread.interrupt();
            reconnectingThread = null;
        } // end of if

        if(reconnectingDialog != null)
            reconnectingDialog.cancel();

        attemptingReconnect = false;
    } // end of cancelReconnect


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    } // end of onCreateOptionsMenu


    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.settings)
            loadSettingsFragment();

        return super.onOptionsItemSelected(item);
    } // end of onOptionsItemSelected


    public boolean isAttemptingToReconnect() {

        return attemptingReconnect;
    } // end of isAttemptingToReconnect
} // end of TallyActivity
