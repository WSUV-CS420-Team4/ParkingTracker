package com.southwaterfront.parkingtracker.dialog;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.client.HttpClient;
import com.southwaterfront.parkingtracker.client.HttpClient.RequestFailedException;

/**
 * Created by Joel on 3/3/2015.
 */
public class LoginDialogFragment extends DialogFragment {

	private static final String LOG_TAG = LoginDialogFragment.class.getSimpleName();

	Lock lock;
	private final Condition done;
	private boolean success;

	private static final Activity main = AssetManager.getInstance().getMainActivity();

	public LoginDialogFragment() {
		lock = new ReentrantLock();
		done = lock.newCondition();
		success = false;
	}

	public boolean waitOnResult() throws InterruptedException {
		try {
			lock.lock();
			done.await();
		} finally {
			lock.unlock();
		}
		return success;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View dialogView = inflater.inflate(R.layout.login, null);

		final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextLoginName);
		final EditText editTextPassword = (EditText) dialogView.findViewById(R.id.editTextLoginPassword);
		final TextView textViewLoginTitle = (TextView) dialogView.findViewById(R.id.textViewLoginTitle);

		builder.setView(dialogView)
		.setPositiveButton("Sign In", null)

		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					lock.lock();
					done.signal();
				} finally {
					lock.unlock();
				}
				LoginDialogFragment.this.getDialog().cancel();
			}
		});

		AlertDialog dialog = builder.create();

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialog) {
				final Button cancel = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
				final Button confirm = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);

				confirm.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Runnable r = new Runnable() {

							public void run() {
								boolean loggedIn = false;
								try {
									setText(textViewLoginTitle, "Logging in...");
									loggedIn = HttpClient.sendLoginRequest(editTextName.getText().toString(), editTextPassword.getText().toString());
								} catch (RequestFailedException e) {
									setText(textViewLoginTitle, e.getMessage());
									Log.e(LOG_TAG, e.getMessage(), e);
									return;
								} catch (IOException e) {
									setText(textViewLoginTitle, "Unable to connect to server");
									Log.e(LOG_TAG, e.getMessage(), e);
									return;
								}
								if (loggedIn) {
									success = true;
									try {
										lock.lock();
										done.signal();
									} finally {
										lock.unlock();
									}
									dialog.dismiss();
								} else {
									setText(textViewLoginTitle, "Incorrect\nusername/password");
								}
							}
						};
						Thread netThread = new Thread(r);
						netThread.start();

					}

				});

				// Change color of button when pressed
				cancel.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch(event.getAction() & MotionEvent.ACTION_MASK) {
						case MotionEvent.ACTION_DOWN:
							cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_darker));
							break;
						case MotionEvent.ACTION_UP:
							cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
							break;
						}
						return false;
					}
				});

				// Change color of button when pressed
				confirm.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction() & MotionEvent.ACTION_MASK) {
						case MotionEvent.ACTION_DOWN:
							confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_darker));
							break;
						case MotionEvent.ACTION_UP:
							confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
							break;
						}
						return false;
					}
				});

				// Set button background to match dialog
				if (cancel != null) {
					cancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
					cancel.setTextColor(Color.WHITE);
					cancel.invalidate();
				}

				// Set button background to match dialog
				if (confirm != null) {
					confirm.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dark));
					confirm.setTextColor(Color.WHITE);
					confirm.invalidate();
				}
			}
		});



		return dialog;
	}

	private void setText(final TextView v, final String m) {
		main.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				v.setText(m);
			}

		});
	}
}
