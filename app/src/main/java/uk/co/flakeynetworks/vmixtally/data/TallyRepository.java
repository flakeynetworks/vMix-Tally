package uk.co.flakeynetworks.vmixtally.data;

import androidx.lifecycle.LiveData;
import io.reactivex.Observable;
import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.status.Input;

public interface TallyRepository {

    // Current input
    Observable<Input> getCurrentInput();
    void setCurrentInput(Input input);
    LiveData<Boolean> getInputsChanged();

    // Last connected host information
    void saveHost(String address);
    String getSavedHost();

    // Current host connected to
    LiveData<VMixHost> getHost();
    void setHost(VMixHost host);

    LiveData<TCPAPI> getTcpConnection();

    void connectToHost(String address, int port);

    LiveData<Boolean> isAttemptingReconnect();
    void cancelReconnectAttempt();

    void disconnectFromHost();

    LiveData<String> getErrorMessages();
} // end of TallyRespository
