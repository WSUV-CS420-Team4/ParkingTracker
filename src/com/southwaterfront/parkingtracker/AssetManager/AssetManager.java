package com.southwaterfront.parkingtracker.AssetManager;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.json.JsonObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.southwaterfront.parkingtracker.client.HttpClient;
import com.southwaterfront.parkingtracker.client.HttpClient.RequestFailedException;
import com.southwaterfront.parkingtracker.data.BlockFace;
import com.southwaterfront.parkingtracker.jsonify.BlockParser;
import com.southwaterfront.parkingtracker.jsonify.Jsonify;
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

	private final File internalFileDir;

	private final String externalStorageDirFileName = "ParkingTracker";

	private final File externalFileDir;

	private final File imageCacheDir;
	
	private final File authToken;
	
	private final String authTokenFile = "authTokenFile";

	private final String IMAGE_CACHE_DIR_NAME = "imageCache";

	private final String CACHE_DIR_NAME = "swfPt";

	private final String alprRuntimeFolder = "runtime_data";

	private final String configFileName = "openalpr.conf";

	private final File alprConfigFile;

	private final String streetModelFileName = "streetModel.json";

	private final File streetModelJsonFile;
	
	private Set<BlockFace> streetModel;

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
		this.imageCacheDir = new File(this.cacheDir, this.IMAGE_CACHE_DIR_NAME);
		if (!this.imageCacheDir.exists())
			this.imageCacheDir.mkdir();
		this.alprConfigFile = new File(this.internalFileDir + File.separator + this.alprRuntimeFolder + File.separator + this.configFileName);
		this.authToken = new File(this.internalFileDir, this.authTokenFile);
		this.streetModelJsonFile = new File(this.internalFileDir, this.streetModelFileName);
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

	public File getAlprConfigFile() {
		return this.alprConfigFile;
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
		openAlprSanity();
	}

	private void openAlprSanity() {
		String runtimeDataDir = internalFileDir.getAbsolutePath() + File.separatorChar + this.alprRuntimeFolder;
		if (!new File(runtimeDataDir).exists())
			copyAlprAssetFolder(this.alprRuntimeFolder,
					runtimeDataDir);
		if (!this.streetModelJsonFile.exists())
			copyAsset(this.streetModelFileName, this.streetModelJsonFile.getAbsolutePath());
		
		/// try to download streetModel
		try {
			InputStream in = HttpClient.getStreetModel();
			JsonObject obj = IStoJSON(in);
			this.streetModel = BlockParser.parseBlock(obj);
			Utils.asyncFileDelete(streetModelJsonFile);
			Utils.asyncFileWrite(obj, streetModelJsonFile);		
			
		// failed to download, using backup	
		} catch (RequestFailedException e1) {
			FileInputStream in = null;
			
			try {
				in = new FileInputStream(this.streetModelJsonFile);
				JsonObject obj = IStoJSON(in);
				this.streetModel = BlockParser.parseBlock(obj);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("Street model file not found"); // Impossible ??
			}
		}	
		
		if (this.streetModel == null)
			// TODO: Do something drastic
			throw new IllegalStateException("Ded");
	}

	/**
	 * Getter for street model
	 * 
	 * @return The model as defined as a set of empty block faces
	 */
	public Set<BlockFace> getStreetModel() {
		return this.streetModel;
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		int read;
		while((read = in.read(buffer)) != -1)
			out.write(buffer, 0, read);
		out.flush();
	}
	
	/**
	 * Getter for authToken
	 * 
	 * 
	 * @return authToken
	 */
	public File getAuthToken() {
		return this.authToken;
	}
	
	/**
	 * Converts InputStream to JsonObject
	 * 
	 * 
	 * @return JsonObject
	 */
	
	private JsonObject IStoJSON(InputStream in){
		JsonObject obj = null;
		try{
			obj = Jsonify.createJsonObjectFromStream(in);
		} catch(Exception e){
			Log.e(LOG_TAG, "Parsing json object failed: " + in, e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		return obj;
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
	 * Getter for the street model as defined in JSON
	 * 
	 * @return File reference
	 */
	public File getSteetModelFile() {
		return this.streetModelJsonFile;
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

	private boolean copyAlprAssetFolder(String fromAssetPath, String toPath) {
		try {
			String[] files = androidAssetManager.list(fromAssetPath);
			new File(toPath).mkdirs();
			boolean res = true;
			for (String file : files)
				if (file.contains("."))
					res &= copyAsset(fromAssetPath + File.separator + file,
							toPath + File.separator + file);
				else
					res &= copyAlprAssetFolder(fromAssetPath + File.separator + file,
							toPath + File.separator + file);
			return res;
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return false;
		}
	}

	private boolean copyAsset(String fromAssetPath, String toPath) {
		InputStream in = null;
		OutputStream out = null;
		try {
			File f = new File(toPath);
			if (!f.exists())
				f.createNewFile();
			else
				return true;
			in = androidAssetManager.open(fromAssetPath);
			out = new FileOutputStream(toPath);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			return true;
		} catch(Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return false;
		}
	}

}
