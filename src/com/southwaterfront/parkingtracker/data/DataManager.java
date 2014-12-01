package com.southwaterfront.parkingtracker.data;

import java.io.File;
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

import javax.json.JsonObject;

import android.util.Log;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.jsonify.BlockFaceJsonBuilder;
import com.southwaterfront.parkingtracker.persist.PersistenceWorker;
import com.southwaterfront.parkingtracker.persist.PersistentTask;
import com.southwaterfront.parkingtracker.persist.PersistentTask.Result;
import com.southwaterfront.parkingtracker.persist.PersistentTask.Task;

/**
 * This singleton will be used to manage the collected data. This
 * class should have the ability to recognize sessions, cache data when necessary,
 * persist when necessary. 
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class DataManager {

	private static final String LOG_TAG = "DataManager";

	private final String DATA_CACHE_DIR_NAME = "Parking_Data";

	private final String FILE_NAME_FORMAT = "yyyy-MM-dd HH:mm";

	private static final DataManager instance = new DataManager();

	private static final char FILE_NAME_DELIMITER = ' ';

	private final AssetManager assetManager;

	private final File dataCacheDir;

	private final SortedSet<Session> sessions;

	private final SimpleDateFormat dateFormat;

	private Session currentSession;

	private final BlockingQueue<PersistentTask> persistenceTasks;

	private final Thread persistenceThread;

	/**
	 * Update both every time current session changes
	 */
	private BlockingQueue<BlockFace> dataTasks;

	private Thread dataThread;

	/**
	 * Container class to hold session information. A session is defined as
	 * one complete round of picture taking.
	 * 
	 * @author Vitaliy Gavrilov
	 *
	 */
	public class Session implements Comparable<Session> {
		private static final String LOG_TAG = "Session";

		public final String SESSION_ID;
		public final Date		createTime;
		public final File 	cacheFolder;
		public final Date 	loadTime;

		public final Set<BlockFace> blockFaces;

		public Session(Date createTime, File cacheFolder) {
			if (createTime == null || cacheFolder == null)
				throw new IllegalArgumentException("Arguments cannot be null");

			this.createTime = createTime;
			this.SESSION_ID = dateFormat.format(this.createTime);
			this.cacheFolder = cacheFolder;
			this.loadTime = new Date(System.currentTimeMillis());

			this.blockFaces = new HashSet<BlockFace>();
		}

		public Session(String createTime, File cacheFolder) {
			if (createTime == null || cacheFolder == null)
				throw new IllegalArgumentException("Arguments cannot be null");

			this.SESSION_ID = createTime;
			try {
				this.createTime = dateFormat.parse(createTime);
			} catch (ParseException e) {
				Log.i(LOG_TAG, "Parsing data string failed, this is not a valid session");
				throw new RuntimeException("Session could not be created from string " + createTime);
			}
			this.cacheFolder = cacheFolder;
			this.loadTime = new Date(System.currentTimeMillis());

			this.blockFaces = new HashSet<BlockFace>();
		}

		@Override
		public int compareTo(Session another) {
			return this.createTime.compareTo(another.createTime);
		}

	}

	/**
	 * Internally used data worker to process the data on a separate thread
	 * 
	 * @author Vitaliy Gavrilov
	 *
	 */
	private class DataWorker implements Runnable {
		private String LOG_TAG;

		private final Session session;

		private final BlockingQueue<BlockFace> tasks;

		public boolean running;

		public DataWorker(Session sess, BlockingQueue<BlockFace> tasks) {
			this.LOG_TAG = "DataWorker " + sess.SESSION_ID;
			this.session = sess;
			this.tasks = tasks;
			running = true;
		}

		@Override
		public void run() {
			while (running || tasks.size() != 0) {
				BlockFace face = null;
				try {
					face = tasks.take();
				} catch (InterruptedException e) {
					running = false;
					continue;
				} finally {
					if (face != null) {
						Log.i(LOG_TAG, "Adding block face " + face.block + " " + face.face + " to session " + session.SESSION_ID);
						
						this.session.blockFaces.add(face);

						JsonObject obj = BlockFaceJsonBuilder.buildObjectFromBlockFace(face);

						String fileName = createBlockFaceFileName(face);

						File file = new File(this.session.cacheFolder, fileName);

						PersistentTask task = writeToFile(obj, file);
						
						Result result = task.waitOnResult();
						
						if (result == Result.FAIL) {
							String text = "Writing file " + fileName + " to internal cache failed with error message: " + task.getErrorMessage();
							
							Log.i(LOG_TAG, text);
						}
					}
				}
				Log.i(LOG_TAG, "Worker for " + session.SESSION_ID + " interrupted and finished");
			}

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
		File cacheDir = assetManager.getCacheDir();
		this.dataCacheDir = new File(cacheDir, DATA_CACHE_DIR_NAME);
		if (!this.dataCacheDir.exists())
			this.dataCacheDir.mkdir();
		this.sessions = new TreeSet<Session>();
		this.dateFormat = new SimpleDateFormat(FILE_NAME_FORMAT);

		this.persistenceTasks = new LinkedBlockingQueue<PersistentTask>();
		PersistenceWorker worker = new PersistenceWorker(this.persistenceTasks);
		this.persistenceThread = new Thread(worker);
		this.persistenceThread.setDaemon(true);
		this.persistenceThread.start();


		loadSessions();

		if (this.sessions.isEmpty()) {
			startNewSession();
		} else {
			this.currentSession = this.sessions.first();
		}

		// Set after current session has been set
		setNewDataWorker();
	}

	private void loadSessions() {
		for (File f : this.dataCacheDir.listFiles()) {
			if (f.isDirectory()) {
				if (f.isFile() || f.list().length == 0) {
					Log.i(LOG_TAG, "Found file " + f.getName() + " in data cache folder that has no data, deleting");
					deleteFile(f);
				} else {
					Session sess;
					try {
						sess = new Session(f.getName(), f);
					} catch (Exception e) {
						Log.i(LOG_TAG, "Attempted to create session from invalid folder, how did this get here?");
						continue;
					}
					this.sessions.add(sess);
					Log.i(LOG_TAG, "Adding session " + sess.SESSION_ID + " to available sessions");
				}
			}

		}
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
		Date now = new Date(System.currentTimeMillis());
		String dirName = this.dateFormat.format(now);
		File dir = new File(this.dataCacheDir, dirName);

		if (dir.exists()) {
			Log.e(LOG_TAG, "Trying to create a new session failed because a session with a start date within a minute already exists");
			throw new RuntimeException("Attemp to create a new session has failed");
		}

		dir.mkdir();

		Log.i(LOG_TAG, "Successfully created session " + dirName);

		Session newSess = new Session(now, dir);

		this.sessions.add(newSess);

		this.currentSession = newSess;

		setNewDataWorker();
	}

	private void setNewDataWorker() {
		if (this.dataThread != null)
			this.dataThread.interrupt();

		this.dataTasks = new LinkedBlockingQueue<BlockFace>();
		DataWorker dataWorker = new DataWorker(this.currentSession, this.dataTasks);
		this.dataThread = new Thread(dataWorker);
		this.dataThread.setDaemon(true);
		this.dataThread.start();
	}

	/**
	 * Getter for current session time stamp
	 * 
	 * @return Date object
	 */
	public Date getSessionStartTime() {
		return this.currentSession.createTime;
	}

	/**
	 * Getter for current session name. Similar to time but
	 * it is a formatted string of {@link #getSessionStartTime()}
	 * 
	 * @return Session ID
	 */
	public String getSessionName() {
		return this.currentSession.SESSION_ID;
	}

	/**
	 * Getter for set of sessions available, though
	 * not necessarily in memory.
	 * 
	 * @return Set of sessions
	 */
	public SortedSet<Session> getAvailableSessions() {
		return Collections.unmodifiableSortedSet(this.sessions);
	}

	/**
	 * Removes sessions which are not the current session
	 */
	public void removeNonCurrentSessions() {
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
		File cacheDir = session.cacheFolder;
		Log.i(LOG_TAG, "Removing cache folder " + cacheDir + " created at " + session.SESSION_ID);

		deleteFile(cacheDir);
		return this.sessions.remove(session);
	}

	private PersistentTask deleteFile(File file) {
		PersistentTask task = new PersistentTask(null, file, Task.DELETE);
		this.persistenceTasks.add(task);
		return task;
	}

	private PersistentTask writeToFile(Object data, File file) {
		PersistentTask task = new PersistentTask(data, file, Task.WRITE);
		this.persistenceTasks.add(task);
		return task;
	}

	/**
	 * This call stores the block face. It deals with all necessary persistence,
	 * session adherence, and encapsulation. The data processing is sent to a worker
	 * thread so it is safe to call this on the UI thread.
	 * 
	 * 
	 * @param face BlockFace to add to data
	 */
	public void addBlockFace(BlockFace face) {
		if (face == null)
			return;

		this.dataTasks.add(face);
	}

	private String createBlockFaceFileName(BlockFace face) {
		return face.block + FILE_NAME_DELIMITER + face.face;
	}

}
