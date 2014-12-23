package com.southwaterfront.parkingtracker.data;

import com.southwaterfront.parkingtracker.data.DataManager.Session;
import com.southwaterfront.parkingtracker.util.AsyncTask;
import com.southwaterfront.parkingtracker.util.Result;

/**
 * A task definition that can be used to perform data related tasks
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class DataTask extends AsyncTask {
	
	/**
	 * Available tasks
	 */
	public static enum Tasks {
		/**
		 * Store a block face
		 */
		STORE_FACE,
		/**
		 * Upload session data to server
		 */
		UPLOAD_DATA
	}
	
	public final Object obj;
	
	public final CallBack callBack;
	
	public final Tasks type;
	
	public DataTask(Object obj, CallBack callBack, Tasks type) {
		super();
		
		if (obj == null || type == null)
			throw new IllegalArgumentException("Cannot be null object, pass a BlockFace for a store or a Session for upload");
		if (type == Tasks.STORE_FACE && !(obj instanceof BlockFace))
			throw new IllegalArgumentException("Must store a block face object");
		if (type == Tasks.UPLOAD_DATA && !(obj instanceof Session))
			throw new IllegalArgumentException("Need a Session object reference for upload task");
		
		this.obj = obj;
		this.callBack = callBack;
		this.type = type;
	}
	
	@Override
	public void setResult(Result result, String error) {
		super.setResult(result, error);
	}

}
