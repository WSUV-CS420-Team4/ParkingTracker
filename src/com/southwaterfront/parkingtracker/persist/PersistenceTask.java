package com.southwaterfront.parkingtracker.persist;

import java.io.File;

import com.southwaterfront.parkingtracker.data.Result;
import com.southwaterfront.parkingtracker.data.Task;

/**
 * Defines a task to be done by a {@link PersistenceWorker}. Tasks are defined
 * by enum {@link PersistenceTask.Tasks}
 * 
 * @author Vitaily Gavrilov
 *
 */
public class PersistenceTask extends Task {

	/**
	 * Available tasks
	 */
	public static enum Tasks {
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

	public final Object data;

	public final File file;

	public final Tasks task;

	public PersistenceTask(Object data, File file, Tasks task) {
		super();
		
		if (file == null || task == null)
			throw new IllegalArgumentException("File and task cannot be null");
		if ((task == Tasks.APPEND || task == Tasks.WRITE) && data == null)
			throw new IllegalArgumentException("Data cannot be null if writing");
		
		this.data = data;
		this.file = file;
		this.task = task;
	}

	/**
	 * Allows the setting of a result, effectively works as a
	 * set once method.
	 * 
	 * @param result Result of task
	 * @param error Error message, can be null
	 */
	@Override
	public void setResult(Result result, String error) {
		super.setResult(result, error);
	}
}
