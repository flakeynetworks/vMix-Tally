package uk.co.flakeynetworks.vmixtally;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;

import uk.co.flakeynetworks.vmix.VMixHost;
import uk.co.flakeynetworks.vmix.api.TCPAPI;
import uk.co.flakeynetworks.vmix.status.Input;
import uk.co.flakeynetworks.vmix.status.InputStatusChangeListener;
import uk.co.flakeynetworks.vmix.status.VMixStatus;

public class TallyActivity extends AppCompatActivity {


    private ImageView tallyColor;
    private Input input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tally);

        tallyColor = findViewById(R.id.tallyColor);

        new Thread(() -> startListeningForTallyChange()).start();
    } // end of onCreate


    private void startListeningForTallyChange() {

        try {
            VMixHost host = new VMixHost("192.168.0.174", 8088);
            //VMixHost host = new VMixHost("10.0.2.2", 8088);

            TCPAPI tcpConnection = new TCPAPI(host);

            if(!tcpConnection.connect());
            if(!host.update());

            VMixStatus status = host.getStatus();

            // Get the first input
            input = status.getInput(0);

            // Add a listener to the input
            InputStatusChangeListener statusListener = new InputStatusChangeListener() {
                @Override
                public void isProgramChange() {

                    runOnUiThread(() -> updateTally());
                } // end of isProgramChange

                @Override
                public void isPreviewChange() {

                    runOnUiThread(() -> updateTally());
                } // end of isPreviewChange
            };

            input.addStatusChangeListener(statusListener);

            // Subscribe for tally changes
            tcpConnection.getProtocol().subscribeTally();
        } catch(IOException e) {
        } // end of catch
    } // end of startListeningForTallyChange


    private void updateTally() {

        if(input.isProgram())
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyProgram));
        else if(input.isPreview())
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyPreview));
        else
            tallyColor.setBackgroundColor(getResources().getColor(R.color.tallyNone));
    } // end of updateTally
} // end of TallyActivity
