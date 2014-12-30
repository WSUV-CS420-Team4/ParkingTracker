package com.southwaterfront.parkingtracker.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.data.DataManager;
import com.southwaterfront.parkingtracker.notification.Notifications;

/**
 * The receiver that will notify the application when the wifi
 * state has changed. This can be used to push notifications to the user
 * to recommend an upload.
 * 
 * <br>
 * 
 * The behavior of this receiver is such that if it is called when the wifi is connected
 * and the app is in the background, a notification will be posted to the phone
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class WifiReceiver extends BroadcastReceiver {

	public static final String LOG_TAG = "WifiReceiver";

	public static final Notification uploadNotification = Notifications.getUploadNotifcation();

	public static final DataManager data = DataManager.getInstance();

	@Override
	public void onReceive(Context context, Intent intent) {
		if(!isInitialStickyBroadcast()) {
			if (Utils.isWifiConnected()) {
				if (data.existsUploadableSessions() && !Main.isInForeground) {
					NotificationManager notManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notManager.notify(0, uploadNotification);
				}

				Log.i(LOG_TAG, "Wifi now connected");
			} else
				Log.i(LOG_TAG, "Wifi now disconnected");  
		}
	}

}
