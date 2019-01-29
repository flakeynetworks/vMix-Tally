package uk.co.flakeynetworks.vmixtally.data;

import androidx.lifecycle.LiveData;
import uk.co.flakeynetworks.vmix.status.Input;

public interface TallyRepository {

    LiveData<Input> getCurrentInput();
    void setCurrentInput(Input input);

    void saveHost(String address);
    LiveData<String> getHost();
} // end of TallyRespository
