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
 * The class is to be used to build the final {#link {@link JsonObject}
 * containing all of the parking data collected throughout a single session. 
 * The order of operations is first to create a {#link {@link MasterDataJsonBuilder},
 * then add a block face by calling {@link #startBlockFace(String, String)}. After creating a 
 * block face, each call to {@link #addStall(String, Date, String[])} adds a stall to the 
 * current block face. In so, the process is meant to be iterative through each stall per block.
 * It is also possible to build the master JsonObject from JsonObjects containing a single block 
 * faces using {@link #addBlockFace(JsonObject)}.
 * <br>
 * The iterative process can be tedious, if the data is already organized into {@link BlockFace} objects,
 * simply calling {@link MasterDataJsonBuilder#buildObjectFromBlockFaces(List)} can be used. Or the object
 * can be built by building individual block face objects using {@link #addBlockFace(BlockFace)}.
 * <br>
 * When all of the data is collected, {@link #buildObject()} is called to build the collective
 * JsonObject. The object can then be gotten as a {@link JsonObject} or a series of bytes. It is
 * important to note this is a build once class, the built object is not mutable. Therefore once
 * {@link #buildObject()} is called, the object can no longer be mutated and calling methods to add
 * data will not do anything and neither will rebuilding.
 * <br>
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class MasterDataJsonBuilder {
	
	@SuppressWarnings("unused")
	private static final String LOG_TAG = "MasterDataJsonBuilder";

	private static final JsonBuilderFactory 	builderFactory = Json.createBuilderFactory(null);
	private static final JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory(null);
	
	private final JsonObjectBuilder objectBuilder;
	private final JsonArrayBuilder blockFaceArrayBuilder;
	private JsonObjectBuilder currentBlockFaceBuilder;
	private JsonArrayBuilder currentStallsArrayBuilder;
	private JsonObjectBuilder currentStallBuilder;
	private JsonObject object;

	private SimpleDateFormat dateFormat;

	public MasterDataJsonBuilder() {
		this.objectBuilder = builderFactory.createObjectBuilder();
		this.blockFaceArrayBuilder = builderFactory.createArrayBuilder();
		this.currentStallBuilder = null;
		this.currentBlockFaceBuilder = null;
		this.object = null;

		this.dateFormat = new SimpleDateFormat(Jsonify.DATA_TIME_FORMAT);
	}
	
	/**
	 * Convenience method to build JsonObject from a list of {@link BlockFace} 
	 * objects. If the whole data already exists as a group of block faces, this
	 * is an easy way to get a JsonObject of all of the data.
	 * 
	 * @param elems List of {@link BlockFace} objects to jsonify
	 * @return JsonObject containing all block faces
	 */
	public static JsonObject buildObjectFromBlockFaces(List<BlockFace> elems) {
		if (elems == null) 
			throw new IllegalArgumentException("List of block faces cannot be null");
		
		JsonObject object = null;
		
		MasterDataJsonBuilder builder = new MasterDataJsonBuilder();
		
		for (BlockFace face : elems)
			builder.addBlockFace(face);
		
		object = builder.buildObject();
		
		return object;
	}

	/**
	 * Creates a block face in the final JsonObject. This is meant
	 * to be called once for a block face and then {@link #addStall(String, Date, String[])}
	 * for each stall before adding the next block face. Adding the same block
	 * face twice has unspecified results.
	 * <br>
	 * If the JsonObject is already built no work will be done.
	 * 
	 * @param block Name of block
	 * @param face Name of face
	 */
	public void startBlockFace(String block, String face) {
		if (this.object != null)
			return;
		if (block == null || face == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		if (this.currentBlockFaceBuilder != null) {
			this.currentBlockFaceBuilder.add(Jsonify.STALLS_ARRAY_ID, this.currentStallsArrayBuilder);
			this.blockFaceArrayBuilder.add(this.currentBlockFaceBuilder);
		}
		
		this.currentBlockFaceBuilder = builderFactory.createObjectBuilder();
		this.currentStallsArrayBuilder = builderFactory.createArrayBuilder();
		this.currentBlockFaceBuilder.add(Jsonify.BLOCK_ID, block);
		this.currentBlockFaceBuilder.add(Jsonify.FACE_ID, face);
	}
	
	/**
	 * This is a way to add a block face to the object by simply passing
	 * the JsonObject to the current build. Using this method completes
	 * the previous block that was being built and adds the new one. There 
	 * is no ability to add stalls to this block face so before {@link #addStall(String, Date, String[])}
	 * is called again, {@link #startBlockFace(String, String)} is required again.
	 * <br>
	 * There is no check for syntax correctness in the given JsonObject, that
	 * responsibility is to be handled by the user.
	 * 
	 * @param blockFace JsonObject containing a single block face
	 */
	public void addBlockFace(JsonObject blockFace) {
		if (this.object != null)
			return;
		if (blockFace == null)
			throw new IllegalArgumentException("Block face cannot be null");
		
		if (this.currentBlockFaceBuilder != null) {
			this.currentBlockFaceBuilder.add(Jsonify.STALLS_ARRAY_ID, this.currentStallsArrayBuilder);
			this.blockFaceArrayBuilder.add(this.currentBlockFaceBuilder);
		}
		
		this.blockFaceArrayBuilder.add(blockFace);
		
		this.currentBlockFaceBuilder = null;
		this.currentStallsArrayBuilder = null;
	}
	
	/**
	 * This is a way to add a block face to the object from a
	 * {@link BlockFace} object to the current build. Using this method completes
	 * the previous block that was being built and adds the new one. There 
	 * is no ability to add stalls to this block face so before {@link #addStall(String, Date, String[])}
	 * is called again, {@link #startBlockFace(String, String)} is required again.
	 * 
	 * @param blockFace Block face to add to JsonObject
	 */
	public void addBlockFace(BlockFace blockFace) {
		if (this.object != null)
			return;
		if (blockFace == null)
			throw new IllegalArgumentException("Block face cannot be null");
		
		startBlockFace(blockFace.block, blockFace.face);
		
		for (ParkingStall stall : blockFace.getParkingStalls())
			addStall(stall.plate, stall.dTStamp, stall.attr);
		
		this.currentBlockFaceBuilder.add(Jsonify.STALLS_ARRAY_ID, this.currentStallsArrayBuilder);
		this.blockFaceArrayBuilder.add(this.currentBlockFaceBuilder);
		
		this.currentBlockFaceBuilder = null;
		this.currentStallsArrayBuilder = null;
	}
	
	/**
	 * Adds a stall to the current block face in the JsonObject. If
	 * a block face has not been created, an {@link IllegalStateException}
	 * will be thrown.
	 * <br>
	 * If the JsonObject is already built no work will be done.
	 * 
	 * @param stall Parking stall to add
	 */
	public void addStall(ParkingStall stall) {
		if (this.object != null)
			return;
		if (stall == null)
			throw new IllegalArgumentException("Stall cannot be null");
		
		addStall(stall.plate, stall.dTStamp, stall.attr);
	}

	/**
	 * Adds a stall to the current block face in the JsonObject. If
	 * a block face has not been created, an {@link IllegalStateException}
	 * will be thrown.
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
		if (currentStallsArrayBuilder == null)
			throw new IllegalStateException("Cannot add a stall without a block face");

		this.currentStallBuilder = builderFactory.createObjectBuilder();
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
		this.currentStallsArrayBuilder.add(this.currentStallBuilder);
	}

	/**
	 * Finalizes the current JsonObject, builds it and 
	 * returns it. This method has no bearing of completeness, it will simply
	 * build all of the user supplied data into a JsonObject.
	 * <br>
	 * If the JsonObject is already built the previously built object will be returned.
	 * 
	 * @return Completed JsonObject with all of the parking data
	 */
	public JsonObject buildObject() {
		if (this.object != null)
			return this.object;
		
		if (this.currentBlockFaceBuilder != null) {
			this.currentBlockFaceBuilder.add(Jsonify.STALLS_ARRAY_ID, this.currentStallsArrayBuilder);
			this.blockFaceArrayBuilder.add(this.currentBlockFaceBuilder);
		}
		this.objectBuilder.add(Jsonify.BLOCKFACES_ARRAY_ID, blockFaceArrayBuilder);
		
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
		
		JsonGenerator generator = MasterDataJsonBuilder.generatorFactory.createGenerator(out);
		
		generator.write(this.object);
		generator.flush();
		generator.close();
	}
	
}
