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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;

/**
 * ChoosePlateDialogFragment
 */
public class ChoosePlateDialogFragment extends DialogFragment {

    private View dialogView;

    public static ChoosePlateDialogFragment newInstance() {
        return new ChoosePlateDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.choose_plate, null);

        final ListView listView = (ListView) dialogView.findViewById(R.id.listViewChoosePlate);
        final TextView plateNo = (TextView) dialogView.findViewById(R.id.textViewChoosePlateResult);
        final Button addLicense = (Button) dialogView.findViewById(R.id.buttonChoosePlateAddLicense);
        final Button addFlag = (Button) dialogView.findViewById(R.id.buttonChoosePlateAddFlag);

        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO Confirm
                        Log.i("ChoosePlateDialog", "Confirm Clicked!");
                        ((Main) getActivity()).showLocationSelectDialog();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChoosePlateDialogFragment.this.getDialog().cancel();
                    }
                });

        listView.setAdapter( ((Main)getActivity()).getArrayAdapter() );
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ListViewChoosePlate", "You clicked Item: " + id + " at position:" + position);
                plateNo.setText((String) parent.getItemAtPosition(position));
            }
        });

        if ( ((Main)getActivity()).getLicensePlates().size() == 1 ) {
            plateNo.setText("1 Result");
        } else {
            plateNo.setText(((Main)getActivity()).getLicensePlates().size() + " Results");
        }

        addLicense.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                ((Main) getActivity()).showAddLicenseDialog();
            }
        });

        addFlag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                ((Main) getActivity()).showSetFlagsDialog();
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

    public void setResultText(final String string) {
        ((TextView) dialogView.findViewById(R.id.textViewChoosePlateResult)).setText(string);
    }
}
