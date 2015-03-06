package com.southwaterfront.parkingtracker.jsonify;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import com.southwaterfront.parkingtracker.data.BlockFace;
import com.southwaterfront.parkingtracker.data.ParkingStall;

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
	
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "BlockFaceJsonBuilder";

	private static final JsonBuilderFactory 	jsonFactory = Json.createBuilderFactory(null);
	private static final JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory(null);
	
	private final JsonObjectBuilder objectBuilder;
	private 			JsonArrayBuilder 	stallsArrayBuilder;
	private 			JsonObjectBuilder currentStallBuilder;
	private 			JsonObject 				object;

	private SimpleDateFormat dateFormat;

	public BlockFaceJsonBuilder(int block, String face) {
		this.objectBuilder = jsonFactory.createObjectBuilder();
		this.stallsArrayBuilder = jsonFactory.createArrayBuilder();
		this.currentStallBuilder = null;
		this.object = null;

		this.dateFormat = new SimpleDateFormat(Jsonify.DATA_TIME_FORMAT);
		
		this.objectBuilder.add(Jsonify.BLOCK_ID, block);
		this.objectBuilder.add(Jsonify.FACE_ID, face);
	}
	
	/**
	 * Builds a JsonObject from a {@link BlockFace}
	 * 
	 * @param blockFace Block face to use
	 * @return Built JsonObject
	 */
	public static JsonObject buildObjectFromBlockFace(BlockFace blockFace) {
		if (blockFace == null)
			throw new IllegalArgumentException("Block face cannot be null");
		
		JsonObject object = null;
		
		BlockFaceJsonBuilder builder = new BlockFaceJsonBuilder(blockFace.block, blockFace.face);
		
		builder.addStalls(blockFace.getParkingStalls());
		
		object = builder.buildObject();
		
		return object;
	}
	
	/**
	 * Add a list of stalls
	 * 
	 * @param stalls List of stalls
	 */
	public void addStalls(List<ParkingStall> stalls) {
		if (stalls == null)
			throw new IllegalArgumentException("Stalls cannot be null");
		
		for (ParkingStall stall : stalls)
			addStall(stall.plate, stall.dTStamp, stall.attr);
	}
	
	/**
	 * Add a stall to this block face
	 * 
	 * @param stall Parking stall to add, non null
	 */
	public void addStall(ParkingStall stall) {
		if (stall == null)
			throw new IllegalArgumentException("Stall cannot be null");
		
		addStall(stall.plate, stall.dTStamp, stall.attr);
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
		
		if (plate.equals(ParkingStall.EmptyStall.plate)) {
			this.currentStallBuilder = jsonFactory.createObjectBuilder();
			this.stallsArrayBuilder.add(this.currentStallBuilder);
			return;
		}

		this.currentStallBuilder = jsonFactory.createObjectBuilder();
		this.currentStallBuilder.add(Jsonify.PLATE_ID, plate);

		String dtStamp = this.dateFormat.format(time);
		this.currentStallBuilder.add(Jsonify.TIME_ID, dtStamp);

		if (attr != null) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < attr.length; i++) {
				builder.append(attr[i]);
				if (i < attr.length - 1)
					builder.append(Jsonify.STRING_DELIMITER);
			}
			this.currentStallBuilder.add(Jsonify.ATTR_ID, builder.toString());
		} else {
			this.currentStallBuilder.addNull(Jsonify.ATTR_ID);
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
		
		this.objectBuilder.add(Jsonify.STALLS_ARRAY_ID, this.stallsArrayBuilder);
		
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
	 * <br>
	 * RFC 4627 compliant
	 * 
	 * @return Byte representation of the created JsonObject.
	 */
	public byte[] getJsonObjectBytes() {
		if (this.object == null)
			throw new IllegalStateException("Object not created, call buildObject first");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(512);
		writeToStream(out);
		
		byte[] bytes = out.toByteArray();

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
	
	/**
	 * Writes the object byte representation to the given output stream.
	 * If the object has not been built yet, {@link IllegalStateException} is
	 * throw.
	 * 
	 * @param out Output stream to write to
	 */
	public void writeToStream(OutputStream out) {
		if (this.object == null)
			throw new IllegalStateException("Cannot write object to stream because JsonObject has not been built");
		
		JsonGenerator generator = BlockFaceJsonBuilder.generatorFactory.createGenerator(out);
		
		generator.write(this.object);
		generator.flush();
		generator.close();
	}
	
}
