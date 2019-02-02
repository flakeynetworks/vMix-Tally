package uk.co.flakeynetworks.vmixtally.ui.tally;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.disposables.Disposable;
import uk.co.flakeynetworks.vmixtally.data.TallyRepository;
import uk.co.flakeynetworks.vmixtally.model.TallyInput;

public class TallyViewModel extends AndroidViewModel {


    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final TallyRepository repository;
    private final MutableLiveData<TallyInput> liveInput = new MutableLiveData<>();
    private Disposable observerDisposable;


    public TallyViewModel(@NonNull Application application, TallyRepository repository) {

        super(application);

        this.repository = repository;
        observerDisposable = repository.getCurrentInput().subscribe(input -> {

            liveInput.postValue(new TallyInput(input));
        });
    } // end of constructor


    @Override
    protected void onCleared() {

        observerDisposable.dispose();
        super.onCleared();
    } // end of onCleared


    LiveData<TallyInput> getInput() { return liveInput; } // end of getInput
} // end of TallyViewModel
