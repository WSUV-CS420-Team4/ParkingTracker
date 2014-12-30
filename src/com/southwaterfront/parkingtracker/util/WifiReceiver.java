package com.southwaterfront.parkingtracker.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
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
	
	public static final SharedPreferences pref;
	
	public static final String uploadNotificationSettingKey;
	
	static {
		AssetManager assets = AssetManager.getInstance();
		pref = PreferenceManager.getDefaultSharedPreferences(assets.getMainContext());
		Resources r = assets.getAppResources();
		uploadNotificationSettingKey = r.getString(R.string.uploadNotificationSetting);
	}
	
	/**
	 * This is necessary because {@link ConnectivityManager#CONNECTIVITY_ACTION} doesn't just
	 * refer to a change in the active connection, so this receiver may be triggered when
	 * even there is a 3G/4G change. So we need fine grain control over when we trigger
	 * a notification.
	 */
	public static boolean wifiWasConnected = Utils.isWifiConnected();

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean uploadSettingSet = pref.getBoolean(uploadNotificationSettingKey, true);
		if(!isInitialStickyBroadcast() && uploadSettingSet) {
			boolean wifiIsConnected = Utils.isWifiConnected();
			if (!wifiWasConnected && wifiIsConnected) {
				if (data.existsUploadableSessions() && !Main.isInForeground) {
					NotificationManager notManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notManager.notify(0, uploadNotification);
				}

				Log.i(LOG_TAG, "Wifi now connected");
			} else if (wifiWasConnected && !wifiIsConnected)
				Log.i(LOG_TAG, "Wifi now disconnected");
			wifiWasConnected = wifiIsConnected;
		}
	}

}
