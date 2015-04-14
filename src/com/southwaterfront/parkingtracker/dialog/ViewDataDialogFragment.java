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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.customAdapters.DataAdapter;
import com.southwaterfront.parkingtracker.data.BlockFace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Joel on 4/2/2015.
 */
public class ViewDataDialogFragment extends DialogFragment {

    private View dialogView;
    private List<BlockFace> data;

    public static ViewDataDialogFragment newInstance() {
        return new ViewDataDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        dialogView = inflater.inflate(R.layout.view_data, null);

        final ListView dataList = (ListView) dialogView.findViewById(R.id.listViewViewDataData);

        // TODO
        if ( ((Main) getActivity()).getData() != null) {
            data  = ((Main) getActivity()).getData();
        }
        Collections.sort(data);
        BlockFace[] temp = new BlockFace[data.size()];
        data.toArray(temp);
        DataAdapter dataAdapter = new DataAdapter(getActivity(), R.layout.listview_layout_data, temp);

        dataList.setAdapter(dataAdapter);

        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO Confirm
                        Log.i("ViewDataDialogFragment", "Confirm Clicked!");
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ViewDataDialogFragment.this.getDialog().cancel();
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
}
