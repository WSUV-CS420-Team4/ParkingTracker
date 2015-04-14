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
import com.southwaterfront.parkingtracker.data.ParkingDataCollector;
import com.southwaterfront.parkingtracker.data.ParkingStall;
import com.southwaterfront.parkingtracker.dialog.AddLicenseDialogFragment;
import com.southwaterfront.parkingtracker.dialog.ChoosePlateDialogFragment;
import com.southwaterfront.parkingtracker.dialog.LocationSelectDialogFragment;
import com.southwaterfront.parkingtracker.dialog.SetFlagsDialogFragment;
import com.southwaterfront.parkingtracker.dialog.ViewDataDialogFragment;
import com.southwaterfront.parkingtracker.prefs.ParkingTrackerPreferences;
import com.southwaterfront.parkingtracker.util.AsyncTask;
import com.southwaterfront.parkingtracker.util.Result;
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
	private DataManager dataManager;
	private ParkingDataCollector dataCollector;
	private AlprEngine ocrEngine;
	private TextView textViewNotification;
	private AlertDialog.Builder wifiAlert;
	private WifiStateUploadableDataReceiver wifiReceiver;
	private IntentFilter wifiFilter;
	private File photoFile;

	private List<String> licensePlates = new ArrayList<String>();
	private ArrayAdapter<String> arrayAdapter;

	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;

	private Button buttonTakePhoto;
	private Button buttonMap;
	private Button buttonSync;
	private Button buttonData;
	private Button buttonOptions;

	private ProgressBar progressBar;

	private CharSequence[] flagOptions = { "Handicap Placards", "Residential  Permit", "Employee Permit", "Student Permit", "Carpool Permit", "Other" };
	private boolean[] flagSelections;

	private List<Integer> blockArray = new ArrayList<Integer>();
	private List<String> faceArray = new ArrayList<String>();
	private List<Integer> stallArray = new ArrayList<Integer>();

	private String currentResult;
	private int currentBlock;
	private int currentFace;
	private int currentStall;

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

		textViewNotification.setText("Recognizing license plate");
		progressBar.setVisibility(View.VISIBLE);
		buttonTakePhoto.setVisibility(View.GONE);
		ocrEngine.runAlpr(photoFile, new AlprCallBack() {
			// TODO : also why are we making a new anonymous class for every alpr call?
			@Override
			public void call(final String[] result) {
				// TODO : Work with Joel to update the view data adapter HERE
				Main.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							setOcrResult(result);
						} finally {
							photoFile.delete();
						}
					}

				});
			}

		});
	}

	private void setOcrResult(String[] result) {
		licensePlates.clear();
		clearFlagSelections();
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
		buttonTakePhoto.setVisibility(View.VISIBLE);
		if (result != null) {
			arrayAdapter = new ArrayAdapter<String>(Main.this, android.R.layout.simple_list_item_1, licensePlates );
			arrayAdapter.setDropDownViewResource(R.layout.choose_plate);

			Log.i("List", "licensePlates: " + licensePlates.size());

			// ChoosePlateDialogFragment Show Here
			showChoosePlateDialog();

			Toast.makeText(Main.this, licensePlates.size() + " Results", Toast.LENGTH_LONG).show();
		} else {
			showChoosePlateDialog();
			//textViewNotification.setText("0 Results");
			//Toast.makeText(Main.this, "0 Results", Toast.LENGTH_SHORT).show();
            Toast.makeText(Main.this, "OCR unable to detect license plate", Toast.LENGTH_SHORT).show();
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
		if (dataManager != null)
			dataManager.saveCurrentSessionData();
		isInForeground = false;
	}

	private void onCreateAppInit() {
		Runnable r = new Runnable() {

			public void run() {
				AssetManager.init(Main.this);
				assets = AssetManager.getInstance();
				assets.assetSanityCheck();
				dataManager = DataManager.getInstance();
				ocrEngine = AlprEngine.getInstance();
				Utils.resetCacheSize();
				wifiFilter = new IntentFilter();
				wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
				wifiReceiver = new WifiStateUploadableDataReceiver();
				registerReceiver(wifiReceiver, wifiFilter);
				dataCollector = dataManager.getCurrentSession().getDataCollector();
				Main.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						enableButtons();
					}

				});
			}
		};
		Thread initThread = new Thread(r);
		initThread.start();
		Log.i(LOG_TAG, "App initialized successfully");
	}

	private void initButtons() {

		buttonTakePhoto = (Button) findViewById(R.id.buttonMainPhoto);
		buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				dispatchTakePictureIntent2();
			}
		});


		buttonSync = (Button) findViewById(R.id.buttonMainSync);
		buttonSync.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Utils.isWifiConnected() || !ParkingTrackerPreferences.getNonWifiConnectionNotificationSetting()) {
					upload();
				}
				else
					wifiAlert.show();
			}
		});

		buttonMap = (Button) findViewById(R.id.buttonMainMap);
		buttonMap.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("Main", "Map Clicked!");
			}
		});

		// Temp Button init location
		buttonData = (Button) findViewById(R.id.buttonMainData);
		buttonData.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("Main", "View Data Clicked!");
				viewData();
				showViewDataDialog();
			}
		});

		buttonOptions = (Button) findViewById(R.id.buttonMainOptions);
		buttonOptions.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("Main", "Options Clicked!");
			}
		});
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

		setupLocationSelect();

		initButtons();

		currentResult = "";

		// Temp ProgressBar init location
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

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
		wifiAlert.setMessage("You are not internet connected through wifi. Are you sure you want to continue?")
		.setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener);

	}

	public void showChoosePlateDialog() {
		// Create the fragment and show it as a dialog.
		ChoosePlateDialogFragment newFragment = ChoosePlateDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "choosePlate");
	}

	public void showSetFlagsDialog() {
		// Create the fragment and show it as a dialog.
		SetFlagsDialogFragment newFragment = SetFlagsDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "setFlags");
	}


	public void showAddLicenseDialog() {
		// Create the fragment and show it as a dialog.
		AddLicenseDialogFragment newFragment = AddLicenseDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "addLicense");
	}

	public void showLocationSelectDialog() {
		// Create the fragment and show it as a dialog.
		LocationSelectDialogFragment newFragment = LocationSelectDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "locationSelect");
	}

	public void showViewDataDialog() {
		// Create the fragment and show it as a dialog.
		ViewDataDialogFragment newFragment = ViewDataDialogFragment.newInstance();
		newFragment.show(getFragmentManager(), "locationSelect");
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
		dataManager.close();
		ocrEngine.close();
	}

	private void upload() {
		dataManager.uploadSessionData(new CallBack() {

			@Override
			public void call(final AsyncTask task) {
				Main.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						TextView view = (TextView) findViewById(R.id.textViewMainNotification);
						//view.setText("Result was a " + task.getResult());
						if (task.getResult() == Result.SUCCESS) {
							dataCollector = dataManager.getCurrentSession().getDataCollector();
							view.setText("Successfully uploaded data");
						} else
							view.setText(task.getErrorMessage());
					}

				});
			}

		});

	}

	private String[] generateFlags() {

		String[] flagArray;

		//Log.i("flagSelections", "size: " + flagSelections.length);

		int length = 0;
		for (int i = 0; i < flagSelections.length; i++) {
			if ( flagSelections[i] ) {
				length++;
			}
		}

		//Log.i("generateFlags", "flag[" + i + "]: " + flagSelections[i]);
		flagArray = new String[length];
		length = 0;
		for (int i = 0; i < flagSelections.length; i++) {
			if ( flagSelections[i] ) {
				//flagArray[length++] = (String) flagOptions[length];
                flagArray[length] = (String) flagOptions[length];
                length++;
			}
		}

		return flagArray;
	}


	public void addData() {

		// Currently doesn't add correctly
		// TODO: Still need to add Attr (need to convert Boolean[] to String[] and replace null)
		dataCollector.setStall(blockArray.get(currentBlock), faceArray.get(currentFace), stallArray.get(currentStall) - 1,
				new ParkingStall(currentResult, new Date(System.currentTimeMillis()), generateFlags()));
		Log.i("Main", "Added " + currentResult + " to block " + blockArray.get(currentBlock) +
				", face " + faceArray.get(currentFace) + ", stall " + stallArray.get(currentStall));
	}

	public void viewData() {
		// Logs all the stalls currently held in data
		Log.i("viewData", "data size: " + dataCollector.getBlockFaces().size());

		// TODO: Check stall format 0-14 or 1-15
		for (BlockFace face : getData()) {
			for (int i = 0; i < face.getParkingStalls().size(); i++) {
				Log.i("stall", "block: " + face.block + " face: " + face.face + " stall: " + (i+1) +
						" plate: " + face.getParkingStalls().get(i).plate + " attr: " + face.getParkingStalls().get(i).attr);
			}

			// Results without stall data
			/*for ( ParkingStall stall: face.getParkingStalls()) {
                Log.i("stall", "block: " + face.block + " face: " + face.face + " plate: " + stall.plate + " attr: " + stall.attr);
            }*/
		}
	}

	public void enableButtons() {
		buttonTakePhoto.setEnabled(true);
		buttonMap.setEnabled(true);
		buttonSync.setEnabled(true);
		buttonData.setEnabled(true);
		buttonOptions.setEnabled(true);
	}

	public void disableButtons() {
		buttonTakePhoto.setEnabled(false);
		buttonMap.setEnabled(false);
		buttonSync.setEnabled(false);
		buttonData.setEnabled(false);
		buttonOptions.setEnabled(false);
	}

	public void setupLocationSelect() {
		// TODO remove hard code
		// assets.getStreetModel();

		blockArray = new ArrayList<Integer>() {{
			add(1);
			add(2);
			add(3);
			add(4);
			add(5);
			add(6);
			add(7);
			add(8);
			add(9);
			add(10);
			add(11);
			add(12);
			add(13);
			add(14);
			add(15);
			add(16);
			add(17);
			add(18);
			add(19);
			add(20);
			add(21);
			add(22);
			add(23);
			add(24);
		}};
		currentBlock = 0;

		faceArray = new ArrayList<String>() {{
			add("A");
			add("B");
			add("C");
			add("D");
		}};
		currentFace = 0;

		stallArray = new ArrayList<Integer>() {{
			add(1);
			add(2);
			add(3);
			add(4);
			add(5);
			add(6);
			add(7);
			add(8);
			add(9);
			add(10);
			add(11);
			add(12);
			add(13);
			add(14);
			add(15);
		}};
		currentStall = 0;
	}

	public List<BlockFace> getData() {
		// TODO: Joel take a look at this plz
		return new ArrayList<BlockFace>(dataCollector.getBlockFaces());
	}

	public CharSequence[] getFlagOptions() {
		return flagOptions;
	}

	public boolean[] getFlagSelections() {
		return flagSelections;
	}

	public List<Integer> getBlockArray() {
		return blockArray;
	}

	public List<String> getFaceArray() {
		return faceArray;
	}

	public List<Integer> getStallArray() {
		return stallArray;
	}

	public void setFlagSelections(boolean[] flagSelections) {
		this.flagSelections = flagSelections;
	}

	public int getCurrentBlock() {
		return currentBlock;
	}

	public int getCurrentFace() {
		return currentFace;
	}

	public int getCurrentStall() {
		return currentStall;
	}

	public void setCurrentResult(String currentResult) {
		this.currentResult = currentResult;
	}

	public void setCurrentBlock(int currentBlock) {
		this.currentBlock = currentBlock;
	}

	public void setCurrentFace(int currentFace) {
		this.currentFace = currentFace;
	}

	public void setCurrentStall(int currentStall) {
		this.currentStall = currentStall;
	}

	private void clearFlagSelections() {
		flagSelections =  new boolean[ flagOptions.length ];
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