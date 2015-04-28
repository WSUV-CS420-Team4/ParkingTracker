package com.southwaterfront.parkingtracker.dialog;

import java.util.ArrayList;
import java.util.List;

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

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;

public class LocationSelectDialogFragment extends DialogFragment {

    private List<Integer> blockArray;
    private List<String> faceArray;
    private List<Integer> stallArray;

    public static LocationSelectDialogFragment newInstance() {
        return new LocationSelectDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if ( ((Main) getActivity()).getBlockArray() != null) {
            blockArray  = new ArrayList<Integer>(((Main) getActivity()).getBlockArray());
        }

        if ( ((Main) getActivity()).getFaceArray() != null) {
            faceArray  = new ArrayList<String>(((Main) getActivity()).getFaceArray());
        }

        if ( ((Main) getActivity()).getStallArray() != null) {
            stallArray  = new ArrayList<Integer>(((Main) getActivity()).getStallArray());
        }

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
                        Log.i("LocationSelectDialog", "Confirm Clicked!");
                        // Add the newly recorded license data to Main.java's data
                        boolean added = ((Main) getActivity()).addData();

                        ((ChoosePlateDialogFragment)getFragmentManager().findFragmentByTag("choosePlate")).dismiss();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LocationSelectDialogFragment.this.getDialog().cancel();
                    }
                });

        // ---

        ArrayAdapter<Integer> tempIntAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, blockArray);
        tempIntAdapter.setDropDownViewResource(R.layout.spinner_layout);

        block.setAdapter(tempIntAdapter);

        block.setSelection( ((Main) getActivity()).getCurrentBlock() );

        block.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("BlockSpinner", "" + parent.getItemAtPosition(position));
                ((Main) getActivity()).setCurrentBlock(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // ---

        ArrayAdapter<String> tempStrAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, faceArray);
        tempStrAdapter.setDropDownViewResource(R.layout.spinner_layout);

        face.setAdapter(tempStrAdapter);

        face.setSelection(((Main) getActivity()).getCurrentFace());

        face.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("FaceSpinner", "" + parent.getItemAtPosition(position));
                ((Main) getActivity()).setCurrentFace(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // ---

        tempIntAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, stallArray);
        tempIntAdapter.setDropDownViewResource(R.layout.spinner_layout);

        stall.setAdapter(tempIntAdapter);

        stall.setSelection( ((Main) getActivity()).getCurrentStall() );

        stall.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("StallSpinner", "" + parent.getItemAtPosition(position));
                ((Main) getActivity()).setCurrentStall(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // ---

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
