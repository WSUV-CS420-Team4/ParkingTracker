package com.southwaterfront.parkingtracker.util;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;

/**
 * Static utility helper to keep track abstract keeping track of gps location. It is
 * important to note this class does not track network location or any other kind, but strictly 
 * gps location. To get updates, start the service with {@link GPSLocationUtils#startService(int, int)}, and 
 * register a listener with {@link GPSLocationUtils#addListener(LocationChangeListener)}.
 * <p>
 * This class may never be used, but the barebones for the services is started here
 * 
 * @author Vitaly Gavrilov
 *
 */
public class GPSLocationUtils {

	private static final String LOG_TAG = "LocationUtils";

	private static final LocationManager locationManager;
	private static final LocationListener listener;
	private static boolean locationEnabled;
	private static Location lastLocation;
	private static boolean isServiceRunning = false;
	private static Set<LocationChangeListener> tasks = new HashSet<LocationChangeListener>();

	/**
	 * Listener that can be used to do work when the gps location changes
	 * 
	 * @author Vitaly Gavrilov
	 *
	 */
	public static interface LocationChangeListener {
		public void onLocationChanged(Location l);
	}

	static {
		AssetManager assets = AssetManager.getInstance();
		Context context = assets.getMainContext();

		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		listener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				lastLocation = location;
				for (LocationChangeListener l : tasks) {
					try {
						l.onLocationChanged(location);
					} catch (Exception e) {
						Log.e(LOG_TAG, e.getMessage(), e);
					}
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
				if (provider.equals(LocationManager.GPS_PROVIDER))
					locationEnabled = true;
			}

			@Override
			public void onProviderDisabled(String provider) {
				if (provider.equals(LocationManager.GPS_PROVIDER))
					locationEnabled = false;
			}

		};

	}

	/**
	 * Checks if GPS fine locatio is on
	 * 
	 * @return True if on, false otherwise
	 */
	public boolean isLocationOn() {
		return locationEnabled;
	}

	/**
	 * Gets the last known gps location
	 * 
	 * @return Location last provided by gps service
	 */
	public Location getLastKnownLocation() {
		if (isServiceRunning)
			return lastLocation;
		else {
			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
	}

	/**
	 * Starts the srvice to track gps location changes
	 * 
	 * @param minMs Minimum milliseconds to wait for a location change,
	 * not exact but to help the system conserve battery
	 * @param minMeters Minimum meters change to wait for
	 */
	public synchronized void startService(int minMs, int minMeters) {
		if (!isServiceRunning) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minMs, minMeters, listener);
			isServiceRunning = true;
		}
	}

	/**
	 * Turns off the gps tracking service
	 */
	public synchronized void stopService() {
		if (isServiceRunning) {
			locationManager.removeUpdates(listener);
		}
	}

	/**
	 * Checks if service is runnning
	 * 
	 * @return True if running, false otherwise
	 */
	public boolean isServiceRunning() {
		return isServiceRunning;
	}

	/**
	 * Adds a listener to run when a change happens
	 * 
	 * @param l An instance of the listener interface
	 */
	public void addListener(LocationChangeListener l) {
		tasks.add(l);
	}

	/**
	 * Removes the listener from being called
	 * 
	 * @param l Listener instance to remove
	 */
	public void removeListener(LocationChangeListener l) {
		tasks.remove(l);
	}
}
