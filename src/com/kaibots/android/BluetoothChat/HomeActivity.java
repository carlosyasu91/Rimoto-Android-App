package com.kaibots.android.BluetoothChat;

import com.kaibotsgen.android.BluetoothChat.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class HomeActivity extends Activity{
	
	private ImageButton btnGoToControlScreen;
	private ImageButton btnGoToScanScreen;
	private Button btnGoToChangeNameScreen;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	static boolean FROMHOME = false;

	private static final int REQUEST_CONNECT_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	BluetoothChatActivity chatActivity = new BluetoothChatActivity();

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
	}
	
	@Override
	public void onStart(){
		
		super.onStart();
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} 
		
		btnGoToControlScreen = (ImageButton)findViewById(R.id.goToControlScreen);
		btnGoToControlScreen.setOnTouchListener(new View.OnTouchListener() {
				@Override
			    public boolean onTouch(View v, MotionEvent event) {
			        switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			            	Intent intent = new Intent(HomeActivity.this, BluetoothChatActivity.class);
			                HomeActivity.this.startActivity(intent);
			            	return true; // if you want to handle the touch event
			            case MotionEvent.ACTION_UP:
			        }
			        return false;
			}
		});
		
		btnGoToScanScreen = (ImageButton)findViewById(R.id.goToScanScreen);
		btnGoToScanScreen.setOnTouchListener(new View.OnTouchListener() {
				@Override
			    public boolean onTouch(View v, MotionEvent event) {
			        switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			            	Intent intent = new Intent(HomeActivity.this, BluetoothChatActivity.class);
			            	FROMHOME = true;
			                HomeActivity.this.startActivity(intent);
			            	return true; // if you want to handle the touch event
			            	
			            case MotionEvent.ACTION_UP:
			        }
			        return false;
			}
		});
		
		btnGoToChangeNameScreen = (Button)findViewById(R.id.goToChangeNameScreen);
		btnGoToChangeNameScreen.setOnTouchListener(new View.OnTouchListener() {
				@Override
			    public boolean onTouch(View v, MotionEvent event) {
			        switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			            	Intent intent = new Intent(HomeActivity.this, ChangeNameActivity.class);
			            	FROMHOME = true;
			                HomeActivity.this.startActivity(intent);
			            	return true; // if you want to handle the touch event
			            	
			            case MotionEvent.ACTION_UP:
			        }
			        return false;
			}
		});



		
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				//mChatService.start();
			}
		}
	}

	
}
