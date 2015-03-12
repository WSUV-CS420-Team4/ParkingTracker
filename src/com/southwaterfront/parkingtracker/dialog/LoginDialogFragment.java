package com.southwaterfront.parkingtracker.dialog;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import android.widget.Button;
import android.widget.EditText;

import com.southwaterfront.parkingtracker.R;
import com.southwaterfront.parkingtracker.client.HttpClient;
import com.southwaterfront.parkingtracker.client.HttpClient.RequestFailedException;

/**
 * Created by Joel on 3/3/2015.
 */
public class LoginDialogFragment extends DialogFragment {
	
	private final Lock lock;
	private final Condition done;
	private boolean success;
	
	public LoginDialogFragment() {
		lock = new ReentrantLock();
		done = lock.newCondition();
		success = false;
	}
	
	public boolean waitOnResult() throws InterruptedException {
		done.await();
		return success;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

		View dialogView = inflater.inflate(R.layout.login, null);

		final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextLoginName);
		final EditText editTextPassword = (EditText) dialogView.findViewById(R.id.editTextLoginPassword);

		builder.setView(dialogView)
		.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//TODO LoginDialogFragment the user
				Runnable r = new Runnable() {

					public void run() {
						try {
							HttpClient.sendLoginRequest(editTextName.getText().toString(), editTextPassword.getText().toString());
						} catch (RequestFailedException e) {
							// TODO Auto-generated catch block
							Log.e("LoginDialog", e.getMessage(), e);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e("LoginDialog", e.getMessage(), e);
						}
						
						done.signal();
					}
				};
				Thread netThread = new Thread(r);
				netThread.start();
				// HttpClient.sendLoginRequest(username, password);

				//LocationSelectDialogFragment temp = new LocationSelectDialogFragment();
				//temp.show(getFragmentManager(), "Temp");
			}
		})

		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				done.signal();
				LoginDialogFragment.this.getDialog().cancel();
			}
		});

		AlertDialog dialog = builder.create();
		// dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(l);

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialog) {
				final Button cancel = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
				final Button confirm = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);

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
}
