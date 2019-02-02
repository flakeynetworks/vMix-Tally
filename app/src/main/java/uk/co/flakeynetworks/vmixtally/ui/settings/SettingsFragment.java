package uk.co.flakeynetworks.vmixtally.ui.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmix.status.VMixStatus;
import uk.co.flakeynetworks.vmixtally.R;
import uk.co.flakeynetworks.vmixtally.ViewModelFactory;
import uk.co.flakeynetworks.vmixtally.ui.dialog.ReconnectingDialog;

/**
 * Created by Richard Stokes on 9/24/2018.
 */

public class SettingsFragment extends Fragment {


    private EditText addressField;
    private EditText portField;

    private ReconnectingDialog reconnectingDialog;

    private SettingsViewModel viewModel;
    private SettingsNavigator navigator = new SettingsNavigator() {

        @Override
        public void showTally() {

            Navigation.findNavController(getView()).navigate(R.id.action_settings_to_tally);
        } // end of showTally

        @Override
        public void showYouTubeHowToVideo() {
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(getString(R.string.youtube_how_to_url))));
        } // end of showYouTubeHowToVideo
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Get the view model
        ViewModelFactory vmFactory = ViewModelFactory.getInstance(getActivity().getApplication());
        viewModel = ViewModelProviders.of(this, vmFactory).get(SettingsViewModel.class);

        // Set the address field & port field
        addressField = view.findViewById(R.id.addressField);
        addressField.setText(viewModel.getSavedHost());
        addressField.setSelection(addressField.getText().toString().length());
        portField = view.findViewById(R.id.portNumber);

        // Setup the connect button
        Button connectButton = view.findViewById(R.id.connectButton);
        connectButton.setOnClickListener(v -> {

            hideKeyboard();
            showConnecting();

            // Validate the port number
            try {
                int port = Integer.parseInt(portField.getText().toString());

                if(port < 1 || port > 65535)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                portField.setError("Invalid port number");
            } // end of catch

            // Attempt to connect to the vmix instance
            viewModel.connectToHost(addressField.getText().toString(), Integer.parseInt(portField.getText().toString()));
        });
        
        // Setup listening for changes in the host
        setupHostListening();

        // List for reconnecting
        viewModel.getIsReconnecting().observe(this, truth -> {

            if(truth)
                // Show reconnecting
                showReconnectingDialog();
            else
                removeReconnectingDialog();
        });

        setHasOptionsMenu(true);

        return view;
    } // end of onCreateView


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.settings_actionbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    } // end of onCreateOptionsMenu


    private void showReconnectingDialog() {

        // Means there is already one showing
        if(reconnectingDialog != null) return;

        DialogInterface.OnCancelListener listener = dialog -> {

            reconnectingDialog.dismiss();
            reconnectingDialog = null;
        };

        reconnectingDialog = new ReconnectingDialog(getContext(), false, listener);
        reconnectingDialog.setCancelAction(new ReconnectingDialog.CancelAction() {
            @Override
            public void execute() {

                viewModel.cancelReconnect();
            } // end of execute
        });

        reconnectingDialog.show();
    } // end of showReconnectingDialog


    private void removeReconnectingDialog() {

        if(reconnectingDialog != null)
            reconnectingDialog.cancel();
    } // end of reconnected


    @Override
    public void onResume() {

        super.onResume();

        hideKeyboard();
    } // end of onResume


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        hideKeyboard();
    } // end of onViewCreated


    private void showError(@NonNull String message) {

        new Handler(Looper.getMainLooper()).post(() -> {

            try {
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

                LinearLayout inputLayout = getView().findViewById(R.id.inputBox);
                inputLayout.setVisibility(View.GONE);

                LinearLayout nextBox = getView().findViewById(R.id.nextBox);
                nextBox.setVisibility(View.GONE);
            } catch(NullPointerException e) {

                Crashlytics.setString("Dialog Error Message", message);
                if(getView() != null)
                    Crashlytics.setString("View Object", getView().toString());
                else
                    Crashlytics.setString("View Object", "null");

                Crashlytics.logException(e);
            } // end of catch
        });
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

        LinearLayout inputLayout = getView().findViewById(R.id.inputBox);
        inputLayout.setVisibility(View.GONE);

        LinearLayout nextBox = getView().findViewById(R.id.nextBox);
        nextBox.setVisibility(View.GONE);
    } // end of showConnecting


    private void hideConnectionSuccess() {

        // TODO implement at some point, not sure if this is a reachable state
    } // end of hideConnectionSuccess


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


        // Setup the show tally button
        Button showTallyBtn = getView().findViewById(R.id.showTallyBtn);
        showTallyBtn.setOnClickListener(v -> {

            // Get the currently selected input
            Spinner inputSpinner = getView().findViewById(R.id.inputSpinner);

            Input input = (Input) inputSpinner.getSelectedItem();
            viewModel.inputSelected(input);

            // Load the activity
            navigator.showTally();
        });


        // Show the next box
        LinearLayout nextBox = getView().findViewById(R.id.nextBox);
        nextBox.setVisibility(View.VISIBLE);

        hideKeyboard();
    } // end of showSuccess


    private void setupHostListening() {

        // Start listening to any changes of the vmix host.
        viewModel.getHost().observe(this, host -> {

            if(host == null) return;

            VMixStatus vmixStatus = host.getStatus();
            if(vmixStatus == null) return;

            updateListOfInputs(vmixStatus);
        });


        // See if we need to load from a previous state
        viewModel.getTcpConnection().observe(this, tcpConnection -> {

            if(tcpConnection == null)
                hideConnectionSuccess();
            else
                showSuccess();
        });


        // Start listening for changes to the list of inputs
        viewModel.getInputsChanged().observe(this, changed -> {
            if(changed)
                updateListOfInputs();
        });
    } // end of setupHostListening


    private void updateListOfInputs() {

        updateListOfInputs(viewModel.getHost().getValue().getStatus());
    } // end of updateListOfInputs


    private void updateListOfInputs(VMixStatus vmixStatus) {

        Spinner inputSpinner = getView().findViewById(R.id.inputSpinner);
        if(inputSpinner == null) return;

        List<Input> inputs = new ArrayList<>();
        for(int i = 0; i < vmixStatus.getNumberOfInputs(); i++) {

            Input input = vmixStatus.getInput(i);
            if(input == null) continue;

            inputs.add(vmixStatus.getInput(i));
        } // end of for

        ArrayAdapter<Input> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.input_spinner_item, inputs);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputSpinner.setAdapter(dataAdapter);
    } // end of updateListOfInputs


    private void hideKeyboard() {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    } // end of hideKeyboard
} // end of SettingsFragment
