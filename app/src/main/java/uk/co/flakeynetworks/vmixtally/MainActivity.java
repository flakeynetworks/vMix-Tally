package uk.co.flakeynetworks.vmixtally;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.api.TCPAPIListener;
import uk.co.flakeynetworks.vmix.status.Input;


public class MainActivity extends AppCompatActivity {

    private static VMixHost host;
    private static TCPAPI tcpConnection;
    private static Input input;
    private static boolean attemptingReconnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        if(savedInstanceState == null) {

            // Load the settings fragment
            loadSettingsFragment();
        } // end of if
    } // end of onCreate


    private void loadFragment(Fragment fragment) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragmentPlaceHolder, fragment);

        transaction.commitAllowingStateLoss();
    } // end of loadFragment


    public void loadSettingsFragment() {

        // Set the title bar title
        getSupportActionBar().setTitle(getString(R.string.settingstitle));  // provide compatibility to all the versions

        SettingsFragment fragment = new SettingsFragment();
        loadFragment(fragment);
    } // end of loadSettingsFragment


    public void loadTallyFragment() {

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

        // end of onClick
        builder.setTitle("Input removed")
                .setMessage(input.getName() + " was removed.")
                .setPositiveButton(R.string.ok, (dialog, which) -> {

                    // TODO cancel the reconnect here
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

        tcpConnection = null;

        attemptingReconnect = true;
        loadSettingsFragment();
    } // end of tcpConnectionClosed


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    } // end of onCreateOptionsMenu


    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.settings)
            loadSettingsFragment();

        return super.onOptionsItemSelected(item);
    } // end of onOptionsItemSelected


    public boolean isAttemptingToReconnect() {

        return attemptingReconnect;
    } // end of isAttemptingToReconnect
} // end of MainActivity
