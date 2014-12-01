package com.southwaterfront.parkingtracker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import OcrEngine.OcrEngine;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.data.BlockFace;
import com.southwaterfront.parkingtracker.data.DataManager;
import com.southwaterfront.parkingtracker.data.ParkingStall;

public class Main extends Activity {

	private static final String LOG_TAG = "Main";

	// -------------------------------------------------------------------------------------------------
	// Temp placement of code
	// -------------------------------------------------------------------------------------------------
	
	static final int REQUEST_TAKE_PHOTO = 2;
	String mCurrentPhotoPath;
	
	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "IMG_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
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
	    /*
	    int[] pixels  = new int[bitmap.getHeight()*bitmap.getWidth()];
	    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
	    
	    for(int i = 0; i < pixels.length; i++) {
	    	int pixel = pixels[i];
	    	if (((pixel >>> 0) & 255) < 200)
	    		pixels[i] = 0x00FFFFFF;
	    }
	    
	    bitmap = Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
	    imageView.setImageBitmap(bitmap);
	    */
	    OcrEngine o = OcrEngine.getInstance();
	    String result = o.runOcr(bitmap, null);
	    result = result.substring(0, result.length() > 30 ? 30 : result.length());
	    
	    int duration = Toast.LENGTH_LONG;

	    Toast toast = Toast.makeText(getBaseContext(), result, duration);
	    toast.show();
	    TextView view = (TextView) findViewById(R.id.textView2);
	    view.setText(result);
	}
	// -------------------------------------------------------------------------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		AssetManager.init(this.getApplicationContext());
		AssetManager assetManager = AssetManager.getInstance();
		DataManager dataManager = DataManager.getInstance();
		Log.i(LOG_TAG, "Session start time " + dataManager.getSessionName());


		assetManager.assetSanityCheck();
		
		// Temp Button init location
		final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
            	dispatchTakePictureIntent2(); 	
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
