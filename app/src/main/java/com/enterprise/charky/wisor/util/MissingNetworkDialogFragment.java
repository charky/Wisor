package com.enterprise.charky.wisor.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.enterprise.charky.wisor.R;

/**
 * Created by charky on 13.12.15.
 * Dialog for prompting to activate the Network
 */
public class MissingNetworkDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface MissingNetworkDialogListener {
        void onMNDialogPositiveClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    private MissingNetworkDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.warning_no_lan_title);
        builder.setMessage(R.string.warning_no_lan_msg);
        builder.setPositiveButton(R.string.bt_wifi_is_on, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onMNDialogPositiveClick(MissingNetworkDialogFragment.this);

            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (MissingNetworkDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement MissingNetworkDialogListener");
        }
    }


}
