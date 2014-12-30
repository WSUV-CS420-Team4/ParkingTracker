package com.southwaterfront.parkingtracker.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.southwaterfront.parkingtracker.Main;
import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;


/**
 * A sort of managing class for notifications
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class Notifications {

	private static final Notification uploadNotification;
	
	static {
		Context context = AssetManager.getInstance().getMainContext();
		
		Intent resultIntent = new Intent(context, Main.class);
		PendingIntent resultPendingIntent =
		    PendingIntent.getActivity(
		    context,
		    0,
		    resultIntent,
		    PendingIntent.FLAG_UPDATE_CURRENT
		);
		
		Notification.Builder builder = new Notification.Builder(context)
    .setContentTitle("Wifi now connected")
    .setContentText("Click for upload screen")
    .setSmallIcon(R.drawable.ic_launcher)
    .setContentIntent(resultPendingIntent)
    .setAutoCancel(true);
		uploadNotification = builder.getNotification();
	}
	
	/**
	 * Get the upload notification
	 * 
	 * @return Notification
	 */
	public static Notification getUploadNotifcation() {
		return Notifications.uploadNotification;
	}
}
