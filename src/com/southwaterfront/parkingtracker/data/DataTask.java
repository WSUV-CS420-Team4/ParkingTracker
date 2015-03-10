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
		 * Save session data
		 */
		SAVE_DATA,
		/**
		 * Upload session data to server
		 */
		UPLOAD_DATA
	}
	
	public final Session sess;
	
	public final CallBack callBack;
	
	public final Tasks type;
	
	public DataTask(Session s, CallBack callBack, Tasks type) {
		super();
		if (s == null || type == null)
			throw new IllegalArgumentException("Cannot be null object, pass a BlockFace for a store or a Session for upload");
		
		this.sess = s;
		this.callBack = callBack;
		this.type = type;
	}
	
	@Override
	public void setResult(Result result, String error) {
		super.setResult(result, error);
	}

}
