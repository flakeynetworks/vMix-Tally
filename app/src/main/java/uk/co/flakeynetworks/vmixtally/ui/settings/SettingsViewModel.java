package uk.co.flakeynetworks.vmixtally.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmixtally.R;
import uk.co.flakeynetworks.vmixtally.data.TallyRepository;
import uk.co.flakeynetworks.vmixtally.model.ErrorMessage;

@SuppressWarnings("WeakerAccess")
public class SettingsViewModel extends AndroidViewModel {

    private final TallyRepository repository;

    private String address;
    private int port;

    // UI elements
    private final MutableLiveData<Boolean> displayTick = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayCross = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayStatusBox = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayConnectingBox = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayNextButton = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayInputs = new MutableLiveData<>();
    private final MutableLiveData<String> statusText = new MutableLiveData<>();
    private final MutableLiveData<String> addressError = new MutableLiveData<>();
    private final MutableLiveData<String> portError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addressEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> portEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectButtonEnabled = new MutableLiveData<>(true);
    private final MutableLiveData<String> connectButtonText = new MutableLiveData<>();


    public SettingsViewModel(@NonNull Application application, @NonNull TallyRepository repository) {

        super(application);

        this.repository = repository;
        address = repository.getSavedHost();
        port = repository.getSavedPort();
    } // end of constructor


    public String getAddress() { return address; } // end of getAddress
    public void setAddress(String address) {

        this.address = address;

        // Validate the address
        if(!isAddressValid())
            setAddressError(getApplication().getString(R.string.error_invalid_address));
        else
            setAddressError(null);
    } // end of setAddress

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isAddressValid() { return address != null && !address.isEmpty(); } // end of isAddressValid

    public int getPort() { return port; } // end of getPort
    public void setPort(int port) {

        this.port = port;
        if(!isPortValid())
            setPortError(getApplication().getString(R.string.error_invalid_port));
        else
            setPortError(null);
    } // end of setPort


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isPortValid() {

        try {

            if(port < 1 || port > 65535)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {

            return false;
        } // end of catch

        return true;
    } // end of isPortValid



    void connectToHost() { repository.connectToHost(address, port); }
    void inputSelected(Input input) { repository.setCurrentInput(input); }
    public void cancelReconnect() { repository.cancelReconnectAttempt(); }
    public void disconnectFromHost() { repository.disconnectFromHost(); }


    public LiveData<VMixHost> getHost() { return repository.getHost(); }
    public LiveData<Boolean> getIsReconnecting() { return repository.isAttemptingReconnect(); }
    public LiveData<Boolean> getInputsChanged() { return repository.getInputsChanged(); }
    public LiveData<TCPAPI> getTcpConnection() { return repository.getTcpConnection(); }



    public LiveData<ErrorMessage> getErrorMessages() { return repository.getErrorMessages(); }

    public void setStatusText(String message) { statusText.postValue(message); }
    public LiveData<String> getStatusText() { return statusText; }


    public void setPortError(String message) { portError.postValue(message); }
    public LiveData<String> getPortError() { return portError; }


    public void setAddressError(String message) { addressError.postValue(message); }
    public LiveData<String> getAddressError() { return addressError; }


    public void setAddressEnabled(boolean truth) { addressEnabled.postValue(truth); }
    public LiveData<Boolean> getAddressEnabled() { return addressEnabled; }


    public void setPortEnabled(boolean truth) { portEnabled.postValue(truth); }
    public LiveData<Boolean> getPortEnabled() { return portEnabled; }


    public void displayConnectingBox(boolean truth) { displayConnectingBox.postValue(truth); }
    public LiveData<Boolean> getDisplayConnectingBox() { return displayConnectingBox; }


    public void setConnectButtonEnabled(boolean truth) { connectButtonEnabled.postValue(truth); }
    public LiveData<Boolean> getConnectButtonEnabled() { return connectButtonEnabled; }


    public void displayInputs(boolean truth) { displayInputs.postValue(truth); }
    public LiveData<Boolean> getDisplayInputs() { return displayInputs; }

    public void displayNextButton(boolean truth) { displayNextButton.postValue(truth); }
    public LiveData<Boolean> getDisplayNextButton() { return displayNextButton; }


    public LiveData<Boolean> getDisplayTick() { return displayTick;  }
    public void displayTick(boolean truth) { displayTick.postValue(truth); }


    public LiveData<Boolean> getDisplayCross() { return displayCross;  }
    public void displayCross(boolean truth) { displayCross.postValue(truth); }


    public LiveData<Boolean> getDisplayStatusBox() { return displayStatusBox;  }
    public void displayStatusBox(boolean truth) { displayStatusBox.postValue(truth); }


    public LiveData<String> getConnectButtonText() { return connectButtonText; } // end of getConnectButtonText
    public void setConnectButtonText(String text) { connectButtonText.postValue(text); } // end of setConnectButtonText
} // end of SettingsViewModel
