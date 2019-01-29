package uk.co.flakeynetworks.vmixtally;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmix.status.InputStatusChangeListener;

/**
 * Created by Richard Stokes on 9/24/2018.
 */

public class TallyFragment extends Fragment {

    private ImageView tallyColor;
    private Input input;

    private TallyActivity mainActivity;

    // Add a listener to the input
    private InputStatusChangeListener statusListener = new InputStatusChangeListener() {
        @Override
        public void isProgramChange() {

            getActivity().runOnUiThread(() -> updateTally());
        } // end of isProgramChange

        @Override
        public void isPreviewChange() {

            getActivity().runOnUiThread(() -> updateTally());
        } // end of isPreviewChange

        @Override
        public void inputRemoved() {

            getActivity().runOnUiThread(() -> mainActivity.inputWasRemoved());
        } // end of inputRemoved
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tally, container, false);

        mainActivity = (TallyActivity) getActivity();

        // Get the first input
        input = mainActivity.getInput();
        if(input == null) {

            Crashlytics.log("Unexpected State, loading tally fragment but a null input.");
            mainActivity.loadSettingsFragment();
            return view;
        } // end of if


        tallyColor = view.findViewById(R.id.tallyColor);
        tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyNone));

        new Thread(() -> startListeningForTallyChange()).start();


        // Set the title bar title
        mainActivity.getSupportActionBar().setTitle("Tally: " + input.getName());  // provide compatibility to all the versions

        return view;
    } // end of onCreateView


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateTally();
    } // end of onViewCreated


    @Override
    public void onDestroy() {

        // Remove the listener for this input
        input.removeStatusChangeListener(statusListener);

        super.onDestroy();
    } // end of onDestroy


    private void startListeningForTallyChange() {

        if(input == null) {

            Crashlytics.log("Unexpected State, loading tally fragment but a null input.");
            mainActivity.runOnUiThread(() -> mainActivity.loadSettingsFragment());
            return;
        } // end of if


        // Add a listener for this input
        input.addStatusChangeListener(statusListener);
    } // end of startListeningForTallyChange


    private void updateTally() {

        TextView tallyText = getView().findViewById(R.id.tallyText);

        // Check that there is a valid input
        if(input == null) mainActivity.inputWasRemoved();

        if(input.isProgram()) {
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyProgram));
            tallyText.setText(getString(R.string.tally_program));
        } else if(input.isPreview()) {
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyPreview));
            tallyText.setText(getString(R.string.tally_preview));
        } else {
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyNone));
            tallyText.setText(getString(R.string.tally_none));
        } // end of else
    } // end of updateTally
} // end of TallyFragment
