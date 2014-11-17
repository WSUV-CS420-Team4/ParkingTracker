package com.southwaterfront.parkingtracker.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;

/**
 * This singleton will be used to manage the collected data. This
 * class should have the ability to recognize sessions, cache data when necessary,
 * persist when necessary. 
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class DataManager {
	
	private static final DataManager instance = new DataManager();
	
	private final AssetManager assetManager;
	
	private final File cacheDir;
	
	private final List<Session> sessions;
	
	/**
	 * Container class to hold session information. A session is defined as
	 * one complete round of picture taking.
	 * 
	 * @author Vitaliy Gavrilov
	 *
	 */
	private class Session {
		public final String SESSION_ID;
		public final File 	cacheFolder;
		public final Date 	startTime;
		
		public Session(String sID, File cacheFolder) {
			if (sID == null || cacheFolder == null)
				throw new IllegalArgumentException("Arguments cannot be null");
			
			this.SESSION_ID = sID;
			this.cacheFolder = cacheFolder;
			this.startTime = new Date(System.currentTimeMillis());
		}
	}
	
	/**
	 * Getter for instance of DataManager
	 * 
	 * @return Instance of DataManager
	 */
	public static DataManager getInstance() {
		return DataManager.instance;
	}
	
	/**
	 * Disallow instantiation
	 */
	private DataManager() {
		this.assetManager = AssetManager.getInstance();
		this.cacheDir = assetManager.getCacheDir();
		this.sessions = new ArrayList<Session>();
	}
	
	
}
