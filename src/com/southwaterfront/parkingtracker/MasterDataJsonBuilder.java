package com.southwaterfront.parkingtracker;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * The class is to be used to build the final {#link {@link JsonObject}
 * containing all of the parking data collected throughout a single session. 
 * The order of operations is first to create a {#link {@link MasterDataJsonBuilder},
 * then add a block face by calling {@link #addBlockFace(String, String)}. After creating a 
 * block face, each call to {@link #addStall(String, Date, String[])} adds a stall to the 
 * current block face. In so, the process is meant to be iterative through each stall per block.
 * <br>
 * When all of the data is collected, {@link #buildObject()} is called to build the collective
 * JsonObject. The object can then be gotten as a {@link JsonObject} or a series of bytes.
 * <br>
 * This class also holds the final key Strings used to create the JsonObject.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class MasterDataJsonBuilder {

	public static final char STRING_DELIMITER = '|';
	public static final String BLOCKFACES_ARRAY_ID = "blockfaces";
	public static final String BLOCK_ID = "block";
	public static final String FACE_ID = "face";
	public static final String STALLS_ARRAY_ID = "stalls";
	public static final String PLATE_ID = "plate";
	public static final String TIME_ID = "time";
	public static final String ATTR_ID = "attr";

	/**
	 * Date time format string for {@link SimpleDateFormat}
	 */
	public static final String DATA_TIME_FORMAT = "EEE, dd MMM yyy HH:mm:ss Z";

	public static final String BYTE_ENCODING = "UTF-8";

	private final JsonBuilderFactory jsonFactory;
	private final JsonObjectBuilder objectBuilder;
	private final JsonArrayBuilder blockFaceArrayBuilder;
	private JsonObjectBuilder currentBlockFaceBuilder;
	private JsonArrayBuilder currentStallsArrayBuilder;
	private JsonObjectBuilder currentStallBuilder;
	private JsonObject object;

	private SimpleDateFormat dateFormat;

	public MasterDataJsonBuilder() {
		this.jsonFactory = Json.createBuilderFactory(null);
		this.objectBuilder = jsonFactory.createObjectBuilder();
		this.blockFaceArrayBuilder = jsonFactory.createArrayBuilder();
		this.currentStallBuilder = null;
		this.currentBlockFaceBuilder = null;
		this.object = null;

		this.dateFormat = new SimpleDateFormat(DATA_TIME_FORMAT);
	}

	/**
	 * Creates a block face in the final JsonObject. This is meant
	 * to be called once for a block face and then {@link #addStall(String, Date, String[])}
	 * for each stall before adding the next block face. Adding the same block
	 * face twice has unspecified results.
	 * 
	 * @param block Name of block
	 * @param face Name of face
	 */
	public void addBlockFace(String block, String face) {
		if (block == null || face == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		if (this.currentBlockFaceBuilder != null) {
			this.currentBlockFaceBuilder.add(STALLS_ARRAY_ID, this.currentStallsArrayBuilder);
			this.blockFaceArrayBuilder.add(this.currentBlockFaceBuilder);
		}
		this.currentBlockFaceBuilder = jsonFactory.createObjectBuilder();
		this.currentStallsArrayBuilder = jsonFactory.createArrayBuilder();
		this.currentBlockFaceBuilder.add(BLOCK_ID, block);
		this.currentBlockFaceBuilder.add(FACE_ID, face);
	}

	/**
	 * Adds a stall to the current block face in the JsonObject. If
	 * a block face has not been created, an {@link IllegalStateException}
	 * will be thrown.
	 * 
	 * @param plate Plate number
	 * @param time Date/time stamp
	 * @param attr Array of attributes for current stall, possibly null
	 */
	public void addStall(String plate, Date time, String[] attr) {
		if (plate == null || time == null)
			throw new IllegalArgumentException("Both plate and time are required");
		if (currentStallsArrayBuilder == null)
			throw new IllegalStateException("Cannot add a stall without a block face");

		this.currentStallBuilder = jsonFactory.createObjectBuilder();
		this.currentStallBuilder.add(PLATE_ID, plate);

		String dtStamp = this.dateFormat.format(time);
		this.currentStallBuilder.add(TIME_ID, dtStamp);

		if (attr != null) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < attr.length; i++) {
				builder.append(attr[i]);
				if (i < attr.length - 1)
					builder.append(STRING_DELIMITER);
			}
			this.currentStallBuilder.add(ATTR_ID, builder.toString());
		} else {
			this.currentStallBuilder.addNull(ATTR_ID);
		}
		this.currentStallsArrayBuilder.add(this.currentStallBuilder);
	}

	/**
	 * Finalizes the current JsonObject, builds it and 
	 * returns it. This method has no bearing of completeness, it will simply
	 * build all of the user supplied data into a JsonObject.
	 * 
	 * @return Completed JsonObject with all of the parking data
	 */
	public JsonObject buildObject() {
		if (this.currentBlockFaceBuilder != null) {
			this.currentBlockFaceBuilder.add(STALLS_ARRAY_ID, this.currentStallsArrayBuilder);
			this.blockFaceArrayBuilder.add(this.currentBlockFaceBuilder);
		}
		this.objectBuilder.add(BLOCKFACES_ARRAY_ID, blockFaceArrayBuilder);
		
		this.object = objectBuilder.build();
		return this.object;
	}

	/**
	 * Getter for finalized JsonObject, an {@link IllegalStateException} 
	 * is thrown if the data is not finalized with {@link #buildObject()}.
	 * 
	 * @return Completed JsonObject with all of the parking data
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
			bytes = this.object.toString().getBytes(BYTE_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		return bytes;
	}

}
