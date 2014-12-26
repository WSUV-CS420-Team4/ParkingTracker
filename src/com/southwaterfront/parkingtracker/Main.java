package com.southwaterfront.parkingtracker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import OcrEngine.OcrCallBack;
import OcrEngine.OcrEngine;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.data.BlockFace;
import com.southwaterfront.parkingtracker.data.CallBack;
import com.southwaterfront.parkingtracker.data.DataManager;
import com.southwaterfront.parkingtracker.data.ParkingStall;
import com.southwaterfront.parkingtracker.util.AsyncTask;
import com.southwaterfront.parkingtracker.util.Utils;

public class Main extends Activity {

	private static final String LOG_TAG = "Main";

	// -------------------------------------------------------------------------------------------------
	// Temp placement of code
	// -------------------------------------------------------------------------------------------------

	static final int REQUEST_TAKE_PHOTO = 2;
	String mCurrentPhotoPath;

	private AssetManager assets;
	private DataManager data;
	private BlockFace face;
	private OcrEngine ocrEngine;
	TextView textView;
	EditText editText;
	AlertDialog.Builder wifiAlert;

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
			File photoFile = null;
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
		ocrEngine.runOcr(bitmap, null, new OcrCallBack() {

			@Override
			public void call(final String result) {
				Main.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setOcrResult(result);
					}
					
				});
			}
			
		});
	}
	
	private void setOcrResult(String result) {
		result = result.substring(0, result.length() > 30 ? 30 : result.length());
		textView.setText("OCR Demo App");
		editText.setText(result);
		face.addStall(new ParkingStall(result, new Date(System.currentTimeMillis()), null));
	}
	// -------------------------------------------------------------------------------------------------

	@Override
	public void onStart() {
		super.onStart();
		textView = (TextView) findViewById(R.id.textView2);
		editText = (EditText) findViewById(R.id.editText1);
		
		/*
		String wifi = "Wifi is ";
		if (!Utils.isWifiConnected()) wifi += "not ";
		wifi += "connected";
		Toast.makeText(getApplicationContext(), wifi,
			   Toast.LENGTH_LONG).show();
			   */
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/**
		 * Always leave the next 5 lines in the main on create in this order
		 */
		AssetManager.init(this.getApplicationContext());
		assets = AssetManager.getInstance();
		assets.assetSanityCheck();
		data = DataManager.getInstance();
		ocrEngine = OcrEngine.getInstance();
		Utils.resetCacheSize();

		face = new BlockFace("3", "A");

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
				if (Utils.isWifiConnected())
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}