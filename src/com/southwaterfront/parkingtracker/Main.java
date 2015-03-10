package com.southwaterfront.parkingtracker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.alpr.AlprCallBack;
import com.southwaterfront.parkingtracker.alpr.AlprEngine;
import com.southwaterfront.parkingtracker.data.BlockFace;
import com.southwaterfront.parkingtracker.data.CallBack;
import com.southwaterfront.parkingtracker.data.DataManager;
import com.southwaterfront.parkingtracker.data.ParkingStall;
import com.southwaterfront.parkingtracker.dialog.ChoosePlateDialogFragment;
import com.southwaterfront.parkingtracker.dialog.LoginDialogFragment;
import com.southwaterfront.parkingtracker.prefs.ParkingTrackerPreferences;
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
	TextView textViewNotification;
	AlertDialog.Builder wifiAlert;
	WifiStateUploadableDataReceiver wifiReceiver;
	IntentFilter wifiFilter;
	File photoFile;

    List<String> licensePlates = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Button takePhoto;
    ProgressBar progressBar;

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
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
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
        // No longer need to set pic
		/*final ImageView imageView = (ImageView)  findViewById(R.id.imageView1);

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
		imageView.setImageBitmap(bitmap);*/

		textViewNotification.setText("Waiting for OcrEngine");
        progressBar.setVisibility(View.VISIBLE);
        takePhoto.setVisibility(View.GONE);

		ocrEngine.runOcr(photoFile, new AlprCallBack() {

			@Override
			public void call(final String[] result) {
				Main.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setOcrResult(result);
                        photoFile.delete();
					}

				});
			}

		});
	}

	int stall = 0;

	private void setOcrResult(String[] result) {
        licensePlates.clear();
		if (result != null) {
			/*int i = 0;
			for (; i < result.length - 1; i++)
				viewString += result[i] + "\n";
			viewString += result[i];*/
            for (int j = 0; j < result.length; j++) {
                licensePlates.add( result[j] );
            }
		}
		textViewNotification.setText(""); // "OCR Demo App"
        progressBar.setVisibility(View.GONE);
        takePhoto.setVisibility(View.VISIBLE);
		if (result != null) {
            // TODO Change this so instead of best estimated result it is what the user clicks on
            face.setStall(new ParkingStall(result[0], new Date(System.currentTimeMillis()), null), stall++);

            /*String tempt[] = new String[licensePlates.size()];
            tempt = licensePlates.toArray(tempt);*/
            arrayAdapter = new ArrayAdapter<String>(Main.this, android.R.layout.simple_list_item_1, licensePlates );
            arrayAdapter.setDropDownViewResource(R.layout.choose_plate);

            Log.i("List", "licensePlates: " + licensePlates.size());
            //choosePlateDialogFragment.setAdapter(arrayAdapter);

            // ChoosePlateDialogFragment Show Here
            showChoosePlateDialog();
            //choosePlateDialogFragment.show(getFragmentManager(), "Login");

            Toast.makeText(Main.this, licensePlates.size() + " Results", Toast.LENGTH_LONG).show();
        }
	}
	// -------------------------------------------------------------------------------------------------

	@Override
	public void onStart() {
		super.onStart();
		textViewNotification = (TextView) findViewById(R.id.textViewMainNotification);
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
		
		Log.i(LOG_TAG, "App initialized successfully");
	}

    private void ChoosePlateInit() {

        //choosePlateDialogFragment = new ChoosePlateDialogFragment();

        // Old Popup window style
        /*LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
        popupLayout = layoutInflater.inflate(R.layout.choose_plate, null);

        popupWindow = new PopupWindow(popupLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(popupLayout);
        popupWindow.setFocusable(true);

        listView = (ListView) popupLayout.findViewById(R.id.listViewChoosePlate);

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("ListView", "You clicked Item: " + id + " at position:" + position);
                plateNo.setText((String) parent.getItemAtPosition(position));
            }
        });

        plateNo = (TextView) popupLayout.findViewById(R.id.textViewChoosePlateResult);*/

    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set content view AFTER ABOVE sequence (to avoid crash)
        this.setContentView(R.layout.activity_main); // Probably could just remove title bar before first set contentView

        /**
         * Leave this method call
         */
		onCreateAppInit();

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        ChoosePlateInit();


		face = BlockFace.emptyPaddedBlockFace(1, "C", 14);

		// Temp Button init location
		takePhoto = (Button) findViewById(R.id.buttonMainPhoto);
		takePhoto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				dispatchTakePictureIntent2(); 	
			}
		});


		// Temp Button init location
		final Button button2 = (Button) findViewById(R.id.buttonMainSync);
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                if (Utils.isWifiConnected() || !ParkingTrackerPreferences.getNonWifiConnectionNotificationSetting()) {
                    upload();
                }
				else
					wifiAlert.show();
			}
		});

        // Temp ProgressBar init location
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Temp DialogFragment init location
        LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.show(getFragmentManager(), "Login");

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

    public void showChoosePlateDialog() {
        // Create the fragment and show it as a dialog.
        ChoosePlateDialogFragment newFragment = ChoosePlateDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "choosePlate");
    }

    public ArrayAdapter<String> getArrayAdapter() {
        return arrayAdapter;
    }

    public List<String> getLicensePlates() {
        return licensePlates;
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
		//data.saveBlockFace(face, null);
		face = new BlockFace(3, "B");
		data.uploadSessionData(new CallBack() {

			@Override
			public void call(final AsyncTask task) {
				Main.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						TextView view = (TextView) findViewById(R.id.textViewMainNotification);
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