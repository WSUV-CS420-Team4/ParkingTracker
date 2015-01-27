package com.southwaterfront.parkingtracker.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.data.DataManager;
import com.southwaterfront.parkingtracker.notification.Notifications;
import com.southwaterfront.parkingtracker.prefs.ParkingTrackerPreferences;

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
public class WifiStateUploadableDataReceiver extends BroadcastReceiver {

	private final String LOG_TAG = "WifiReceiver";

	private final Notification uploadNotification;

	private final DataManager data;
	
	private final NotificationManager notificationManager;

	/**
	 * This is necessary because {@link ConnectivityManager#CONNECTIVITY_ACTION} doesn't just
	 * refer to a change in the active connection, so this receiver may be triggered when
	 * even there is a 3G/4G change. So we need fine grain control over when we trigger
	 * a notification.
	 */
	private boolean wifiWasConnected = Utils.isWifiConnected();

	public WifiStateUploadableDataReceiver() {
		uploadNotification = Notifications.getUploadNotifcation();
		data = DataManager.getInstance();
		
		AssetManager assets = AssetManager.getInstance();
		Context context = assets.getMainContext();

		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean uploadSettingSet = ParkingTrackerPreferences.getWifiDataUploadNotificationSetting();
		boolean wifiIsConnected = Utils.isWifiConnected();
		if(!isInitialStickyBroadcast() && uploadSettingSet) {
			if (!wifiWasConnected && wifiIsConnected) {
				if (data.existsUploadableSessions() && !Main.isInForeground)
					notificationManager.notify(0, uploadNotification);

				Log.i(LOG_TAG, "Wifi now connected");
			} else if (wifiWasConnected && !wifiIsConnected)
				Log.i(LOG_TAG, "Wifi now disconnected");
		}
		wifiWasConnected = wifiIsConnected;
	}

}
