package com.southwaterfront.parkingtracker.persist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.json.JsonObject;

import com.southwaterfront.parkingtracker.jsonify.Jsonify;
import com.southwaterfront.parkingtracker.util.LogUtils;
import com.southwaterfront.parkingtracker.util.Utils;

/**
 * This class provides convenience methods for users 
 * to persist data to disk
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class Persistor {

	private static final String LOG_TAG = "Persistor";

	/**
	 * Writes a {@link JsonObject} to a {@link File}. The user is
	 * responsible for making sure the file exists and is writable.
	 * 
	 * @param f File to write to
	 * @param obj JsonObject to persist
	 */
	public static void writeToFile(File f, JsonObject obj) {
		writeJson(f, obj, false);
	}

	/**
	 * Appends a {@link JsonObject} to a {@link File}. The user is
	 * responsible for making sure the file exists and is writable.
	 * 
	 * @param f File to write to
	 * @param obj JsonObject to persist
	 */
	public static void appendToFile(File f, JsonObject obj) {
		writeJson(f, obj, true);
	}

	/**
	 * Writes bytes to a {@link File}. The user is
	 * responsible for making sure the file exists and is writable.
	 * 
	 * @param f File to write to
	 * @param bytes Byte sequence to persist
	 */
	public static void writeToFile(File f, byte[] bytes) {
		writeBytes(f, bytes, 0, bytes.length, false);
	}

	/**
	 * Appends bytes to a {@link File}. The user is
	 * responsible for making sure the file exists and is writable.
	 * 
	 * @param f File to write to
	 * @param bytes Byte sequence to persist
	 */
	public static void appendToFile(File f, byte[] bytes) {
		writeBytes(f, bytes, 0, bytes.length, true);
	}

	private static void writeJson(File f, JsonObject obj, boolean append) {
		if (f == null || obj == null)
			throw new IllegalArgumentException("Arguments cannot be null");

		if (!f.exists() || !f.isFile() || !f.canWrite())
			throw new IllegalStateException("File must exist and be writable");

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f, append);
		} catch (FileNotFoundException e) {
			// Impossible!!!
		}

		Jsonify.writeJsonObjectToStream(obj, out);
		
		if (Utils.isCacheFile(f))
			Utils.updateCacheSize(f.length());
		LogUtils.i(LOG_TAG, "Successfully wrote a JsonObject to " + f.getAbsolutePath());

		try {
			out.close();
		} catch (IOException e) {
			LogUtils.e(LOG_TAG, "OutputStream could not close", e);
		}
	}

	private static void writeBytes(File f, byte[] bytes, int start, int end, boolean append) {
		if (f == null || bytes == null)
			throw new IllegalArgumentException("Arguments cannot be null");

		if (!f.exists() || !f.isFile() || !f.canWrite())
			throw new IllegalStateException("File must exist and be writable");

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f, append);
		} catch (FileNotFoundException e) {
			// Impossible!!!
		}

		try {
			out.write(bytes, start, end);
			out.flush();
			out.close();

			if (Utils.isCacheFile(f))
				Utils.updateCacheSize(f.length());

			LogUtils.i(LOG_TAG, "Successfully wrote " + bytes.length + " bytes to " + f.getAbsolutePath());
		} catch (IOException e) {
			LogUtils.e(LOG_TAG, "Write of file to " + f.getAbsolutePath() + " failed", e);
		}
	}

}
