package com.southwaterfront.parkingtracker.prefs;

import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

/**
 * A small wrapper for the app preferences. This class listens
 * to prefernces changes and updates them as needed.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class ParkingTrackerPreferences {

	private static String LOG_TAG = "ParkingTrackerPreferences";
	
	public static final String wifiDataUploadNotificationKey;
	private static boolean wifiDataUploadNotificationSetting = true;
	
	public static final String nonWifiConnectionNotificationKey;
	private static boolean nonWifiConnectionNotificationSetting = true;
	
	static {
		AssetManager assets = AssetManager.getInstance();
		Context context = assets.getMainContext();
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Resources r = assets.getAppResources();
		
		wifiDataUploadNotificationKey = r.getString(R.string.uploadNotificationSetting);
		nonWifiConnectionNotificationKey = r.getString(R.string.wifiAlertSetting);
		
		wifiDataUploadNotificationSetting = pref.getBoolean(wifiDataUploadNotificationKey, true);
		nonWifiConnectionNotificationSetting = pref.getBoolean(nonWifiConnectionNotificationKey, true);
		
		SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key.equals(wifiDataUploadNotificationKey)) {
					wifiDataUploadNotificationSetting = sharedPreferences.getBoolean(wifiDataUploadNotificationKey, wifiDataUploadNotificationSetting);
				} else if (key.equals(nonWifiConnectionNotificationKey)) {
					nonWifiConnectionNotificationSetting = sharedPreferences.getBoolean(nonWifiConnectionNotificationKey, nonWifiConnectionNotificationSetting);
				}
			}
			
		};
		pref.registerOnSharedPreferenceChangeListener(prefListener);
	}
	
	/**
	 * Getter for setting of whether to set a notification if 
	 * wifi connectivity was enabled and data is available to upload.
	 * 
	 * @return True if setting set, false if not
	 */
	public static boolean getWifiDataUploadNotificationSetting() {
		return wifiDataUploadNotificationSetting;
	}
	
	/**
	 * Getter for setting of whether to warn if trying to upload
	 * over a non wifi connection
	 * 
	 * @return True if setting set, false if not
	 */
	public static boolean getNonWifiConnectionNotificationSetting() {
		return nonWifiConnectionNotificationSetting;
	}
	
}
