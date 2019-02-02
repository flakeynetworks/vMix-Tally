package uk.co.flakeynetworks.vmixtally.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmix.status.InputStatusChangeListener;

public class TallyInput {

    public static final int STATE_INVALID = -1;
    public static final int STATE_ON_STANDBY = 0;
    public static final int STATE_ON_PREVIEW = 1;
    public static final int STATE_ON_PROGRAM = 2;


    private Input input;
    private MutableLiveData<Integer> currentState = new MutableLiveData<>();


    // Add a listener for tally changes on the current input
    private final InputStatusChangeListener inputTallyListener = new InputStatusChangeListener() {
        @Override
        public void isProgramChange() { checkState(); } // end of isProgramChange

        @Override
        public void isPreviewChange() { checkState(); } // end of isPreviewChange

        @Override
        public void inputRemoved() { checkState(); } // end of inputRemoved
    };


    public TallyInput(Input input) {

        this.input = input;
        input.addStatusChangeListener(inputTallyListener);

        checkState();
    } // end of constructor


    private void checkState() {

        if(!input.isInputValid())
            currentState.postValue(STATE_INVALID);
        else if(input.isProgram())
            currentState.postValue(STATE_ON_PROGRAM);
        else if(input.isPreview())
            currentState.postValue(STATE_ON_PREVIEW);
        else
            currentState.postValue(STATE_ON_STANDBY);
    } // end of checkState


    public LiveData<Integer> getTallyStatus() { return currentState; } // end of getTallyStatus

    public Input getInput() { return input; } // end of getInput
} // end of TallyInput
