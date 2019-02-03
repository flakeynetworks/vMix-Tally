package uk.co.flakeynetworks.vmixtally.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmixtally.data.TallyRepository;

public class SettingsViewModel extends AndroidViewModel {


    private final TallyRepository repository;


    public SettingsViewModel(@NonNull Application application, @NonNull TallyRepository repository) {

        super(application);

        this.repository = repository;
    } // end of constructor


    String getSavedHost() { return repository.getSavedHost(); } // end of getSavedHost


    void connectToHost(String address, int port) { repository.connectToHost(address, port); } // end of connectToHost


    void inputSelected(Input input) { repository.setCurrentInput(input); } // end of inputSelected


    public LiveData<VMixHost> getHost() { return repository.getHost(); } // end of getHost


    public LiveData<Boolean> getIsReconnecting() { return repository.isAttemptingReconnect(); } // end of getIsReconnecting


    public void cancelReconnect() { repository.cancelReconnectAttempt(); } // end of cancelReconnect


    public LiveData<Boolean> getInputsChanged() { return repository.getInputsChanged(); } // end of getInputsChanged


    public LiveData<TCPAPI> getTcpConnection() { return repository.getTcpConnection(); } // end of getTCPConnection


    public void disconnectFromHost() { repository.disconnectFromHost(); } // end of disconnectFromHost

    public LiveData<String> getErrorMessages() { return repository.getErrorMessages(); } // end of getErrorMessage
} // end of SettingsViewModel
