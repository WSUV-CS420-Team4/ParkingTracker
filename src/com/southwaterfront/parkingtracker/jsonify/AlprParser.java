package com.southwaterfront.parkingtracker.jsonify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.southwaterfront.parkingtracker.alpr.AlprEngine;

import android.util.Log;

/**
 * Helper to parse the JSON results from the Alpr engine
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class AlprParser {
	
	private static String LOG_TAG = "AlprParser";
	
	private static AlprEngine alpr = AlprEngine.getInstance();

	public static String[] parseAlprResult(String message) {
		ByteArrayInputStream in = new ByteArrayInputStream(message.getBytes());
		JsonObject obj;
		try {
			obj = Jsonify.createJsonObjectFromStream(in);
		} catch (Exception e) {
			Log.e(LOG_TAG, "Parsing json object failed: " + message, e);
			return null;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		ArrayList<String> gatherList = new ArrayList<String>(alpr.getNumberOfResults());
		JsonArray resultArray = obj.getJsonArray(Jsonify.RESULTS_ARRAY_ID);
		if (resultArray == null)
			return null;
		JsonObject resultObj = resultArray.getJsonObject(0);
		if (resultObj == null)
			return null;
		resultArray = resultObj.getJsonArray(Jsonify.CANDIDATE_ARRAY_ID);
		if (resultArray == null)
			return null;
		
		for (JsonValue v : resultArray) {
			if (v instanceof JsonObject) {
				JsonObject vO = (JsonObject) v;
				String plate = vO.getString(Jsonify.PLATE_ID);
				if (plate != null)
					gatherList.add(plate);
			}
		}
		String[] result = gatherList.toArray(new String[gatherList.size()]);
		return result;
	}

}
