package com.southwaterfront.parkingtracker.data;

import com.southwaterfront.parkingtracker.util.AsyncTask;


/**
 * Callback interface
 * @author Vitaliy Gavrilov
 *
 */
public interface CallBack {
	
	/**
	 * Single call method
	 */
	public void call(final AsyncTask task);
	
}
