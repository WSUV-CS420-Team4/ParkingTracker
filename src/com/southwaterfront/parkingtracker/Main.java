package com.southwaterfront.parkingtracker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;

public class Main extends Activity {
	
	private static final String LOG_TAG = "Main";
	
	private static Context MAIN_CONTEXT = null;
	
	public static Context getMainContext() {
		return MAIN_CONTEXT;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MAIN_CONTEXT = this.getApplicationContext();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AssetManager.assetSanityCheck();
		
		TessBaseAPI baseApi = new TessBaseAPI();
		String path = AssetManager.getEnglishLanguageDataDir();
		try {
			baseApi.init(path, "eng");
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
