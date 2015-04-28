package com.southwaterfront.parkingtracker.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.customAdapters.DataAdapter;
import com.southwaterfront.parkingtracker.data.BlockFace;

import java.util.Collections;
import java.util.List;

/**
 * Created by Joel on 4/2/2015.
 */
public class ViewDataDialogFragment extends DialogFragment {

    private View dialogView;
    private DataAdapter dataAdapter;
    private List<BlockFace> data;

    public static ViewDataDialogFragment newInstance() {
        return new ViewDataDialogFragment();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, view, menuInfo);
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        menu.setHeaderTitle( "" );
        //menu.add(Menu.NONE, 1, 1, "Edit");
        menu.add(Menu.NONE, 2, 2, "Delete");

        Log.i("LongClick", "Location: " + dataAdapter.data.get(info.position).getBlock() + " "
                + dataAdapter.data.get(info.position).getFace() + " "
                + dataAdapter.data.get(info.position).getStall());

        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onContextItemSelected(item);

                Log.i("LongClick", "Select: " + item.getItemId());

                switch (item.getItemId()) {
                    case 1:
                        // Edit
                        break;
                    case 2:
                        // Delete
                        ((Main) getActivity()).removeData( dataAdapter.data.get(info.position).getBlock(),
                                dataAdapter.data.get(info.position).getFace(),
                               dataAdapter.data.get(info.position).getStall());

                        // Remove from adapter
                        dataAdapter.remove(info.position);
                        dataAdapter.notifyDataSetChanged();
                        break;
                }

                return true;
            }
        };

        for (int i = 0, n = menu.size(); i < n; i++)
            menu.getItem(i).setOnMenuItemClickListener(listener);
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

        dataAdapter = new DataAdapter(getActivity(), R.layout.listview_layout_data, temp);

        dataList.setAdapter(dataAdapter);
        registerForContextMenu(dataList);


        builder.setView(dialogView)
                .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("ViewDataDialogFragment", "Confirm Clicked!");
                    }
                });

                /*.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ViewDataDialogFragment.this.getDialog().cancel();
                    }
                });*/

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                //final Button cancel = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                final Button confirm = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);

                // Change color of button when pressed
                /*cancel.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_darker));
                                break;
                            case MotionEvent.ACTION_UP:
                                cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                                break;
                        }
                        return false;
                    }
                });*/

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
                /*cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                cancel.setTextColor(Color.WHITE);
                cancel.invalidate();*/

                // Set button background to match dialog
                confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
                confirm.setTextColor(Color.WHITE);
                confirm.invalidate();
            }
        });

        return dialog;
    }

}
