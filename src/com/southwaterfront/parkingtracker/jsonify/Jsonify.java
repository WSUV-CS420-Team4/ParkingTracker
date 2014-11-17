package com.southwaterfront.parkingtracker.jsonify;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

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
	
	private static final JsonReaderFactory readerFactory = Json.createReaderFactory(null);
	private static final JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory(null);
	
	/**
	 * This class cannot be instantiated
	 */
	private Jsonify() {
		
	}
	
	/**
	 * Creates a JsonObject from the InputStream. An internal error
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
		
		JsonReader reader = readerFactory.createReader(in);
		
		JsonObject result = reader.readObject();
		
		reader.close();
		
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
	public static void writeJsonObjecttoStream(JsonObject object, OutputStream out) {
		if (object == null || out == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		JsonGenerator generator = generatorFactory.createGenerator(out);
		
		generator.write(object);
		generator.flush();
		generator.close();
	}
	
}
