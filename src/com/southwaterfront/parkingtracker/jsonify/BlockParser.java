package com.southwaterfront.parkingtracker.jsonify;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import android.util.Log;

import com.southwaterfront.parkingtracker.data.BlockFace;

/***********
 * 
 * Receives and parses JSON map information from the server
 * 
 * @author Ryan "Bob" Dean
 *
 */

public class BlockParser {

	private static String LOG_TAG = "BlockParser";

	public static Set<BlockFace> parseBlock(InputStream message){
		JsonObject obj;
		try{
			obj = Jsonify.createJsonObjectFromStream(message);
		} catch(Exception e){
			Log.e(LOG_TAG, "Parsing json object failed: " + message, e);
			return null;
		} finally {
			try {
				message.close();
			} catch (IOException e) {
			}
		}

		Set<BlockFace> result = new HashSet<BlockFace>();

		JsonArray BlockFaces = obj.getJsonArray(Jsonify.BLOCKFACES_ARRAY_ID);
		if (BlockFaces == null){
			return null;
		}


		for (JsonValue v : BlockFaces) {
			if (v instanceof JsonObject) {
				JsonObject vO = (JsonObject) v;
				try{
					int block = vO.getInt(Jsonify.BLOCK_ID);
					String face = vO.getString(Jsonify.FACE_ID);
					int numStalls = vO.getInt(Jsonify.NUM_STALLS_ID);			
					result.add(BlockFace.emptyPaddedBlockFace(String.valueOf(block), face, numStalls));
				} catch(Exception e){
					Log.e(LOG_TAG, "Parsing json object failed: " + message, e);
				}
			}
		}

		return result;
	}

}


