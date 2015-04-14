package com.southwaterfront.parkingtracker.util;

import android.util.Log;

/**
 * Android Log wrapper to enable/disable logging easy
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class LogUtils {

	private static final boolean DEBUG = false;

	public static void i(String tag, String msg) {
		if (DEBUG)
		 Log.i(tag, msg);
	}
	
	public static void i(String tag, String msg, Throwable t) {
		if (DEBUG)
		 Log.i(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		if (DEBUG)
		 Log.e(tag, msg);
	}
	
	public static void e(String tag, String msg, Throwable t) {
		if (DEBUG)
		 Log.e(tag, msg, t);
	}
	
}
