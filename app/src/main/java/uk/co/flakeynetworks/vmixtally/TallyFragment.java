package uk.co.flakeynetworks.vmixtally;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmix.status.InputStatusChangeListener;
import uk.co.flakeynetworks.vmix.status.VMixStatus;

/**
 * Created by Richard Stokes on 9/24/2018.
 */

public class TallyFragment extends Fragment {

    private ImageView tallyColor;
    private Input input;

    private MainActivity mainActivity;

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

            mainActivity.inputWasRemoved();
        } // end of inputRemoved
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tally, container, false);

        mainActivity = (MainActivity) getActivity();

        tallyColor = view.findViewById(R.id.tallyColor);
        tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyNone));

        new Thread(() -> startListeningForTallyChange()).start();

        return view;
    } // end of onCreateView


    @Override
    public void onDestroy() {

        // Remove the listener for this input
        input.removeStatusChangeListener(statusListener);

        super.onDestroy();
    } // end of onDestroy


    private void startListeningForTallyChange() {

        TCPAPI tcpConnection = mainActivity.getTcpConnection();

        // Get the first input
        input = mainActivity.getInput();

        // Add a listener for this input
        input.addStatusChangeListener(statusListener);

        // Subscribe for tally changes
        tcpConnection.getProtocol().subscribeTally();
    } // end of startListeningForTallyChange


    private void updateTally() {

        if(input.isProgram())
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyProgram));
        else if(input.isPreview())
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyPreview));
        else
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyNone));
    } // end of updateTally
} // end of TallyFragment
