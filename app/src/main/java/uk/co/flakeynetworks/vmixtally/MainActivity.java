package uk.co.flakeynetworks.vmixtally;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.api.TCPAPIListener;
import uk.co.flakeynetworks.vmix.status.Input;


public class MainActivity extends AppCompatActivity {

    private VMixHost host;
    private TCPAPI tcpConnection;
    private Input input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        // Load the settings fragment
        loadSettingsFragment();
    } // end of onCreate


    private void loadFragment(Fragment fragment) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentPlaceHolder, fragment);

        transaction.commit();
    } // end of loadFragment


    public void loadSettingsFragment() {

        // Set the title bar title
        getSupportActionBar().setTitle(getString(R.string.settingstitle));  // provide compatibility to all the versions

        SettingsFragment fragment = new SettingsFragment();
        loadFragment(fragment);
    } // end of loadSettingsFragment


    public void loadTallyFragment() {

        // Set the title bar title
        getSupportActionBar().setTitle("Tally: " + input.getName());  // provide compatibility to all the versions

        TallyFragment fragment = new TallyFragment();
        loadFragment(fragment);
    } // end of loadTallyFragment


    public VMixHost getHost() { return host; } // end of getHost
    public void setHost(VMixHost host) {

        this.host = host;
        saveHost();
    } // end of setHost


    public TCPAPI getTcpConnection() { return tcpConnection; } // end of getTCPConnection
    public void setTcpConnection(TCPAPI tcpConnection) {

        this.tcpConnection = tcpConnection;

        tcpConnection.addListener(new TCPAPIListener() {
            @Override
            public void disconnected() {

                runOnUiThread(() -> tcpConnectionClosed());
            } // end of disconnected
        });
    } // end of setTcpConnection


    public void setInput(Input input) {

        this.input = input;
    } // end of setInput


    public Input getInput() { return input; } // end of getInput


    private void saveHost() {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_host), host.getAddress());

        editor.commit();
    } // end of saveHost


    public String getLastSavedHost() {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        return sharedPref.getString(getString(R.string.saved_host), "");
    } // end of getLastSavedHost


    public void inputWasRemoved() {

        // TODO show an alert box here to say the input was removed.
        loadSettingsFragment();

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setTitle("Input removed")
                .setMessage(input.getName() + " was removed.")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    } // end of onClick
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    } // end of inputWasRemoved


    public void showReconnectingDialog() {

        // end of cancel
        DialogInterface.OnCancelListener listener = dialog -> {

        };

        ReconnectingDialog dialog = new ReconnectingDialog(this, false, listener);
        dialog.show();
    } // end of showReconnectingDialog


    public void tcpConnectionClosed() {

        loadSettingsFragment();
        showReconnectingDialog();
    } // end of tcpConnectionClosed
} // end of MainActivity
