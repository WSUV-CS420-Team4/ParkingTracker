package com.southwaterfront.parkingtracker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.alpr.AlprCallBack;
import com.southwaterfront.parkingtracker.alpr.AlprEngine;
import com.southwaterfront.parkingtracker.data.BlockFace;
import com.southwaterfront.parkingtracker.data.CallBack;
import com.southwaterfront.parkingtracker.data.DataManager;
import com.southwaterfront.parkingtracker.data.ParkingStall;
import com.southwaterfront.parkingtracker.util.AsyncTask;
import com.southwaterfront.parkingtracker.util.Utils;
import com.southwaterfront.parkingtracker.util.WifiStateUploadableDataReceiver;

public class Main extends Activity {

	private static final String LOG_TAG = "Main";

	// -------------------------------------------------------------------------------------------------
	// Temp placement of code
	// -------------------------------------------------------------------------------------------------
	public static boolean isInForeground = false;
	static final int REQUEST_TAKE_PHOTO = 2;
	String mCurrentPhotoPath;

	private AssetManager assets;
	private DataManager data;
	private BlockFace face;
	private AlprEngine ocrEngine;
	TextView textView;
	EditText editText;
	AlertDialog.Builder wifiAlert;
	WifiStateUploadableDataReceiver wifiReceiver;
	IntentFilter wifiFilter;
	SharedPreferences prefs;
	String wifiAlertPrefKey;
	File photoFile;

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		//File imagesCache = AssetManager.getInstance().getImageCacheDir();
		File image = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
				//imagesCache
				);

		// Save a file: path for use with ACTION_VIEW intents
		//mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	private void dispatchTakePictureIntent2(){
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			//galleryAddPic();
			setPic();
		}
	}

	private void setPic() {
		final ImageView imageView = (ImageView)  findViewById(R.id.imageView1);

		// Get the dimensions of the View
		int targetW = imageView.getWidth();
		int targetH = imageView.getHeight();

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;


		// Determine how much to scale down the image
		int scaleFactor;
		if (targetW == 0 || targetH == 0){
			scaleFactor = 1;
		} else {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);
		}

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		imageView.setImageBitmap(bitmap);

		textView.setText("Waiting for OcrEngine");
		ocrEngine.runOcr(photoFile, new AlprCallBack() {

			@Override
			public void call(final String[] result) {
				Main.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setOcrResult(result);
					}

				});
			}

		});
	}

	int stall = 0;

	private void setOcrResult(String[] result) {
		String viewString = "";
		if (result != null) {
			int i = 0;
			for (; i < result.length - 1; i++)
				viewString += result[i] + "\n";
			viewString += result[i];
		}
		textView.setText("OCR Demo App");
		editText.setText(viewString);
		if (result != null)
			face.setStall(new ParkingStall(result[0], new Date(System.currentTimeMillis()), null), stall++);
	}
	// -------------------------------------------------------------------------------------------------

	@Override
	public void onStart() {
		super.onStart();
		textView = (TextView) findViewById(R.id.textView2);
		editText = (EditText) findViewById(R.id.editText1);
	}

	@Override
	public void onResume() {
		super.onResume();
		isInForeground = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		isInForeground = false;
	}

	private void onCreateAppInit() {
		AssetManager.init(this);
		assets = AssetManager.getInstance();
		assets.assetSanityCheck();
		data = DataManager.getInstance();
		ocrEngine = AlprEngine.getInstance();
		Utils.resetCacheSize();
		wifiFilter = new IntentFilter();
		wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		this.wifiReceiver = new WifiStateUploadableDataReceiver();
		this.registerReceiver(this.wifiReceiver, this.wifiFilter);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		wifiAlertPrefKey = getResources().getString(R.string.wifiAlertSetting);
		
		Log.i(LOG_TAG, "App initialized successfully");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/**
		 * Leave this method call
		 */
		onCreateAppInit();


		face = BlockFace.emptyPaddedBlockFace("1", "C", 14);

		// Temp Button init location
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				dispatchTakePictureIntent2(); 	
			}
		});


		// Temp Button init location
		final Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Utils.isWifiConnected() || !wifiAlertEnabled())
					upload();
				else
					wifiAlert.show();
			}
		});

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					upload();
					dialog.dismiss();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
					break;
				}
			}
		};

		wifiAlert = new AlertDialog.Builder(this);
		wifiAlert.setMessage("You are not internet connected through wifi. Are you sure you want to continue?").setPositiveButton("Yes", dialogClickListener)
		.setNegativeButton("No", dialogClickListener);

	}

	private boolean wifiAlertEnabled() {
		return prefs.getBoolean(wifiAlertPrefKey, true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		onDestroyAppClose();
	}
	
	private void onDestroyAppClose() {
		this.unregisterReceiver(this.wifiReceiver);
		data.close();
		ocrEngine.close();
	}

	private void upload() {
		data.saveBlockFace(face, null);
		face = new BlockFace("3", "B");
		data.uploadSessionData(new CallBack() {

			@Override
			public void call(final AsyncTask task) {
				Main.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						TextView view = (TextView) findViewById(R.id.textView2);
						view.setText("Result was a " + task.getResult());
					}

				});
			}

		});

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
			Intent settings = new Intent(this, Settings.class);
			startActivity(settings);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}