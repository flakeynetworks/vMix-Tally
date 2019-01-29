package uk.co.flakeynetworks.vmixtally.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import uk.co.flakeynetworks.vmixtally.R;

/**
 * Created by Richard Stokes on 9/26/2018.
 */

public class ReconnectingDialog extends Dialog {


    public static class CancelAction {

        public void execute() { } // end of execute
    } // end of CancelAction

    private CancelAction cancelAction;


    public ReconnectingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {

        super(context, cancelable, cancelListener);
    } // end of constructor


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_reconnecting);


        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {

            if(cancelAction != null)
                cancelAction.execute();
        });
        setCanceledOnTouchOutside(false);
    } // end of onCreate


    public void setCancelAction(CancelAction cancelAction) {

        this.cancelAction = cancelAction;
    } // end of setCancelAction
}  // end of ReconnectingDialog
