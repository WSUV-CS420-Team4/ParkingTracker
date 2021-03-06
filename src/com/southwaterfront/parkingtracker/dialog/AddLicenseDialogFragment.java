package com.southwaterfront.parkingtracker.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.R;

public class AddLicenseDialogFragment extends DialogFragment {

    public static AddLicenseDialogFragment newInstance() {
        return new AddLicenseDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.add_license, null);
        final EditText createLicense = (EditText) dialogView.findViewById(R.id.editTextLicenseField);

        View choosePlateDialogView = inflater.inflate(R.layout.choose_plate, null);
        final TextView plateNo = (TextView) choosePlateDialogView.findViewById(R.id.textViewChoosePlateResult);

        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ((ChoosePlateDialogFragment)getFragmentManager().findFragmentByTag("choosePlate"))
                                .setResultText(createLicense.getText().toString());
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddLicenseDialogFragment.this.getDialog().cancel();
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                final Button cancel = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                final Button confirm = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);

                // Change color of button when pressed
                cancel.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch(event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_darker));
                                break;
                            case MotionEvent.ACTION_UP:
                                cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                                break;
                        }
                        return false;
                    }
                });

                // Change color of button when pressed
                confirm.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_darker));
                                break;
                            case MotionEvent.ACTION_UP:
                                confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                                break;
                        }
                        return false;
                    }
                });

                // Set button background to match dialog
                if (cancel != null) {
                    cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                    cancel.setTextColor(Color.WHITE);
                    cancel.invalidate();
                }

                // Set button background to match dialog
                if (confirm != null) {
                    confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                    confirm.setTextColor(Color.WHITE);
                    confirm.invalidate();
                }
            }
        });

        return dialog;
    }

}
