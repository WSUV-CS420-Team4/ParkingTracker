package com.southwaterfront.parkingtracker.persist;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import javax.json.JsonObject;

import com.southwaterfront.parkingtracker.util.LogUtils;
import com.southwaterfront.parkingtracker.util.Result;
import com.southwaterfront.parkingtracker.util.Utils;

/**
 * This is a worker that deals with data on disk.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class PersistenceWorker implements Runnable {

	private static final String LOG_TAG = "PersistenceWorker";

	private static final String ERROR_CREATE_FILE = "The file could not be created for the write task";

	private static final String ERROR_WRITE_FILE = "The data could not be written to because access was denied";

	private static final String ERROR_WRITE_NOT_EMPTY = "The file was not written to because it is not empty";

	private static final String ERROR_WRITE_DIR = "The data cannot be written because the file is a directory";

	private static final String ERROR_DATA_TYPE = "The data could not be written because it was neither a byte array or JsonObject";

	private static final String ERROR_DELETE_FILE = "The file could not be deleted because access was denied";

	private static final String ERROR_DELETE_NONE = "The file could not be deleted because it did not exist";

	private static final String ERROR_DELETE_UNKNOWN = "The file could not be deleted because of an unknown error";

	private boolean running;

	private final BlockingQueue<PersistenceTask> tasks;

	public PersistenceWorker(BlockingQueue<PersistenceTask> tasks) {
		this.tasks = tasks;
		this.running = true;
	}

	@Override
	public void run() {
		while (running || !tasks.isEmpty()) {
			try {
				PersistenceTask taskWrapper = tasks.take();

				switch (taskWrapper.task) {
				case APPEND:
					writeData(taskWrapper, true);
					break;
				case DELETE:
					delete(taskWrapper);
					break;
				case WRITE:
					writeData(taskWrapper, false);
					break;
				default:
					break;

				}
				LogUtils.i(LOG_TAG, "Task " + taskWrapper.task + " on file " + taskWrapper.file.getAbsolutePath() +" completed with result " + taskWrapper.getResult() + "\t Error message: " + taskWrapper.getErrorMessage() + "\tThe updated cache size is " + Utils.getCacheSize() + " bytes");
			} catch (InterruptedException e) {
				break;
			}
		}
		LogUtils.i(LOG_TAG, "PersistanceWorker was interrupted and finished");
	}

	private void writeData(PersistenceTask taskWrapper, boolean append) {
		Object data = taskWrapper.data;
		File file = taskWrapper.file;

		if (data instanceof byte[]) {
			byte[] bytes = (byte[]) data;

			validateWriteFile(file, taskWrapper, append);

			if (taskWrapper.getResult() == Result.FAIL)
				return;

			try {
				if (append)
					Persistor.appendToFile(file, bytes);
				else
					Persistor.writeToFile(file, bytes);
			} catch (Exception e) {
				setTaskFailure(taskWrapper, e.getMessage());
			}
			setTaskSuccess(taskWrapper);
		} else if (data instanceof JsonObject) {
			JsonObject obj = (JsonObject) data;

			validateWriteFile(file, taskWrapper, append);

			if (taskWrapper.getResult() == Result.FAIL)
				return;

			try {
				if (append)
					Persistor.appendToFile(file, obj);
				else
					Persistor.writeToFile(file, obj);
			} catch (Exception e) {
				setTaskFailure(taskWrapper, e.getMessage());
			}
			setTaskSuccess(taskWrapper);
		} else {
			setTaskFailure(taskWrapper, ERROR_DATA_TYPE);
		}
	}

	private void delete(PersistenceTask taskWrapper) {
		File file = taskWrapper.file;

		validateDeleteFile(file, taskWrapper);

		if (taskWrapper.getResult() == Result.FAIL)
			return;

		if (file.isFile())
			deleteFile(file, taskWrapper);
		else if (file.isDirectory())
			deleteDirectory(file, taskWrapper);

		if (taskWrapper.getResult() == Result.FAIL)
			return;

		setTaskSuccess(taskWrapper);
	}

	private void deleteFile(File file, PersistenceTask t) {
		try {
			long size = file.length();
			boolean isFile = file.isFile();
			boolean success = file.delete();

			if (!success)
				setTaskFailure(t, ERROR_DELETE_UNKNOWN);
			else if (isFile && Utils.isCacheFile(file))
				Utils.updateCacheSize(-size);
		} catch (Exception e) {
			setTaskFailure(t, e.getMessage());
		} 

	}

	private void deleteDirectory(File dir, PersistenceTask t) {
		if (!dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				deleteDirectory(file, t);
			else if (file.isFile())
				deleteFile(file, t);

			if (t.getResult() == Result.FAIL)
				return;
		}

		deleteFile(dir, t);
	}

	private void validateDeleteFile(File file, PersistenceTask t) {
		if (!file.exists()) {
			setTaskFailure(t, ERROR_DELETE_NONE);
			return;
		}

		if (!file.canWrite())
			setTaskFailure(t, ERROR_DELETE_FILE);
	}

	private void validateWriteFile(File file, PersistenceTask t, boolean append) {
		if (file.isDirectory()) {
			LogUtils.i(LOG_TAG, "Could not write file " + file.getAbsolutePath());
			setTaskFailure(t, ERROR_WRITE_DIR);
			return;
		}

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				LogUtils.i(LOG_TAG, "Could not create file " + file.getAbsolutePath(), e);
				setTaskFailure(t, ERROR_CREATE_FILE);
				return;
			}
		}

		if (!file.canWrite()) {
			LogUtils.i(LOG_TAG, "Cannot write to file " + file.getAbsolutePath());
			setTaskFailure(t, ERROR_WRITE_FILE);
			return;
		}

		if (!append && file.length() > 0) {
			LogUtils.i(LOG_TAG, "Cannot write to file " + file.getAbsolutePath());
			setTaskFailure(t, ERROR_WRITE_NOT_EMPTY);
			return;
		}

	}

	private void setTaskFailure(PersistenceTask t, String message) {
		t.setResult(Result.FAIL, message);
	}

	private void setTaskSuccess(PersistenceTask t) {
		t.setResult(Result.SUCCESS, null);
	}

	public void induceStop() {
		this.running = false;
	}

	public int getNumTasks() {
		return this.tasks.size();
	}

}
