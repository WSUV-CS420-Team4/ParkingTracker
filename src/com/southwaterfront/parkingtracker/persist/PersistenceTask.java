package com.southwaterfront.parkingtracker.persist;

import java.io.File;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Defines a task to be done by a {@link PersistenceWorker}.
 * <br>
 * The tasks are defined by enum in {@link PersistenceTask.Task}, 
 * just as the results in {@link PersistenceTask.Result}. To get the result,
 * call {@link #getResult()}. This will be in progress if not done, or success or fail. If
 * it is a failure there may be an error message which is retrieved by {@link #getErrorMessage()}.
 * To synchronously wait until the task is done use {@link #waitOnResult()} which will return the result
 * when the task is done.
 * <br>
 * Note that setting the result is publicly available, however doing so will negate the actual result 
 * from being set.
 * 
 * @author Vitaily Gavrilov
 *
 */
public class PersistenceTask {

	/**
	 * Available tasks
	 */
	public static enum Task {
		/**
		 * Write to a file
		 */
		WRITE, 
		/**
		 * Append to a file
		 */
		APPEND, 
		/**
		 * Delete a file
		 */
		DELETE
	}

	/**
	 * Result types
	 */
	public static enum Result {
		IN_PROGRESS, SUCCESS, FAIL
	}

	public final Object data;

	public final File file;

	public final Task task;

	private Result result;

	private String errorMessage;

	private final Lock lock;

	private final Condition finished;

	public PersistenceTask(Object data, File file, Task task) {
		if (file == null || task == null)
			throw new IllegalArgumentException("File and task cannot be null");
		if ((task == Task.APPEND || task == Task.WRITE) && data == null)
			throw new IllegalArgumentException("Data cannot be null if writing");

		this.data = data;
		this.file = file;
		this.task = task;
		this.result = Result.IN_PROGRESS;
		this.lock = new ReentrantLock();
		this.finished = this.lock.newCondition();
	}

	/**
	 * Allows the setting of a result, effectively works as a
	 * set once method.
	 * 
	 * @param result Result of task
	 * @param error Error message, can be null
	 */
	public void setResult (Result result, String error) {
		if (result != null && this.result == Result.IN_PROGRESS && result != Result.IN_PROGRESS) {
			try {
				lock.lock();
				this.result = result;
				this.errorMessage = error;
				this.finished.signalAll();
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * Getter for result of task
	 * 
	 * @return Result
	 */
	public Result getResult() {
		return this.result;
	}

	/**
	 * Getter for error message of result, can be null
	 * 
	 * @return Error message
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Allows a user to block until result comes in
	 */
	public Result waitOnResult() {
		if (this.result == Result.IN_PROGRESS) {
			try {
				this.lock.lock();
				if (this.result == Result.IN_PROGRESS)
					this.finished.await();
			} catch (InterruptedException e) {
			} finally {
				this.lock.unlock();
			}
		}
		return this.result;
	}

}
