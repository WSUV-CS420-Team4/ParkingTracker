package com.southwaterfront.parkingtracker.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import javax.json.JsonObject;

import android.util.Log;

import com.southwaterfront.parkingtracker.client.HttpClient;
import com.southwaterfront.parkingtracker.client.HttpClient.RequestFailedException;
import com.southwaterfront.parkingtracker.data.DataManager.Session;
import com.southwaterfront.parkingtracker.jsonify.BlockFaceJsonBuilder;
import com.southwaterfront.parkingtracker.jsonify.Jsonify;
import com.southwaterfront.parkingtracker.jsonify.MasterDataJsonBuilder;
import com.southwaterfront.parkingtracker.util.AsyncTask;
import com.southwaterfront.parkingtracker.util.Result;
import com.southwaterfront.parkingtracker.util.Utils;

/**
 * Internally used data worker to process the data on a separate thread
 * 
 * @author Vitaliy Gavrilov
 *
 */
class DataWorker implements Runnable {

	private final DataManager dataManager;

	private String LOG_TAG;

	private static final String ERROR_FILES = "There is no collectable data to upload";

	private static final String ERROR_SESSION_LOCKED = "Unable to complete the task because the session is locked and is in a read-only state";

	private static final String ERROR_MASTER_CORRUPT = "The master data file with the upload data is corrupt";

	private static final String ERROR_POST = "Failed posting data to server";

	private final Session session;

	private final BlockingQueue<DataTask> tasks;

	private boolean running;

	private Random rand;

	public DataWorker(DataManager dataManager, Session sess, BlockingQueue<DataTask> tasks) {
		this.dataManager = dataManager;
		this.LOG_TAG = "DataWorker " + sess.SESSION_ID;
		this.session = sess;
		this.tasks = tasks;
		this.rand = new Random();
		running = true;
	}

	@Override
	public void run() {
		while (running || tasks.size() != 0) {
			DataTask task = null;
			try {
				task = tasks.take();
			} catch (InterruptedException e) {
				running = false;
				continue;
			} finally {
				if (task != null) {
					switch (task.type) {
					case STORE_FACE:
						storeFace(task);
						Log.i(LOG_TAG, "Completed store face task for session " + this.session);
						break;
					case UPLOAD_DATA:
						uploadData(task);
						Log.i(LOG_TAG, "Completed upload task with result " + task.getResult() + " on session " + task.obj);
						break;
					default:
						break;
					}
					System.gc(); // Break all the rules
					CallBack callBack = task.callBack;
					if (callBack != null) {
						callBack.call(task);
					}
				}
			}
		}
		Log.i(LOG_TAG, "Worker for " + session.SESSION_ID + " interrupted and finished");
	}

	private void storeFace(DataTask task) {
		BlockFace face = (BlockFace) task.obj;

		if (this.session.isLocked()) {
			task.setResult(Result.FAIL, ERROR_SESSION_LOCKED);
			return;
		}

		//this.session.blockFaces.add(face);

		JsonObject obj = BlockFaceJsonBuilder.buildObjectFromBlockFace(face);

		String fileName = this.dataManager.createBlockFaceFileName(face);
		String useName = fileName;

		AsyncTask perTask = null;
		Result perResult = Result.FAIL;
		for (int i = 0; i < 3 && perResult == Result.FAIL; i++) {
			File file = new File(this.session.cacheFolder, useName);
			perTask = Utils.asyncFileWrite(obj, file);
			perResult = perTask.waitOnResult();
			int nonce = rand.nextInt();
			useName = fileName + nonce;
		} 
		
		task.setResult(perResult, perTask.getErrorMessage());
	}

	private void uploadData(DataTask task) {
		Session sess = (Session) task.obj;

		Log.i(LOG_TAG, "Attempting to upload data for session " + sess.SESSION_ID);

		File cacheDir = sess.cacheFolder;

		if (!cacheDir.isDirectory())
			throw new IllegalStateException("How is the cache not a directory?");

		File masterDataFile = sess.masterDataFile;
		JsonObject masterObject;
		if (masterDataFile.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(masterDataFile);
				masterObject = Jsonify.createJsonObjectFromStream(in);
			} catch (Exception e) {
				task.setResult(Result.FAIL, ERROR_MASTER_CORRUPT);
				return;
			} finally {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		} else {
			masterObject = createMasterObjectFromFiles(task, cacheDir, masterDataFile);
			if (task.getResult() == Result.FAIL)
				return;
		}

		try {
			HttpClient.sendPostRequest(masterObject);
		} catch (RequestFailedException e) {
			Log.e(LOG_TAG, "Failed to post data", e);
			task.setResult(Result.FAIL, ERROR_POST);
			return;
		}

		dataManager.removeSession(sess);

		task.setResult(Result.SUCCESS, null);
	}

	private JsonObject createMasterObjectFromFiles(DataTask task, File cacheDir, File masterDataFile) {
		List<JsonObject> objs = buildFileJsonObjects(cacheDir);
		if (objs == null) {
			task.setResult(Result.FAIL, ERROR_FILES);
			return null;
		}

		JsonObject masterObject = MasterDataJsonBuilder.buildObjectFromBlockFaceObjects(objs);

		AsyncTask saveTask = Utils.asyncFileWrite(masterObject, masterDataFile);
		Result saveResult = saveTask.waitOnResult();
		if (saveResult == Result.SUCCESS)
			deleteDirFiles(cacheDir);

		return masterObject;
	}

	/**
	 * Except {@link DataManager#MASTER_DATA_FILE_NAME}
	 * 
	 * @param dir Directory
	 */
	private void deleteDirFiles(File dir) {
		for (File f : dir.listFiles()) {
			if (!f.getName().equals(DataManager.MASTER_DATA_FILE_NAME))
				Utils.asyncFileDelete(f);
		}
	}

	private List<JsonObject> buildFileJsonObjects(File cacheDir) {
		LinkedList<JsonObject> jsonObjs = new LinkedList<JsonObject>();
		File[] files = cacheDir.listFiles();
		if (files.length == 0)
			return null;

		for (File f : files) {
			if (f.getName().equals(DataManager.MASTER_DATA_FILE_NAME))
				continue;

			FileInputStream in = null;
			try {
				in = new FileInputStream(f);
				JsonObject obj = Jsonify.createJsonObjectFromStream(in);
				jsonObjs.add(obj);
			} catch (Exception e) {
				Log.e(LOG_TAG, "Unable to create JsonObject from file", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		if (jsonObjs.isEmpty())
			return null;
		return jsonObjs;
	}

	public void induceStop() {
		this.running = false;
	}

}