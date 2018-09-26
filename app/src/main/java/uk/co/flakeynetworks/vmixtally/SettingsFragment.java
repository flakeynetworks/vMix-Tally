package uk.co.flakeynetworks.vmixtally;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.status.VMixStatus;

/**
 * Created by Richard Stokes on 9/24/2018.
 */

public class SettingsFragment extends Fragment {


    private EditText addressField;
    private MainActivity mainActivity;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mainActivity = (MainActivity) getActivity();

        // Set the address field
        addressField = view.findViewById(R.id.addressField);
        addressField.setText(mainActivity.getLastSavedHost());

        Button connectButton = view.findViewById(R.id.connectButton);

        // end of onClick
        connectButton.setOnClickListener(v -> {

            hideKeyboard();
            showConnecting();

            // Attempt to connect to the vmix instance
            new Thread(this::connectToHost).start();
        });


        // Hides the keyboard
        hideKeyboard();

        Button showTallyBtn = view.findViewById(R.id.showTallyBtn);
        showTallyBtn.setOnClickListener(v -> {

            mainActivity.loadTallyFragment();
        });


        return view;
    } // end of onCreateView

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // See if we need to load from a previous state
        if(mainActivity.getHost() != null && mainActivity.getTcpConnection() != null)
            showSuccess();

        if(mainActivity.isAttemptingToReconnect())
            mainActivity.showReconnectingDialog();
    } // end of onViewCreated


    private void connectToHost() {

        // Make sure we can connect to the web api
        try {

            // TODO Make sure to remove the protocol from the address field
            // TODO Add in the ability to set the port
            // TODO remove to tell them must be http://

            VMixHost host = new VMixHost(addressField.getText().toString(), 8088);

            // Make sure we can connect to the tcp api
            TCPAPI tcpConnection = new TCPAPI(host);

            // Connect tp the tcp api
            if(!tcpConnection.connect()) {

                mainActivity.runOnUiThread(() -> showError("Could not connect. Check port 8099"));
                tcpConnection.close();
                return;
            } // end of if


            // Get an update via the web api
            if(!host.update()) {

                mainActivity.runOnUiThread(() -> showError("Could not connect. Check the web controller port"));
                tcpConnection.close();
                return;
            } // end of if

            mainActivity.setHost(host);
            mainActivity.setTcpConnection(tcpConnection);

            // Show successful connect
            mainActivity.runOnUiThread(this::showSuccess);
        } catch (MalformedURLException e) {

            mainActivity.runOnUiThread(() -> showError("Invalid Address"));
        } // end of catch
    } // end of connectToHost


    private void showError(String message) {

        LinearLayout statusBox = getView().findViewById(R.id.statusBox);
        statusBox.setVisibility(View.VISIBLE);

        ImageView tick = getView().findViewById(R.id.tickImage);
        tick.setVisibility(View.GONE);

        ImageView cross = getView().findViewById(R.id.crossImage);
        cross.setVisibility(View.VISIBLE);

        ProgressBar progressbar = getView().findViewById(R.id.progressBar);
        progressbar.setVisibility(View.GONE);

        TextView status = getView().findViewById(R.id.statusText);
        status.setText(message);

        Button connectButton = getView().findViewById(R.id.connectButton);
        connectButton.setEnabled(true);
    } // end of showError


    private void showConnecting() {

        LinearLayout statusBox = getView().findViewById(R.id.statusBox);
        statusBox.setVisibility(View.VISIBLE);

        ImageView tick = getView().findViewById(R.id.tickImage);
        tick.setVisibility(View.GONE);

        ImageView cross = getView().findViewById(R.id.crossImage);
        cross.setVisibility(View.GONE);

        ProgressBar progressbar = getView().findViewById(R.id.progressBar);
        progressbar.setVisibility(View.VISIBLE);

        TextView status = getView().findViewById(R.id.statusText);
        status.setText(R.string.connectingToServer);

        Button connectButton = getView().findViewById(R.id.connectButton);
        connectButton.setEnabled(false);
    } // end of showConnecting


    private void showSuccess() {

        LinearLayout statusBox = getView().findViewById(R.id.statusBox);
        statusBox.setVisibility(View.VISIBLE);

        ImageView tick = getView().findViewById(R.id.tickImage);
        tick.setVisibility(View.VISIBLE);

        ImageView cross = getView().findViewById(R.id.crossImage);
        cross.setVisibility(View.GONE);

        ProgressBar progressbar = getView().findViewById(R.id.progressBar);
        progressbar.setVisibility(View.GONE);

        TextView status = getView().findViewById(R.id.statusText);
        status.setText("Connected");

        Button connectButton = getView().findViewById(R.id.connectButton);
        connectButton.setEnabled(true);


        // Update the input spinner
        LinearLayout inputLayout = getView().findViewById(R.id.inputBox);
        inputLayout.setVisibility(View.VISIBLE);

        Spinner inputSpinner = getView().findViewById(R.id.inputSpinner);

        VMixStatus vmixStatus = mainActivity.getHost().getStatus();

        // TODO change this so that we have a custom item layout so that we can have an array of inputs
        List<String> inputs = new ArrayList<>();
        for(int i = 0; i < vmixStatus.getNumberOfInputs(); i++)
            inputs.add(vmixStatus.getInput(i).getName());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mainActivity, R.layout.spinner_item, inputs);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputSpinner.setAdapter(dataAdapter);

        // Show the next box
        LinearLayout nextBox = getView().findViewById(R.id.nextBox);
        nextBox.setVisibility(View.VISIBLE);

        Button showTallyBtn = getView().findViewById(R.id.showTallyBtn);
        showTallyBtn.setOnClickListener(v -> {

            // Get the currently selected input
            int position = inputSpinner.getSelectedItemPosition();
            mainActivity.setInput(mainActivity.getHost().getStatus().getInput(position));

            // Load the activity
            mainActivity.loadTallyFragment();
        });
    } // end of showSuccess


    public void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);

        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null)
            view = new View(getActivity());

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    } // end of hideKeyboard
} // end of SettingsFragment
