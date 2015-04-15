package com.southwaterfront.parkingtracker.jsonify;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.southwaterfront.parkingtracker.data.BlockFace;
import com.southwaterfront.parkingtracker.data.ParkingStall;

/**
 * Parser for BlockFace saved files
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class BlockFaceParser {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(Jsonify.DATA_TIME_FORMAT);

	public static BlockFace parse(File f) {
		if (f == null || !f.exists() || f.length() == 0L) 
			throw new IllegalArgumentException("File cannot be null, nonexistent or empty");

		FileInputStream in;
		JsonObject obj;
		int block;
		String face;
		JsonArray arr;
		try {
			in = new FileInputStream(f);
			obj = Jsonify.createJsonObjectFromStream(in);
			block = obj.getInt(Jsonify.BLOCK_ID);
			face = obj.getString(Jsonify.FACE_ID);
			arr = obj.getJsonArray(Jsonify.STALLS_ARRAY_ID);
		} catch (Exception e) {
			return null;
		}

		BlockFace b = BlockFace.emptyPaddedBlockFace(block, face, arr.size());
		for (int i = 0; i < arr.size(); i++) {
			JsonValue elem = arr.get(i);
			if (elem instanceof JsonObject) {
				JsonObject o = (JsonObject) elem;
				String plate = o.getString(Jsonify.PLATE_ID, null);
				String time = o.getString(Jsonify.TIME_ID, null);
				String attr = o.getString(Jsonify.ATTR_ID, null);
				String[] attrs = null;
				if (plate == null || time == null)
					continue;
				Date d;
				try {
					d = dateFormat.parse(time);
				} catch (ParseException e) {
					continue;
				}
				if (attr != null)
					attrs = attr.split(Jsonify.STRING_DELIMITER_REGEX);
				b.setStall(new ParkingStall(plate, d, attrs), i);
			}
		}

		b.resetModifiedFlag();
		return b;
	}

}
