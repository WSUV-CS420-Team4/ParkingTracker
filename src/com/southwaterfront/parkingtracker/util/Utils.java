package com.southwaterfront.parkingtracker.util;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.json.JsonObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.persist.PersistenceTask;
import com.southwaterfront.parkingtracker.persist.PersistenceTask.Tasks;
import com.southwaterfront.parkingtracker.persist.PersistenceWorker;

/**
 * Generic utils that can be used by any class
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class Utils {
	
	public static final String LOG_TAG = "Utils";
	
	private Utils() {}
	
	private static final BlockingQueue<PersistenceTask> persistenceTasks;

	private static final Thread persistenceThread;
	
	private static long cacheSize = 0;
	
	private static final String cacheDirName;
	
	private static final File cacheDir;
	
	private static final ConnectivityManager connManager;
	
	private static final NetworkInfo wifi;
	
	static {
		persistenceTasks = new LinkedBlockingQueue<PersistenceTask>();
		PersistenceWorker worker = new PersistenceWorker(persistenceTasks);
		persistenceThread = new Thread(worker);
		//persistenceThread.setDaemon(true);
		persistenceThread.setName("Persistence Thread");
		persistenceThread.start();
		
		AssetManager assets = AssetManager.getInstance();
		cacheDir = assets.getCacheDir();
		cacheDirName = cacheDir.getAbsolutePath();
		
		cacheSize = getFileSize(cacheDir);
		Log.i(LOG_TAG, "Initial cache directory size is " + cacheSize);
		
		connManager = (ConnectivityManager) assets.getMainContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	}
	
	/**
	 * Determines the length of file in bytes. If the {@link File}
	 * is a file, its size if returned. If it is a directory the it is
	 * the sum of the file sizes in the directory. If the file does not exist
	 * it will return 0.
	 * 
	 * @param f File to get size of
	 * @return Size of file in bytes
	 */
	public static long getFileSize(File f) {
		if (!f.exists()) return 0;
		if (f.isFile()) return f.length();
		long size = 0;
		for (File fi : f.listFiles())
			size += getFileSize(fi);
		return size;
	}
	
	/**
	 * Asynchronous file deletion
	 * 
	 * @param file File to delete
	 * @return The task being worked on, see {@link AsyncTask} for
	 * more information on how to check the result
	 */
	public static AsyncTask asyncFileDelete(File file) {
		PersistenceTask task = new PersistenceTask(null, file, Tasks.DELETE);
		Utils.persistenceTasks.add(task);
		return task;
	}

	/**
	 * Asynchronous file write
	 * 
	 * @param data Data to write, byte[] and {@link JsonObject} supported
	 * @param file File to write to
	 * @return The task being worked on, see {@link AsyncTask} for
	 * more information on how to check the result
	 */
	public static AsyncTask asyncFileWrite(Object data, File file) {
		PersistenceTask task = new PersistenceTask(data, file, Tasks.WRITE);
		Utils.persistenceTasks.add(task);
		return task;
	}
	
	/**
	 * Update the cache size
	 * 
	 * @param diff Change in bytes of cache size
	 * @return Updated cache size
	 */
	public static synchronized long updateCacheSize(long diff) {
		cacheSize += diff;
		return cacheSize;
	}
	
	/**
	 * Getter for cache size
	 * 
	 * @return Cache size
	 */
	public static long getCacheSize() {
		return Utils.cacheSize;
	}
	
	/**
	 * Convenience method to check if the given file 
	 * is in the cache folder. It may in a subdirectory, but
	 * it is in regardless of how deep it is in the cache directory
	 * 
	 * @param f File in question
	 * @return True if in, false otherwise
	 */
	public static boolean isCacheFile(File f) {
		if (!f.exists()) return false;
		return f.getAbsolutePath().startsWith(cacheDirName);
	}
	
	/**
	 * Convenience method for checking if the phone's
	 * internet connection is on wifi
	 * 
	 * @return True if wifi connected, false if not
	 */
	public static boolean isWifiConnected() {
		return wifi.isConnected();
	}
}
