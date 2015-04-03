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
                temp.add( new ParkingData(face.block, face.face, i, face.getParkingStalls().get(i).plate, face.getParkingStalls().get(i).attr) );
                Log.i("stall", "block: " + face.block + " face: " + face.face + " stall: " + i +
                        " plate: " + face.getParkingStalls().get(i).plate + " attr: " + face.getParkingStalls().get(i).attr);
            }
        }

        data = new ParkingData[temp.size()];
        temp.toArray( data );
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DataHolder holder = null;

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

    static class ParkingData {

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
