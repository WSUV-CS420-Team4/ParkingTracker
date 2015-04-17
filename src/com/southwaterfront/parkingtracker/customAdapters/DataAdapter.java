package com.southwaterfront.parkingtracker.customAdapters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.data.BlockFace;


/**
 * Created by Joel on 4/3/2015.
 */
public class DataAdapter extends ArrayAdapter<BlockFace> {

	Context context;
	int layoutResourceId;
	public ArrayList<ParkingData> data;

	public DataAdapter(Context context, int layoutResourceId, BlockFace[] blockFaceArray) {
		super(context, layoutResourceId, blockFaceArray);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		//this.data = data;

		data = new ArrayList<ParkingData>();

		for (BlockFace face : blockFaceArray) {
			for (int i = 0; i < face.getParkingStalls().size(); i++) {
				if ( face.getParkingStalls().get(i).plate.equals("") ||  face.getParkingStalls().get(i).plate == null ) {
					// Do nothing...
				} else {

					data.add( new ParkingData(face.block, face.face, i, face.getParkingStalls().get(i).plate, face.getParkingStalls().get(i).attr) );
					//LogUtils..i("DataAdapter Added", "block: " + face.block + " face: " + face.face + " stall: " + i +
					//        " plate: " + face.getParkingStalls().get(i).plate + " attr: " + face.getParkingStalls().get(i).attr);
					//LogUtils.i("DataAdapter Size", "" + temp.size());
				}
			}
		}

		//data = new ParkingData[temp.size()];
		//temp.toArray( data );
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
			holder.flags = (TextView)row.findViewById(R.id.textViewDataLayoutFlags);

			row.setTag(holder);
		}
		else
		{
			holder = (DataHolder)row.getTag();
		}

		//LogUtils.i("getView position", "" + position);
		ParkingData parkingData = data.get(position);

		String title = "Block: " + parkingData.block + " Face: ";
		if ( parkingData.face != null ) { title += parkingData.face; } else { title += "-"; }
		title += " Stall: " + (parkingData.stall + 1);
		holder.title.setText(title);

		String content = "License: ";
		if ( parkingData.plate != null) { content += parkingData.plate; } else { content += "-"; }

		String flags = "";
		if ( parkingData.attr != null) {
			for (String s : parkingData.attr)
				flags += s + "\n" ;
		}

		if ( flags.endsWith("\n") ) {
			StringBuilder temp = new StringBuilder(flags);
			temp.setCharAt((flags.length()-1), ' ');
			flags = String.valueOf(temp);
		}

		holder.content.setText(content);
		holder.flags.setText(flags);

		return row;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	public void remove(int i) {
		data.remove(i);
	}

	public static class ParkingData {

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
			if (attr != null) { 
				this.attr = new String[attr.length];
				System.arraycopy(attr, 0, this.attr, 0, attr.length);
			} else {
				this.attr = null;
			}
		}

		int block;
		String face;
		int stall;
		boolean modifiedSince;

		String plate;
		Date dTStamp;
		String[] attr;

		public int getBlock() {
			return block;
		}

		public String getFace() {
			return face;
		}

		public int getStall() {
			return stall;
		}

		public String getPlate() {
			return plate;
		}

		public String[] getAttr() {
			return attr;
		}
	}

	static class DataHolder {
		TextView title;
		TextView content;
		TextView flags;
	}
}
