package uk.co.flakeynetworks.vmixtally.ui.tally;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import uk.co.flakeynetworks.vmixtally.R;
import uk.co.flakeynetworks.vmixtally.ViewModelFactory;
import uk.co.flakeynetworks.vmixtally.model.TallyInput;
import uk.co.flakeynetworks.vmixtally.ui.dialog.ReconnectingDialog;

/**
 * Created by Richard Stokes on 9/24/2018.
 */
public class TallyFragment extends Fragment {


    private ImageView tallyColor;
    private TallyViewModel viewModel;
    private TallyInput currentInput;
    private ReconnectingDialog reconnectingDialog;


    private TallyNavigation navigation = new TallyNavigation() {

        @Override
        public void navigateToPreviewScreen() {

            // Pop back one
            Navigation.findNavController(getView()).navigate(R.id.action_tally_pop_to_settings);
        } // end of navigateToSettingsMenu

        @Override
        public void navigateToSettings() {

            Navigation.findNavController(getView()).navigate(R.id.action_tally_pop_to_settings);
        } // end of navigateToSettings
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tally, container, false);

        // Get the view model
        ViewModelFactory factory = ViewModelFactory.getInstance(getActivity().getApplication());
        viewModel = ViewModelProviders.of(this, factory).get(TallyViewModel.class);

        viewModel.getInput().observe(this, input -> {

            // Remove the listener from the old input
            TallyInput oldInput = currentInput;
            if(oldInput != null)
                oldInput.getTallyStatus().removeObservers(this);


            // Add the listener to the new input
            currentInput = input;
            if(input != null) {

                input.getTallyStatus().observe(this, this::updateTally);

                // Set the title bar title
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Tally: " + currentInput.getInput().getName());  // provide compatibility to all the versions
            } else {

                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Tally");  // provide compatibility to all the versions

                // Check if there was an input before this null
                showInputRemovedDialog(oldInput);
                navigation.navigateToPreviewScreen();
            } // end of else
        });

        // List for reconnecting
        viewModel.getIsReconnecting().observe(this, truth -> {

            if(truth)
                // Show reconnecting
                showReconnectingDialog();
            else
                removeReconnectingDialog();
        });

        tallyColor = view.findViewById(R.id.tallyColor);
        tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyNone));

        setHasOptionsMenu(true);

        return view;
    } // end of onCreateView


    private void removeReconnectingDialog() {

        if(reconnectingDialog != null)
            reconnectingDialog.cancel();
    } // end of reconnected


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
                navigation.navigateToSettings();
            } // end of execute
        });

        reconnectingDialog.show();
    } // end of showReconnectingDialog


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.tally_actionbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    } // end of onCreateOptionsMenu


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.settingsMenuItem)
            navigation.navigateToSettings();

        return super.onOptionsItemSelected(item);
    } // end of onOptionsItemSelected



    private void showInputRemovedDialog(TallyInput oldInput) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(getContext());

        String oldInputName = "Input";
        if(oldInput != null) oldInputName = oldInput.getInput().getName();

        builder.setTitle("Input removed")
                .setMessage(oldInputName + " was removed.")
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    } // end of showInputRemovedDialog


    private void updateTally(int status) {

        TextView tallyText = getView().findViewById(R.id.tallyText);

        switch(status) {
            case TallyInput.STATE_ON_PROGRAM:
                tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyProgram));
                tallyText.setText(getString(R.string.tally_program));
                break;

            case TallyInput.STATE_ON_PREVIEW:
                tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyPreview));
                tallyText.setText(getString(R.string.tally_preview));
                break;

            case TallyInput.STATE_ON_STANDBY:
                tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyNone));
                tallyText.setText(getString(R.string.tally_none));
                break;

            case TallyInput.STATE_INVALID:
            default:
                break;
        } // end of switch
    } // end of updateTally
} // end of TallyFragment
