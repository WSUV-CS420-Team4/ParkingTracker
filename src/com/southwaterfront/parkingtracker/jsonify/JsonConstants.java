package com.southwaterfront.parkingtracker.jsonify;

import java.text.SimpleDateFormat;

/**
 * Simply a class to hold constants need by other Json related classes
 * 
 * @author Vitaliy Gavrilov
 *
 */
public final class JsonConstants {

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
	
	/**
	 * This class cannot be instantiated
	 */
	private JsonConstants() {
		
	}
	
}
