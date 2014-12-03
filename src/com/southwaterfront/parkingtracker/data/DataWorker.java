package com.southwaterfront.parkingtracker.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.json.JsonObject;

import android.util.Log;

import com.southwaterfront.parkingtracker.data.DataManager.Session;
import com.southwaterfront.parkingtracker.jsonify.BlockFaceJsonBuilder;
import com.southwaterfront.parkingtracker.jsonify.Jsonify;
import com.southwaterfront.parkingtracker.jsonify.MasterDataJsonBuilder;

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
	
	private static final String ERROR_MASTER_CORRUPT = "The master data file with the upload data is corrupt";

	private final Session session;

	private final BlockingQueue<DataTask> tasks;

	public boolean running;

	public DataWorker(DataManager dataManager, Session sess, BlockingQueue<DataTask> tasks) {
		this.dataManager = dataManager;
		this.LOG_TAG = "DataWorker " + sess.SESSION_ID;
		this.session = sess;
		this.tasks = tasks;
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
						break;
					case UPLOAD_DATA:
						uploadData(task);
						break;
					default:
						break;
					}

					CallBack callBack = task.callBack;
					if (callBack != null) {
						Result result = task.getResult();
						callBack.call(result);
					}
				}
			}
		}
		Log.i(LOG_TAG, "Worker for " + session.SESSION_ID + " interrupted and finished");
	}

	private void storeFace(DataTask task) {
		BlockFace face = (BlockFace) task.obj;

		this.session.blockFaces.add(face);

		JsonObject obj = BlockFaceJsonBuilder.buildObjectFromBlockFace(face);

		String fileName = this.dataManager.createBlockFaceFileName(face);
		File file = new File(this.session.cacheFolder, fileName);

		Task perTask = this.dataManager.writeToFile(obj, file);
		Result perResult = perTask.waitOnResult();

		task.setResult(perResult, perTask.getErrorMessage());

		Log.i(LOG_TAG, "Data task result: " + perResult + " Saving block face " + obj);

	}

	private void uploadData(DataTask task) {
		Session sess = (Session) task.obj;

		Log.i(LOG_TAG, "Attempting to upload data for session " + sess.SESSION_ID);

		File cacheDir = sess.cacheFolder;

		if (!cacheDir.isDirectory())
			throw new IllegalStateException("How is the cache not a directory?");

		File masterDataFile = new File(cacheDir, DataManager.MASTER_DATA_FILE_NAME);
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

		// TODO insert server upload task

		task.setResult(Result.SUCCESS, null);
	}
	
	private JsonObject createMasterObjectFromFiles(DataTask task, File cacheDir, File masterDataFile) {
		List<JsonObject> objs = buildFileJsonObjects(cacheDir);
		if (objs == null) {
			task.setResult(Result.FAIL, ERROR_FILES);
			return null;
		}
		
		JsonObject masterObject = MasterDataJsonBuilder.buildObjectFromBlockFaceObjects(objs);
		
		Task saveTask = dataManager.writeToFile(masterObject, masterDataFile);
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
				dataManager.deleteFile(f);
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

}