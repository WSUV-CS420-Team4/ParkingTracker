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
import android.widget.ListView;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;

/**
 * Created by Joel on 3/4/2015.
 */
public class ChoosePlateDialogFragment extends DialogFragment {

    private ArrayAdapter<String> arrayAdapter;
    private Button addLicense;
    private Button addFlag;
    private ListView listView;
    private TextView plateNo;

    public static ChoosePlateDialogFragment newInstance() {
        ChoosePlateDialogFragment dialog = new ChoosePlateDialogFragment();
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        /*listView = (ListView) inflater.inflate(R.layout.choose_plate, null).findViewById(R.id.listViewChoosePlate);
        plateNo = (TextView) inflater.inflate(R.layout.choose_plate, null).findViewById(R.id.textViewChoosePlateResult);

        if (listView == null)
            Log.i("WHAT?!", "HOW CAN THIS BE?!");

        List<String> temp = ((Main)getActivity()).getLicensePlates();
        Log.i("ChoosePlateDialog", "temp = " + temp.size());
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, temp );
        arrayAdapter.setDropDownViewResource(R.layout.choose_plate);

        listView.setAdapter(arrayAdapter);
        //listView.setAdapter( ((Main)getActivity()).getArrayAdapter() );

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ListViewChoosePlate", "You clicked Item: " + id + " at position:" + position);
                plateNo.setText((String) parent.getItemAtPosition(position));
            }
        });*/

        View dialogView = inflater.inflate(R.layout.choose_plate, null);

        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO SetFlagDialogFragment
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChoosePlateDialogFragment.this.getDialog().cancel();
                    }
                });

        listView = (ListView) dialogView.findViewById(R.id.listViewChoosePlate);
        plateNo = (TextView) dialogView.findViewById(R.id.textViewChoosePlateResult);
        addLicense = (Button) dialogView.findViewById(R.id.buttonChoosePlateAddLicense);
        addFlag = (Button) dialogView.findViewById(R.id.buttonChoosePlateAddFlag);

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
                // TODO
            }
        });

        addFlag.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                // TODO
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

    /*@Override
    public void onResume() {
        super.onResume();
        Log.i("ChoosePlateDialog", "onResume");

        if (listView != null) {
            List<String> temp = ((Main)getActivity()).getLicensePlates();
            Log.i("ChoosePlateDialog2", "temp = " + temp.size());
            arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, temp );
            arrayAdapter.setDropDownViewResource(R.layout.choose_plate);
            arrayAdapter.notifyDataSetChanged();

            listView.setAdapter(arrayAdapter);

            Log.i("ChoosePlateDialog2", "listView != null");
        } else {
            Log.i("ChoosePlateDialog2", "listView == null");
        }
    }*/

    /*public void setAdapter(List<String> list) {
        //arrayAdapter.clear();
        arrayAdapter.addAll(list);
        arrayAdapter.notifyDataSetChanged();
    }*/

    /*public void setAdapter(ArrayAdapter<String> arrayAdapter) {
        this.arrayAdapter = arrayAdapter;

        if (arrayAdapter.isEmpty()) {
            Log.i("arrayAdapter", "Looks like its empty...!");
        } else {
            Log.i("arrayAdapter", "Count = " + arrayAdapter.getCount());
        }

        if (listView != null) {
            listView.setAdapter(this.arrayAdapter);
        } else {
            Log.i("ChoosePlateDialog", "listView == null");
        }
    }*/
}
