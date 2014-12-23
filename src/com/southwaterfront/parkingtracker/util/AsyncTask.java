package com.southwaterfront.parkingtracker.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A generic task to be overridden for specific tasks.
 * <br>
 * The results are defined in {@link Result}. To get the result,
 * call {@link #getResult()}. This will be in progress if not done, or success or fail. If
 * it is a failure there may be an error message which is retrieved by {@link #getErrorMessage()}.
 * To synchronously wait until the task is done use {@link #waitOnResult()} which will return the result
 * when the task is done.
 * <br>
 * Note that setting the result is intended to be called from an extending class method so a Task can freely
 * float around publicly.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public abstract class AsyncTask {

	private Result result;

	private String errorMessage;

	private final Lock lock;

	private final Condition finished;
	
	protected AsyncTask() {
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
	protected void setResult (Result result, String error) {
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
