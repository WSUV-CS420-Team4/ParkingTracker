package com.southwaterfront.parkingtracker.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joel on 3/5/2015.
 */
public class LocationSelectDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.location_select, null);

        final Spinner block = (Spinner) dialogView.findViewById(R.id.spinnerLocationSelectBlock);
        final Spinner face = (Spinner) dialogView.findViewById(R.id.spinnerLocationSelectFace);
        final Spinner stall = (Spinner) dialogView.findViewById(R.id.spinnerLocationSelectStall);

        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO SetFlagDialogFragment
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LocationSelectDialogFragment.this.getDialog().cancel();
                    }
                });

        List<String> tempList = new ArrayList<String>();

        // TODO Check what data I get from getDataModel and pre populate spinners
        AssetManager assetManager = AssetManager.getInstance();
        assetManager.assetSanityCheck();
        //assetManager.getDataModel();


        tempList.add("A");
        tempList.add("B");
        tempList.add("C");
        ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, tempList);
        tempAdapter.setDropDownViewResource(R.layout.spinner_layout);

        block.setAdapter(tempAdapter);

        block.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("BlockSpinner", "" + parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
