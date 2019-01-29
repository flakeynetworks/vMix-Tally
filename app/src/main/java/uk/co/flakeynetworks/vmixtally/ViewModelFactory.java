package uk.co.flakeynetworks.vmixtally;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import uk.co.flakeynetworks.vmixtally.data.TallyRepository;
import uk.co.flakeynetworks.vmixtally.data.VMixTallyRepository;
import uk.co.flakeynetworks.vmixtally.ui.settings.SettingsViewModel;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Application application;
    private final TallyRepository repository;

    private static volatile ViewModelFactory INSTANCE;


    private ViewModelFactory(Application application) {

        this.application = application;
        this.repository = new VMixTallyRepository();
    } // end of constructor


    public static ViewModelFactory getInstance(Application app) {

        if(INSTANCE != null) return INSTANCE;

        synchronized (ViewModelFactory.class) {
            if(INSTANCE == null)
                INSTANCE = new ViewModelFactory(app);
        } // end of synchronized

        return INSTANCE;
    } // end of getInstance


    @VisibleForTesting
    public static void destroyInstance() { INSTANCE = null; } // end of destroyInstance


    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if(modelClass.isAssignableFrom(SettingsViewModel.class))
            //noinspection unchecked
            return (T) new SettingsViewModel(application, repository);

        return super.create(modelClass);
    } // end of create
} // end of ViewModelFactory
