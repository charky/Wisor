package com.enterprise.charky.wisor.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.enterprise.charky.wisor.R;

/**
 * Created by charky on 20.08.16.
 * Dialog for changing the wireless switch name after long press
 */
public class InputDialogFragment extends DialogFragment {

    public interface InputDialogListener {
        void onInputDialogPositiveClick(Bundle dialogBundle);
    }

    public static final String KEY_TITLE = "titleCaption";
    public static final String KEY_EDITTEXT = "editText";


    // Use this instance of the interface to deliver action events
    private InputDialogListener mListener;
    private EditText mEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Get Args
        String strTitle = getArguments().getString(KEY_TITLE);
        String strEditText = getArguments().getString(KEY_EDITTEXT);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle(strTitle);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.input_dialog, null);
        builder.setView(v);

        //Set View Values
        mEditText = (EditText) v.findViewById(R.id.dialogEditText);
        mEditText.setText(strEditText);
        // Show soft keyboard automatically
        mEditText.requestFocus();

        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                Bundle dialogBundle = getArguments();
                dialogBundle.putString(KEY_EDITTEXT, InputDialogFragment.this.mEditText.getText()
                        .toString());
                mListener.onInputDialogPositiveClick(dialogBundle);

            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                dialog.cancel();
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
            mListener = (InputDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement InputDialogListener");
        }
    }
}
