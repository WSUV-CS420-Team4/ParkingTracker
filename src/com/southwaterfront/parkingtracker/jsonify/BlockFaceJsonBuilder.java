package com.southwaterfront.parkingtracker.jsonify;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * This class is a builder for one {@link JsonObject} holding parking data
 * for one block face. An object is instantiated with a block name and face,
 * and adding stalls with {@link #addStall(String, Date, String[])} adds it to this block face.
 * <br>
 * Just as {@link MasterDataJsonBuilder}, this class is immutable. Once the final JsonObject
 * is built, no changes can be made.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class BlockFaceJsonBuilder {

	private static final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(null);;
	
	private final JsonObjectBuilder objectBuilder;
	private JsonArrayBuilder stallsArrayBuilder;
	private JsonObjectBuilder currentStallBuilder;
	private JsonObject object;

	private SimpleDateFormat dateFormat;

	public BlockFaceJsonBuilder(String block, String face) {
		this.objectBuilder = jsonFactory.createObjectBuilder();
		this.stallsArrayBuilder = jsonFactory.createArrayBuilder();
		this.currentStallBuilder = null;
		this.object = null;

		this.dateFormat = new SimpleDateFormat(JsonConstants.DATA_TIME_FORMAT);
		
		this.objectBuilder.add(JsonConstants.BLOCK_ID, block);
		this.objectBuilder.add(JsonConstants.FACE_ID, face);
	}
	
	/**
	 * Adds a stall to this block face in the JsonObject.
	 * <br>
	 * If the JsonObject is already built no work will be done.
	 * 
	 * @param plate Plate number
	 * @param time Date/time stamp
	 * @param attr Array of attributes for current stall, possibly null
	 */
	public void addStall(String plate, Date time, String[] attr) {
		if (this.object != null)
			return;
		if (plate == null || time == null)
			throw new IllegalArgumentException("Both plate and time are required");

		this.currentStallBuilder = jsonFactory.createObjectBuilder();
		this.currentStallBuilder.add(JsonConstants.PLATE_ID, plate);

		String dtStamp = this.dateFormat.format(time);
		this.currentStallBuilder.add(JsonConstants.TIME_ID, dtStamp);

		if (attr != null) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < attr.length; i++) {
				builder.append(attr[i]);
				if (i < attr.length - 1)
					builder.append(JsonConstants.STRING_DELIMITER);
			}
			this.currentStallBuilder.add(JsonConstants.ATTR_ID, builder.toString());
		} else {
			this.currentStallBuilder.addNull(JsonConstants.ATTR_ID);
		}
		
		this.stallsArrayBuilder.add(this.currentStallBuilder);
	}
	
	/**
	 * Finalizes the current JsonObject, builds it and 
	 * returns it. This method has no bearing of completeness, it will simply
	 * build all of the user supplied data into a JsonObject.
	 * <br>
	 * If the JsonObject is already built the previously built object will be returned.
	 * 
	 * @return Completed JsonObject of the block face
	 */
	public JsonObject buildObject() {
		if (this.object != null)
			return this.object;
		
		this.objectBuilder.add(JsonConstants.STALLS_ARRAY_ID, this.stallsArrayBuilder);
		
		this.object = objectBuilder.build();
		return this.object;
	}

	/**
	 * Getter for finalized JsonObject, an {@link IllegalStateException} 
	 * is thrown if the data is not finalized with {@link #buildObject()}.
	 * 
	 * @return Completed JsonObject with the block face data
	 */
	public JsonObject getJsonObject() {
		if (this.object == null)
			throw new IllegalStateException("Object not created, call buildObject first");
		return this.object;
	}

	/**
	 * A convenience method to get the actual byte encoding of this
	 * JsonObject. The byte encoding will be that of {@value MasterDataJsonBuilder#BYTE_ENCODING}.
	 * An {@link IllegalStateException} is thrown if the data is not 
	 * finalized with {@link #buildObject()}.
	 * 
	 * @return Byte representation of the created JsonObject.
	 */
	public byte[] getJsonObjectBytes() {
		if (this.object == null)
			throw new IllegalStateException("Object not created, call buildObject first");
		byte[] bytes;

		try {
			bytes = this.object.toString().getBytes(JsonConstants.BYTE_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		return bytes;
	}
	
	/**
	 * Gives a boolean result on whether the JsonObject has been built
	 * 
	 * @return True is JsonObject is built, false otherwise
	 */
	public boolean isBuilt() {
		return this.object != null;
	}
	
}
