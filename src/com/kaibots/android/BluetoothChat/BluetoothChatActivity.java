/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaibots.android.BluetoothChat;


import com.kaibotsgen.android.BluetoothChat.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChatActivity extends Activity implements SensorEventListener {
	// Debugging
	private static final String TAG = "Rimoto";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Layout Views
	private ImageButton mButtonReverse;
	private Button btnRestarHP;
	private ImageButton moveServo;
	private ImageButton mForwardButton;
	int buttonpressed = 0;

	private int progressStatus = 200;
	private Handler handler = new Handler();
	private int deadflag = 0;
	
	TextView acceleration;
	Sensor accelerometer;
	SensorManager sm;
	boolean mIsRunning = true;
	
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.main);
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		setupUI();
	}

	private void setupUI() {
		
		
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer=sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        
        mForwardButton = (ImageButton) findViewById(R.id.btnFwd);
		mForwardButton.setOnTouchListener(new View.OnTouchListener() {
				@Override
			    public boolean onTouch(View v, MotionEvent event) {
			        switch(event.getAction()) {
			            case MotionEvent.ACTION_DOWN:
			            	if(deadflag == 0){
			            	sendMessage("W");
			            	buttonpressed = 1;
			            	
			                // PRESSED
			                return true; // if you want to handle the touch event
			            	}
			            case MotionEvent.ACTION_UP:
			            	sendMessage("Q");
			            	buttonpressed = 0;
			                // RELEASED
			                return true; // if you want to handle the touch event
			        }
			        return false;
			}
		});

		mButtonReverse = (ImageButton) findViewById(R.id.btnRev);
		mButtonReverse.setOnTouchListener(new View.OnTouchListener() {
			@Override
		    public boolean onTouch(View v, MotionEvent event) {
		        switch(event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	if(deadflag == 0){
		            	sendMessage("S");
		            	buttonpressed = 1;
		            	
		                // PRESSED
		                return true; // if you want to handle the touch event
		            	}
		            case MotionEvent.ACTION_UP:
		            	sendMessage("Q");
		            	buttonpressed = 0;
		                // RELEASED
		                return true; // if you want to handle the touch event
		        } 
		        return false;
		}
	});
		moveServo = (ImageButton) findViewById(R.id.buttonHit);
		moveServo.setOnTouchListener(new View.OnTouchListener() {
			@Override
		    public boolean onTouch(View v, MotionEvent event) {
		        switch(event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	if(deadflag == 0){
		            	sendMessage("H");
		            	buttonpressed = 1;
		            	
		                // PRESSED
		                return true; // if you want to handle the touch event
		            	}
		            case MotionEvent.ACTION_UP:
		            	sendMessage("Q");
		            	buttonpressed = 0;
		                // RELEASED
		                return true; // if you want to handle the touch event
		        }
		        return false;
		}
	});


        
	}

//		  new Thread(new Runnable() {
//	            public void run() {
//	               while (progressStatus > 1) {
//	                  
//	           // Update the progress bar and display the 
//	                                //current value in the text view
//	           handler.post(new Runnable() {
//	           public void run() {
//	        	   if(progressBar.getProgress() <30 ){
//	        		   deadflag = 1;
//	        		   textViewBar.setText("You're Kaibot fainted!");
//	        	   }
//	              progressBar.setProgress(progressStatus);
//	              textViewBar.setText(progressStatus+"/"+progressBar.getMax());
//	           }
//	               });
//	               try {
//	                  // Sleep for 200 milliseconds. 
//	                                //Just to display the progress slowly
//	                  Thread.sleep(100);
//	               } catch (InterruptedException e) {
//	                  e.printStackTrace();
//	               }
//	            }
//	         }
//	         }).start();
		  
	

	@Override
	public void onStart() {
		super.onStart();
		
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
		if(HomeActivity.FROMHOME == true){
			HomeActivity.FROMHOME = false;
			Intent serverIntent = null;
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
		this.mIsRunning = true;
	}

	protected void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the send button with a listener that for click events
		/*forward.setOnTouchListener(new View.OnTouchListener() {        
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        switch(event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	sendData("w");
		            	buttonpressed = 1;
		                // PRESSED
		                return true; // if you want to handle the touch event
		            case MotionEvent.ACTION_UP:
		            	sendData("q");
		            	buttonpressed = 0;
		                // RELEASED
		                return true; // if you want to handle the touch event
		        }
		        return false;
		    }
		});*/

		
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, this.mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		mIsRunning = false;
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if(mChatService !=null)
		//	mChatService.stop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
		//	mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
		}
	}

	// The action listener for the EditText widget, to listen for the return key




	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					break;
				case BluetoothChatService.STATE_CONNECTING:
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				if(readMessage.equals("p")){
					restar();
					}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.backbutton:
			BluetoothChatActivity.this.finish();
		
		}
		return false;
	}
	private void restar() {
		// TODO Auto-generated method stub
		progressStatus-=20;
	}
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
//		acceleration.setText("X: " + event.values[0] +
//				"\nY: " + event.values[1]+
//				"\nZ: " + event.values[2]
//				);
	if(mIsRunning){	
	if(buttonpressed == 0){
		if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED )
		{
		if(event.values[1]<-25 )
		 	sendMessage("D");
			else if(event.values[1]>25 )
			sendMessage("A");
			else if(event.values[1]<=25 && event.values[1] >15 )
				sendMessage("Q");
			else if(event.values[1]>=-25 && event.values[1] <-15 )
				sendMessage("Q");
		}
		}
	}	
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
