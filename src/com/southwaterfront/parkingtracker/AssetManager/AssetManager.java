package com.southwaterfront.parkingtracker.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.southwaterfront.parkingtracker.util.Utils;

/**
 * This is a play on the Android AssetManager, however, this
 * class will deal directly with all assets necessary for this app.
 * That is, all assets internal and external will be abstracted through
 * this manager.
 * 
 * This is a singleton class, only one instance will exist throughout the 
 * vm and is accessible by first calling {@link AssetManager#init(Context)} 
 * one time and then {@link AssetManager#getInstance()} to get the instance.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class AssetManager {

	private static AssetManager instance;
	
	private final Activity mainActivity;
	
	private final String LOG_TAG = "AssetManager";
	
	public static final int MAX_CACHE_SIZE_BYTES = 41943040;

	private final Context mainContext;

	private final android.content.res.AssetManager androidAssetManager;

	private final File cacheDir;

	private final String englishDirectory = "eng";

	private final String tessDataFolder = "tessdata";

	private final File internalFileDir;

	private final String externalStorageDirFileName = "ParkingTracker";

	private final File externalFileDir;

	private String englishLanguageDataDirectory;
	
	private final File imageCacheDir;
	
	private final String IMAGE_CACHE_DIR_NAME = "imageCache";
	
	private final String CACHE_DIR_NAME = "swfPt";
	
	/**
	 * Make constructor private to disallow outside instantiation
	 * of this class
	 */
	private AssetManager() {
		throw new IllegalArgumentException("Cannot use empty constructor");
	}
	
	/**
	 * Private constructor called when {@link #init(Activity)} is called.
	 * This sets all of the necessary fields in the singleton.
	 * 
	 * @param main
	 */
	private AssetManager(Activity main) {
		this.mainActivity = main;
		this.mainContext = this.mainActivity.getApplicationContext();
		this.androidAssetManager = this.mainContext.getAssets();
		this.cacheDir = new File(this.mainContext.getCacheDir(), this.CACHE_DIR_NAME);
		if (!this.cacheDir.exists())
			this.cacheDir.mkdir();
		this.internalFileDir = this.mainContext.getFilesDir();
		this.externalFileDir = new File(Environment.getExternalStorageDirectory(), externalStorageDirFileName);
		if (!this.externalFileDir.exists())
			this.externalFileDir.mkdir();
		this.englishLanguageDataDirectory = null;
		this.imageCacheDir = new File(this.cacheDir, this.IMAGE_CACHE_DIR_NAME);
		if (!this.imageCacheDir.exists())
			this.imageCacheDir.mkdir();
	}
	
	public File getInternalFileDir() {
		return this.internalFileDir;
	}
	
	public File getCacheDir() {
		return this.cacheDir;
	}
	
	public File getExternalFileDir() {
		return this.externalFileDir;
	}
	
	/**
	 * Initializes the AssetManager with the {@link Activity}
	 * that is the main activity of the app
	 * 
	 * @param main
	 */
	public static void init(Activity main) {
		if (main == null)
			throw new IllegalArgumentException("The main context cannot be null");
		if (instance == null)
			instance = new AssetManager(main);
	}
	
	/**
	 * Getter method for the singleton instance of the {@link AssetManager}
	 * 
	 * @return The AssetManager
	 */
	public static AssetManager getInstance() {
		if (instance == null)
			throw new IllegalStateException("The AssetManager has not yet been initialized");
		return AssetManager.instance;
	}

	/**
	 * This method is used to do sanity checks on all data upon the initialization
	 * of this app
	 */
	public void assetSanityCheck() {
		tessdataSanity();
	}

	private void tessdataSanity() {
		File engFolder = new File(internalFileDir, englishDirectory);
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

	private void moveTessdatatoInternalStorage(File tessdata) {
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

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		int read;
		while((read = in.read(buffer)) != -1)
			out.write(buffer, 0, read);
		out.flush();
	}

	/**
	 * Getter for directory needed for OCR engine
	 * 
	 * @return The directory of the english language data
	 */
	public String getEnglishLanguageDataDir() {
		if (englishLanguageDataDirectory == null)
			throw new IllegalStateException("The directory has not been initialized");
		return englishLanguageDataDirectory;
	}
	
	/**
	 * Getter for main activity context
	 * 
	 * @return The context
	 */
	public Context getMainContext() {
		return this.mainContext;
	}

	/**
	 * Get the directory for the image cache
	 * @return Directory
	 */
	public File getImageCacheDir() {
		return this.imageCacheDir;
	}
	
	/**
	 * Clears the image cache dir
	 */
	public void clearImageCache() {
		for (File f : this.imageCacheDir.listFiles())
			Utils.asyncFileDelete(f);
	}
	
	/**
	 * Getter for main activity of app instance
	 * 
	 * @return Main activity
	 */
	public Activity getMainActivity() {
		return this.mainActivity;
	}
	
	/**
	 * Getter for main resources
	 * 
	 * @return The resources
	 */
	public Resources getAppResources() {
		return this.mainContext.getResources();
	}
	
}
