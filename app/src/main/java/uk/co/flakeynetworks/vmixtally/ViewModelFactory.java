package uk.co.flakeynetworks.vmixtally;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Application application;

    private static volatile ViewModelFactory INSTANCE;


    private ViewModelFactory(Application application) {
        this.application = application;
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

        return super.create(modelClass);
    } // end of create
} // end of ViewModelFactory
