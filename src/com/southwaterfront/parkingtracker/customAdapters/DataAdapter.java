package com.southwaterfront.parkingtracker.customAdapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.data.BlockFace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Joel on 4/3/2015.
 */
public class DataAdapter extends ArrayAdapter<BlockFace> {

    Context context;
    int layoutResourceId;
    ParkingData[] data;

    public DataAdapter(Context context, int layoutResourceId, BlockFace[] blockFaceArray) {
        super(context, layoutResourceId, blockFaceArray);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        //this.data = data;

        List<ParkingData> temp = new ArrayList<ParkingData>();

        for (BlockFace face : blockFaceArray) {
            for (int i = 0; i < face.getParkingStalls().size(); i++) {
                if ( face.getParkingStalls().get(i).plate.equals("") ||  face.getParkingStalls().get(i).plate == null ) {
                    // Do nothing...
                } else {
                    temp.add( new ParkingData(face.block, face.face, i, face.getParkingStalls().get(i).plate, face.getParkingStalls().get(i).attr) );
                    //Log.i("DataAdapter Added", "block: " + face.block + " face: " + face.face + " stall: " + i +
                    //        " plate: " + face.getParkingStalls().get(i).plate + " attr: " + face.getParkingStalls().get(i).attr);
                    //Log.i("DataAdapter Size", "" + temp.size());
                }
            }
        }

        data = new ParkingData[temp.size()];
        temp.toArray( data );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DataHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DataHolder();
            holder.title = (TextView)row.findViewById(R.id.textViewDataLayoutTitle);
            holder.content = (TextView)row.findViewById(R.id.textViewDataLayoutContent);

            row.setTag(holder);
        }
        else
        {
            holder = (DataHolder)row.getTag();
        }

        //Log.i("getView position", "" + position);
        ParkingData parkingData = data[position];

        String title = "Block: " + parkingData.block + " Face: ";
        if ( parkingData.face != null ) { title += parkingData.face; } else { title += "-"; }
        title += " Stall: " + parkingData.stall;
        holder.title.setText(title);

        String content = "License: ";
        if ( parkingData.plate != null) { content += parkingData.plate; } else { content += "-"; }
        content += " Attr: ";
        if ( parkingData.attr != null) { content += parkingData.attr.toString(); } else { content += "-"; }
        holder.content.setText(content);

        return row;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    static class ParkingData {

        ParkingData() {
            this.block = 0;
            this.face = null;
            this.stall = 0;
            this.plate = null;
        }

        ParkingData(int block, String face, int stall, String plate, String[] attr) {
            this.block = block;
            this.face = face;
            this.stall = stall;
            this.plate = plate;
            this.attr = attr;
        }

        int block;
        String face;
        int stall;
        boolean modifiedSince;

        String plate;
        Date dTStamp;
        String[] attr;
    }

    static class DataHolder {
        TextView title;
        TextView content;
    }
}
