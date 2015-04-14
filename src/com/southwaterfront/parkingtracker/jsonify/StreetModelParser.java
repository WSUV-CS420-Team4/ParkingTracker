package com.southwaterfront.parkingtracker.jsonify;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.southwaterfront.parkingtracker.data.BlockFaceDefinition;
import com.southwaterfront.parkingtracker.util.LogUtils;

/***********
 * 
 * Receives and parses JSON map information from the server
 * 
 * @author Ryan "Bob" Dean
 *
 */

public class StreetModelParser {

	private static String LOG_TAG = "StreetModelParser";

	public static List<BlockFaceDefinition> parse(JsonObject obj){

		ArrayList<BlockFaceDefinition> result = new ArrayList<BlockFaceDefinition>();

		JsonArray BlockFaces = obj.getJsonArray(Jsonify.BLOCKFACES_ARRAY_ID);
		if (BlockFaces == null) {
			return null;
		}

		for (JsonValue v : BlockFaces) {
			if (v instanceof JsonObject) {
				JsonObject vO = (JsonObject) v;
				try{
					int block = vO.getInt(Jsonify.BLOCK_ID);
					String face = vO.getString(Jsonify.FACE_ID);
					int numStalls = vO.getInt(Jsonify.NUM_STALLS_ID);			
					result.add(new BlockFaceDefinition(block, face, numStalls));
				} catch(Exception e){
					LogUtils.e(LOG_TAG, "Parsing json object failed: " + obj, e);
				}
			}
		}

		return result.isEmpty() ? null : result;
	}

}


