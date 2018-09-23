package uk.co.flakeynetworks.vmixtally;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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


public class MainActivity extends AppCompatActivity {


    private EditText addressField;
    private VMixHost host = null;
    private TCPAPI tcpConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressField = findViewById(R.id.addressField);
        Button connectButton = findViewById(R.id.connectButton);

        LinearLayout statusBox = findViewById(R.id.statusBox);

        // end of onClick
        connectButton.setOnClickListener(v -> {

            hideKeyboard();

            // Disable the connect button
            connectButton.setEnabled(false);

            // Show the status box
            statusBox.setVisibility(View.VISIBLE);

            // Attempt to connect to the vmix instance
            new Thread(() -> connectToHost()).start();
        });


        Button showTallyBtn = findViewById(R.id.showTallyBtn);
        showTallyBtn.setOnClickListener(v -> {

            TallyActivity tallyActivity = new TallyActivity();

            Intent intent = new Intent();
            intent.
        });
    } // end of onCreate


    private void connectToHost() {

        // Make sure we can connect to the web api
        try {

            // TODO Make sure to remove the protocol from the address field
            // TODO Add in the ability to set the port
            // TODO remove to tell them must be http://

            host = new VMixHost(addressField.getText().toString(), 8088);
        } catch (MalformedURLException e) {

            runOnUiThread(() -> showError("Invalid Address"));
        } // end of catch

        // Make sure we can connect to the tcp api
        tcpConnection = new TCPAPI(host);

        // Connect tp the tcp api
        if(!tcpConnection.connect()) {

            runOnUiThread(() -> showError("Could not connect. Check port 8099"));
        } // end of if

        // Get an update via the web api
        if(!host.update()) {

            runOnUiThread(() -> showError("Could not connect. Check the web controller port"));
        } // end of if

        // Show successful connect
        runOnUiThread(() -> showSuccess());
    } // end of connectToHost


    private void showError(String message) {

        ImageView tick = findViewById(R.id.tickImage);
        tick.setVisibility(View.GONE);

        ImageView cross = findViewById(R.id.crossImage);
        cross.setVisibility(View.VISIBLE);

        ProgressBar progressbar = findViewById(R.id.progressBar);
        progressbar.setVisibility(View.GONE);

        TextView status = findViewById(R.id.statusText);
        status.setText(message);
    } // end of showError


    private void showSuccess() {

        ImageView tick = findViewById(R.id.tickImage);
        tick.setVisibility(View.VISIBLE);

        ImageView cross = findViewById(R.id.crossImage);
        cross.setVisibility(View.GONE);

        ProgressBar progressbar = findViewById(R.id.progressBar);
        progressbar.setVisibility(View.GONE);

        TextView status = findViewById(R.id.statusText);
        status.setText("Connected");


        // Update the input spinner
        LinearLayout inputLayout = findViewById(R.id.inputBox);
        inputLayout.setVisibility(View.VISIBLE);

        Spinner inputSpinner = findViewById(R.id.inputSpinner);

        VMixStatus vmixStatus = host.getStatus();

        // TODO change this so that we have a custom item layout so that we can have an array of inputs
        List<String> inputs = new ArrayList<>();
        for(int i = 0; i < vmixStatus.getNumberOfInputs(); i++)
            inputs.add(vmixStatus.getInput(i).getName());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, inputs);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputSpinner.setAdapter(dataAdapter);

        // Show the next box
        LinearLayout nextBox = findViewById(R.id.nextBox);
        nextBox.setVisibility(View.VISIBLE);


    } // end of showSuccess


    public void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null)
            view = new View(this);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    } // end of hideKeyboard
} // end of MainActivity
