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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.client.HttpClient;

/**
 * Created by Joel on 4/13/2015.
 */
public class OptionsDialogFragment extends DialogFragment {

    private View dialogView;

    private int numResults;

    private List<Integer> resultArray = new ArrayList<Integer>() {{
        add(1);
        add(2);
        add(3);
        add(4);
        add(5);
        add(6);
        add(7);
        add(8);
        add(9);
        add(10);
    }};

    public static OptionsDialogFragment newInstance() {
        return new OptionsDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.options, null);

        final Spinner results = (Spinner) dialogView.findViewById(R.id.spinnerOptionsResults);
        final Button loginOut = (Button) dialogView.findViewById(R.id.buttonOptionsLoginOut);

        /*final ListView listView = (ListView) dialogView.findViewById(R.id.listViewChoosePlate);
        final TextView plateNo = (TextView) dialogView.findViewById(R.id.textViewChoosePlateResult);
        final Button addLicense = (Button) dialogView.findViewById(R.id.buttonChoosePlateAddLicense);
        final Button addFlag = (Button) dialogView.findViewById(R.id.buttonChoosePlateAddFlag);*/

        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO Confirm
                        Log.i("ChoosePlateDialog", "Confirm Clicked!");
                        ((Main) getActivity()).setOCRResults(numResults);
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        OptionsDialogFragment.this.getDialog().cancel();
                    }
                });

        // ---

        ArrayAdapter<Integer> tempIntAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, resultArray) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(16);
                ((TextView) v).setGravity(Gravity.CENTER);
                ((TextView) v).setPadding(0, 5, 0, 5);
                //((TextView) v).setPadding(20, 10, 20, 10);

                return v;
            }
        };

        tempIntAdapter.setDropDownViewResource(R.layout.options_results);

        results.setAdapter(tempIntAdapter);

        numResults = ((Main) getActivity()).getOCRResults();
        results.setSelection( ((Main) getActivity()).getOCRResults() - 1 );

        results.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ResultSpinner", "Result: " + parent.getItemAtPosition(position) + " Position: " + position);
                numResults = (position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // ---

        loginOut.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Login / Logout
                Log.i("loginOut", "Button Pressed!");
                if(!HttpClient.isLoggedIn()) {
                	final LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
            		loginDialogFragment.show(getActivity().getFragmentManager(), "Login");
                } else {
                	HttpClient.logout();
                }
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
                cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                cancel.setTextColor(Color.WHITE);
                cancel.invalidate();

                // Set button background to match dialog
                confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                confirm.setTextColor(Color.WHITE);
                confirm.invalidate();
            }
        });

        return dialog;
    }
}
