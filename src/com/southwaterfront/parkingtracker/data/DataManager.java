package com.southwaterfront.parkingtracker.data;

import java.io.Closeable;
import java.io.File;
import java.lang.Thread.State;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.util.AsyncTask;
import com.southwaterfront.parkingtracker.util.Result;
import com.southwaterfront.parkingtracker.util.Utils;

/**
 * This singleton will be used to manage the collected data. This
 * class should have the ability to recognize sessions, cache data when necessary,
 * persist when necessary. 
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class DataManager implements Closeable {

	private static final String LOG_TAG = "DataManager";

	public static final String MASTER_DATA_FILE_NAME = "master_data";

	private final String DATA_CACHE_DIR_NAME = "Parking_Data";

	private final String FILE_NAME_FORMAT = "yyyy-MM-dd HH:mm";

	private static final DataManager instance = new DataManager();

	private boolean closed;

	private static final char FILE_NAME_DELIMITER = ' ';

	private static final String ERROR_CLOSED = "The data manager is closed and cannot be operated on";

	private final AssetManager assetManager;

	private final File dataCacheDir;

	private final SortedSet<Session> sessions;

	private final SimpleDateFormat dateFormat;

	private Session currentSession;

	/**
	 * Update both every time current session changes
	 */
	private BlockingQueue<DataTask> dataTasks;

	private DataWorker dataWorker;

	private Thread dataThread;

	/**
	 * Container class to hold session information. A session is defined as
	 * one complete round of picture taking.
	 * 
	 * @author Vitaliy Gavrilov
	 *
	 */
	public class Session implements Comparable<Session> {
		private final String LOG_TAG;

		public final String SESSION_ID;
		public final Date		createTime;
		public final File 	cacheFolder;
		public final Date 	loadTime;
		public final File		masterDataFile;
		public final Set<BlockFace> blockFaces;

		public Session(Date createTime, File cacheFolder) {
			if (createTime == null || cacheFolder == null)
				throw new IllegalArgumentException("Arguments cannot be null");

			this.createTime = createTime;
			this.SESSION_ID = dateFormat.format(this.createTime);
			this.LOG_TAG = "Session " + this.SESSION_ID;
			this.cacheFolder = cacheFolder;
			this.loadTime = new Date(System.currentTimeMillis());

			this.blockFaces = new HashSet<BlockFace>();

			this.masterDataFile = new File(this.cacheFolder, DataManager.MASTER_DATA_FILE_NAME);
		}

		public Session(String createTime, File cacheFolder) {
			if (createTime == null || cacheFolder == null)
				throw new IllegalArgumentException("Arguments cannot be null");

			this.SESSION_ID = createTime;
			this.LOG_TAG = "Session " + this.SESSION_ID;
			try {
				this.createTime = dateFormat.parse(createTime);
			} catch (ParseException e) {
				Log.i(LOG_TAG, "Parsing data string failed, this is not a valid session");
				throw new RuntimeException("Session could not be created from string " + createTime);
			}
			this.cacheFolder = cacheFolder;
			this.loadTime = new Date(System.currentTimeMillis());

			this.blockFaces = new HashSet<BlockFace>();

			this.masterDataFile = new File(this.cacheFolder, DataManager.MASTER_DATA_FILE_NAME);
		}

		/**
		 * Gets the state of the session
		 * 
		 * @return True if session is locked to writing, false if open
		 */
		public boolean isLocked() {
			return this.masterDataFile.exists();
		}

		@Override
		public int compareTo(Session another) {
			return this.createTime.compareTo(another.createTime);
		}

		@Override
		public String toString() {
			return this.SESSION_ID;
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
		this.closed = false;
		File cacheDir = assetManager.getCacheDir();
		this.dataCacheDir = new File(cacheDir, DATA_CACHE_DIR_NAME);
		if (!this.dataCacheDir.exists())
			this.dataCacheDir.mkdir();
		this.sessions = new TreeSet<Session>();
		this.dateFormat = new SimpleDateFormat(FILE_NAME_FORMAT);
		
		loadSessions();

		if (this.sessions.isEmpty()) {
			startNewSession();
		} else {
			this.currentSession = this.sessions.first();
			if (this.currentSession.isLocked())
				startNewSession();
			else {
				setNewDataWorker();
				Log.i(LOG_TAG, "Using already existing session " + this.currentSession);
			}
		}
		Log.i(LOG_TAG, "The data manager initialized with session " + this.currentSession + " out of " + this.sessions.size() + " available sessions.");
	}

	private void loadSessions() {
		for (File f : this.dataCacheDir.listFiles()) {
			if (f.isFile()) {
				sessLoaderDelHelper(f);
			} else {
				File[] files = f.listFiles();
				if (files == null || files.length == 0) {
					sessLoaderDelHelper(f);
				} else if (files.length == 1 && files[0].length() == 0) {
					sessLoaderDelHelper(f);
				} else {
					Session sess;
					try {
						sess = new Session(f.getName(), f);
					} catch (Exception e) {
						Log.i(LOG_TAG, "Attempted to create session from invalid folder, how did this get here?");
						continue;
					}
					if (sess.masterDataFile.exists() && sess.masterDataFile.length() == 0)
						sessLoaderDelHelper(f);
					this.sessions.add(sess);
					Log.i(LOG_TAG, "Adding session " + sess.SESSION_ID + " to available sessions");
				}
			}
		}
	}

	private void sessLoaderDelHelper(File f) {
		Log.i(LOG_TAG, "Found file " + f.getName() + " in data cache folder that has no data, deleting");
		AsyncTask task = Utils.asyncFileDelete(f);
		task.waitOnResult();
	}

	/**
	 * Used to create a new session. This method creates a time stamp
	 * which is used to create a folder where all cached data will be held.
	 * <br>
	 * ************* Important **************
	 * <br>
	 * Calling this function twice within a minute will result in a {@link RuntimeException}
	 * because the folders are stamped on a per minute basis.
	 * 
	 */
	public void startNewSession() {
		checkNotClosed();

		Date now = new Date(System.currentTimeMillis());
		String dirName = this.dateFormat.format(now);
		File dir = new File(this.dataCacheDir, dirName);

		if (dir.exists()) {
			Log.e(LOG_TAG, "Trying to create a new session failed because a session with a start date within a minute already exists");
			throw new RuntimeException("Attemp to create a new session has failed");
		}

		dir.mkdir();

		Session newSess = new Session(now, dir);

		Log.i(LOG_TAG, "Successfully created new session " + newSess);

		this.sessions.add(newSess);

		this.currentSession = newSess;

		setNewDataWorker();
	}

	private void setNewDataWorker() {
		if (this.dataThread != null) {
			this.dataWorker.induceStop();
			if (this.dataThread != Thread.currentThread()) {
				State state = this.dataThread.getState();
				if (state == State.BLOCKED)
					this.dataThread.interrupt();
			}
		}

		this.dataTasks = new LinkedBlockingQueue<DataTask>();
		this.dataWorker = new DataWorker(this, this.currentSession, this.dataTasks);
		this.dataThread = new Thread(dataWorker);
		//this.dataThread.setDaemon(true);
		this.dataThread.setName("Data Worker for " + this.currentSession);
		this.dataThread.start();
	}

	public String createBlockFaceFileName(BlockFace face) {
		return face.block + FILE_NAME_DELIMITER + face.face;
	}

	/**
	 * Getter for current session time stamp
	 * 
	 * @return Date object
	 */
	public Date getSessionStartTime() {
		checkNotClosed();

		return this.currentSession.createTime;
	}

	/**
	 * Getter for current session name. Similar to time but
	 * it is a formatted string of {@link #getSessionStartTime()}
	 * 
	 * @return Session ID
	 */
	public String getSessionName() {
		checkNotClosed();

		return this.currentSession.SESSION_ID;
	}

	/**
	 * Getter for set of sessions available, though
	 * not necessarily in memory.
	 * 
	 * @return Set of sessions
	 */
	public SortedSet<Session> getAvailableSessions() {
		checkNotClosed();

		return Collections.unmodifiableSortedSet(this.sessions);
	}

	/**
	 * Removes sessions which are not the current session
	 */
	public void removeNonCurrentSessions() {
		checkNotClosed();

		List<Session> toRemove = new LinkedList<Session>();
		for (Session sess : this.sessions) {
			if (sess != this.currentSession)
				toRemove.add(sess);
		}

		for (Session sess : toRemove)
			removeSession(sess);
	}

	/**
	 * Removes a specific session
	 * 
	 * @param session Session to remove
	 * @return True if session was removed, false if not
	 */
	public boolean removeSession(Session session) {
		checkNotClosed();

		File cacheDir = session.cacheFolder;
		Log.i(LOG_TAG, "Removing cache folder " + cacheDir + " created at " + session.SESSION_ID);

		AsyncTask cacheDel = Utils.asyncFileDelete(cacheDir);

		if (session == this.currentSession) {
			Result cacheResult = cacheDel.waitOnResult();
			if (cacheResult == Result.SUCCESS)
				startNewSession();
			else
				return false;
		}

		return this.sessions.remove(session);
	}

	/**
	 * This call stores the block face. It deals with all necessary persistence,
	 * session adherence, and encapsulation. The data processing is sent to a worker
	 * thread so it is safe to call this on the UI thread.
	 * <br>
	 * Within a single {@link Session}, it is good practice to add save only one instance of
	 * a block face representing the same face. If multiple are saved, both will be sent to the server
	 * upon uploading and the server will resolve the data.
	 * 
	 * @param face BlockFace to add to data
	 * @param callBack A callback that can be used 
	 */
	public void saveBlockFace(BlockFace face, CallBack callBack) {
		checkNotClosed();

		if (face == null)
			return;

		DataTask task = new DataTask(face, callBack, DataTask.Tasks.STORE_FACE);

		this.dataTasks.add(task);
	}

	/**
	 * Uploads the current session data to the server
	 * 
	 * @param callBack A callback that will be called upon
	 * task completion
	 */
	public void uploadSessionData(CallBack callBack) {
		uploadSessionData(this.currentSession, callBack);
	}

	/**
	 * Uploads session data to the server. The session must be non null
	 * and from the available sessions.
	 * 
	 * @param sess Session to use
	 * @param callBack A callback that will be called upon
	 * task completion
	 */
	public void uploadSessionData(Session sess, CallBack callBack) {
		checkNotClosed();

		if (sess == null || !this.sessions.contains(sess))
			throw new IllegalArgumentException("The session cannot be null and must be a valid session from the manager");

		DataTask task = new DataTask(sess, callBack, DataTask.Tasks.UPLOAD_DATA);
		this.dataTasks.add(task);
	}

	private void checkNotClosed() {
		if (this.closed)
			throw new IllegalStateException(ERROR_CLOSED);
	}
	
	/**
	 * Method to check if there are available sessions to upload, determined
	 * by the existence of a session that has a block face saved.
	 * 
	 * @return True if uploaded session exists, false otherwise
	 */
	public boolean existsUploadableSessions() {
		for (Session s : this.sessions) {
			String[] files = s.cacheFolder.list();
			if (s.isLocked() || (files != null && files.length > 0))
				return true;
		}
		return false;
	}

	/**
	 * Closes the data manager, should only be called in
	 * Main activity onDestroy()
	 */
	@Override
	public void close() {
		if (!this.closed) {
			for (Session s : this.sessions) {
				File cacheFolder = s.cacheFolder;
				File[] files = cacheFolder.listFiles();
				if (files.length == 0) {
					Log.i(LOG_TAG, "Deleting empty session " + s);
					AsyncTask t = Utils.asyncFileDelete(cacheFolder);
					t.waitOnResult();
				}
				
				this.dataThread.interrupt();
				this.closed = true;
			}
		}
	}

}
