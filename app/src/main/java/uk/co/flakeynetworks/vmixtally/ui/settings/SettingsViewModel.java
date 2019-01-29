package uk.co.flakeynetworks.vmixtally.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import uk.co.flakeynetworks.vmixtally.data.TallyRepository;

public class SettingsViewModel extends AndroidViewModel {


    private final TallyRepository repository;


    public SettingsViewModel(@NonNull Application application, @NonNull TallyRepository repository) {

        super(application);

        this.repository = repository;
    } // end of constructor
} // end of SettingsViewModel
