package com.southwaterfront.parkingtracker.jsonify;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

/**
 * Simply a class to hold constants need by other Json related classes and to
 * provide generic {@link JsonObject} processing.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public final class Jsonify {
	
	private static final String LOG_TAG = "Jsonify";

	public static final char STRING_DELIMITER = '|';
	public static final String BLOCKFACES_ARRAY_ID = "blockfaces";
	public static final String BLOCK_ID = "Block";
	public static final String FACE_ID = "Face";
	public static final String STALLS_ARRAY_ID = "Stalls";
	public static final String PLATE_ID = "Plate";
	public static final String TIME_ID = "Time";
	public static final String ATTR_ID = "Attr";
	public static final String NUM_STALLS_ID = "numStalls";
	public static final String DATE_ID = "epochTime";
	
	public static final String RESULTS_ARRAY_ID = "results";
	public static final String CANDIDATE_ARRAY_ID = "candidates";
	//public static final String PLATE_ID = "plate";

	/**
	 * Date time format string for {@link SimpleDateFormat}
	 */
	public static final String DATA_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static final String BYTE_ENCODING = "UTF-8";
	
	private static final JsonReaderFactory readerFactory = Json.createReaderFactory(null);
	private static final JsonWriterFactory writerFactory = Json.createWriterFactory(null);
	
	/**
	 * This class cannot be instantiated
	 */
	private Jsonify() {
		
	}
	
	/**
	 * Creates a JsonObject from the InputStream. An internal exception
	 * will be thrown if it does not parse to a JsonObject.
	 * <br>
	 * RFC 4627 compliant
	 * 
	 * @param in InputStream of JsonObject
	 * @return Constructed JsonObject
	 */
	public static JsonObject createJsonObjectFromStream(InputStream in) {
		if (in == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		JsonReader reader = null;
		
		synchronized (readerFactory) {
			reader = readerFactory.createReader(in);
		}
		
		JsonObject result = reader.readObject();
		
		return result;
	}
	
	/**
	 * Writes the JsonObject to the output stream.
	 * <br>
	 * RFC 4627 compliant
	 * 
	 * @param object JsonObject to send to stream
	 * @param out Stream to write to
	 */
	public static void writeJsonObjectToStream(JsonObject object, OutputStream out) {
		if (object == null || out == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		JsonWriter writer = null;
		
		synchronized (writerFactory) {
			writer = writerFactory.createWriter(out);
		}
		
		writer.write(object);
		writer.close();
	}
	
}
