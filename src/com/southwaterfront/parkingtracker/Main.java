package com.southwaterfront.parkingtracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.JsonObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.jsonify.BlockFaceJsonBuilder;
import com.southwaterfront.parkingtracker.jsonify.MasterDataJsonBuilder;

public class Main extends Activity {

	private static final String LOG_TAG = "Main";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		AssetManager.init(this.getApplicationContext());
		AssetManager assetManager = AssetManager.getInstance();

		assetManager.assetSanityCheck();
		
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
