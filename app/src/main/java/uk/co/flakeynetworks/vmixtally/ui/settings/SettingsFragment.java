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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmix.status.VMixStatus;
import uk.co.flakeynetworks.vmixtally.R;
import uk.co.flakeynetworks.vmixtally.ViewModelFactory;
import uk.co.flakeynetworks.vmixtally.databinding.FragmentSettingsBinding;
import uk.co.flakeynetworks.vmixtally.model.ErrorMessage;
import uk.co.flakeynetworks.vmixtally.ui.dialog.ReconnectingDialog;

/**
 * Created by Richard Stokes on 9/24/2018.
 */

public class SettingsFragment extends Fragment {

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

        // Get the view model
        ViewModelFactory vmFactory = ViewModelFactory.getInstance(getActivity().getApplication());
        viewModel = ViewModelProviders.of(this, vmFactory).get(SettingsViewModel.class);

        FragmentSettingsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        View view = binding.getRoot();

        binding.setViewmodel(viewModel);
        binding.setController(this);
        binding.setLifecycleOwner(this);


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


    public void connectButtonClicked() {

        // Check if we are already connected in which case disconnect from host
        if(viewModel.getTcpConnection().getValue() != null)
            performDisconnectFromHost();
        else
            performConnectToHost();
    } // end of connectButtonClicked


    private void performDisconnectFromHost() {

        viewModel.disconnectFromHost();
    } // end of performDisconnectFromHost


    private void performConnectToHost() {

        // Validate the address
        if(!viewModel.isAddressValid())
            return;

        // Validate the port number
        if(!viewModel.isPortValid())
            return;

        hideKeyboard();
        showConnecting();

        // Attempt to connect to the vmix instance
        viewModel.connectToHost();
    } // end of performConnectToHost


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.settings_actionbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    } // end of onCreateOptionsMenu


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menuItemHowToVideo)
            navigator.showYouTubeHowToVideo();

        return super.onOptionsItemSelected(item);
    } // end of onOptionsItemSelected


    private void showReconnectingDialog() {

        // Means there is already one showing
        if(reconnectingDialog != null) return;

        DialogInterface.OnCancelListener listener = dialog -> {

            if(reconnectingDialog == null) return;

            if(reconnectingDialog.isShowing())
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

        if(reconnectingDialog != null && reconnectingDialog.isShowing())
            reconnectingDialog.cancel();
    } // end of reconnected


    @Override
    public void onResume() {

        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.settings_title));  // provide compatibility to all the versions
        hideKeyboard();
    } // end of onResume


    private void showError(@NonNull ErrorMessage message) {

        // Check to see if this message is valid
        if(message == null || message.hasBeenDisplayed()) return;

        new Handler(Looper.getMainLooper()).post(() -> {

            viewModel.displayConnectingBox(false);
            viewModel.displayStatusBox(true);
            viewModel.displayTick(false);
            viewModel.displayCross(true);
            viewModel.setStatusText(message.toString());
            viewModel.setConnectButtonEnabled(true);
            viewModel.displayInputs(false);
            viewModel.displayNextButton(false);

            message.setHasBeenDisplayed();
        });
    } // end of showError


    private void showConnecting() {

        viewModel.displayConnectingBox(true);
        viewModel.displayStatusBox(false);
        viewModel.setConnectButtonEnabled(false);
        viewModel.displayInputs(false);
        viewModel.displayNextButton(false);
    } // end of showConnecting


    private void hideConnectionSuccess() {

        // Enable the details boxes
        viewModel.setAddressEnabled(true);
        viewModel.setPortEnabled(true);

        viewModel.setConnectButtonText(getString(R.string.connect_button_text));

        viewModel.displayStatusBox(false);
        viewModel.displayInputs(false);
        viewModel.displayNextButton(false);
    } // end of hideConnectionSuccess


    private void showSuccess() {

        // Disable the details boxes
        viewModel.setAddressEnabled(false);
        viewModel.setPortEnabled(false);

        viewModel.setConnectButtonText(getString(R.string.disconnect_button_text));

        viewModel.setConnectButtonEnabled(true);

        viewModel.displayConnectingBox(false);
        viewModel.displayStatusBox(true);

        viewModel.displayTick(true);
        viewModel.displayCross(false);
        viewModel.setStatusText(getString(R.string.connected));

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

        viewModel.displayInputs(true);
        viewModel.displayNextButton(true);

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


        // Start listening for error messages
        viewModel.getErrorMessages().observe(this, this::showError);
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
