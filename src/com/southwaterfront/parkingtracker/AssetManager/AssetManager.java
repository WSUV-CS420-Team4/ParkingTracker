package com.southwaterfront.parkingtracker.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.southwaterfront.parkingtracker.Main;

/**
 * This is a play on the Android AssetManager, however, this
 * class will deal directly with all assets necessary for this app.
 * That is, all assets internal and external will be abstracted through
 * this manager.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class AssetManager {

	public static final int MAX_CACHE_SIZE_BYTES = 41943040;
	
	private static final String LOG_TAG = "AssetManager";

	private static final Context mainContext = Main.getMainContext();

	private static final android.content.res.AssetManager androidAssetManager = mainContext.getAssets();

	public static final File CACHE_DIR = mainContext.getCacheDir();

	private static final String englishDirectory = "eng";

	private static final String tessDataFolder = "tessdata";

	public static final File INTERNAL_FILE_DIR = mainContext.getFilesDir();

	private static final String EXTERNAL_STORAGE_DIR_NAME = "ParkingTracker";

	public static final File EXTERNAL_FILE_DIR = new File(Environment.getExternalStorageDirectory(), EXTERNAL_STORAGE_DIR_NAME);

	private static String englishLanguageDataDirectory = null;

	static {
		if (!EXTERNAL_FILE_DIR.exists())
			EXTERNAL_FILE_DIR.mkdir();
	}

	/**
	 * This method is used to do sanity checks on all data upon the initialization
	 * of this app
	 */
	public static void assetSanityCheck() {
		tessdataSanity();
	}

	private static void tessdataSanity() {
		File engFolder = new File(INTERNAL_FILE_DIR, englishDirectory);
		if (!engFolder.exists()) {
			Log.i(LOG_TAG, "English language definition folder does not exist");
			engFolder.mkdir();
		}
		File tessdata = new File(engFolder, tessDataFolder);
		if (!tessdata.exists()) {
			Log.i(LOG_TAG, "Tessdata folder does not exist");
			tessdata.mkdir();
			moveTessdatatoInternalStorage(tessdata);
		}
		englishLanguageDataDirectory = engFolder.getAbsolutePath();
		Log.i(LOG_TAG, "Directory of tessdata directory for TessBaseAPI is " + englishLanguageDataDirectory);
	}

	private static void moveTessdatatoInternalStorage(File tessdata) {
		String[] files = null;
		try {
			files = androidAssetManager.list(tessDataFolder);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Failed to get asset file list.", e);
		}
		for(String filename : files) {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = androidAssetManager.open(tessDataFolder + File.separator + filename);
				File outFile = new File(tessdata, filename);
				if (!outFile.exists())
					outFile.createNewFile();
				out = new FileOutputStream(outFile);
				copyFile(in, out);
				in.close();
				out.close();
			} catch(IOException e) {
				Log.e(LOG_TAG, "Failed to copy asset file: " + filename, e);
			}       
		}      
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		int read;
		while((read = in.read(buffer)) != -1)
			out.write(buffer, 0, read);
		out.flush();
	}

	public static String getEnglishLanguageDataDir() {
		if (englishLanguageDataDirectory == null)
			throw new UnsupportedOperationException("The directory has not been initialized");
		return englishLanguageDataDirectory;
	}

}
