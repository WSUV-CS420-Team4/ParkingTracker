package com.southwaterfront.parkingtracker.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * The receiver that will notify the application when the wifi
 * state has changed. This can be used to push notifications to the user
 * to recommend an upload.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class WifiReceiver extends BroadcastReceiver {

	public static final String LOG_TAG = "WifiReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(!isInitialStickyBroadcast()) {
			if (Utils.isWifiConnected()) {
				Log.i(LOG_TAG, "Wifi now connected");
				Utils.postLongToast("Wifi now connected");
			} else
				Log.i(LOG_TAG, "Wifi now disconnected");  
		}
	}

}
