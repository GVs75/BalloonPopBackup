package com.example.balloonpop;

//https://github.com/GVs75/BalloonPop.git


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener{
	
    // Debugging
    private static final String TAG = "Bluetooth";
    private static final boolean D = false;

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	public static final String D_ADD_BALLON = "AB";  

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    //private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    
	private SensorManager 		mSensorManager 		= null;  
	private Sensor 				mAccelerometer 		= null;
	private GameView 			mGameView 			= null;
	private BluetoothAdapter 	mBluetoothAdapter 	= null;

    private BluetoothService 	mBTService = null;
    private String 				mConnectedDeviceName = null;
    
	private boolean  			mIsBTavailable 	= false;
	private boolean  			mIsBTenabled 	= false;
	private boolean  			mIsBTconnected 	= false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
		if (tabletSize) {
			requestWindowFeature(Window.FEATURE_ACTION_BAR);  
		} else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);        
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

        mGameView = new GameView(this);
        
		setContentView(mGameView);
		
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);    
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            mIsBTavailable = false;
        }else
        	mIsBTavailable = true;
        
        
	}

    @Override
    public void onStart() {
        super.onStart();
       
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        if (mBluetoothAdapter != null) {			//Commit: Added mBluetoothAdapter check if it is null
	        if (!mBluetoothAdapter.isEnabled()) {
	            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	            mIsBTenabled = false;
	        } else {
	            if (mBTService == null) setupBTService();
	            mIsBTenabled = true;
	        }
        }
      
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem enabled 		= menu.findItem(R.id.enableBT);
	    MenuItem discover 		= menu.findItem(R.id.action_discover);
	    MenuItem discoverable 	= menu.findItem(R.id.discoverable);
	    MenuItem disconnect 	= menu.findItem(R.id.disconnect);
	
	    if(mIsBTavailable == true){
	    	enabled.setVisible(true);
	    	discover.setVisible(true);
	    	discoverable.setVisible(true);
	    	disconnect.setVisible(true);
	    } else {
	    	enabled.setVisible(false);
	    	discover.setVisible(false);
	    	discoverable.setVisible(false);
	    	disconnect.setVisible(false);
	    }
	    
    	if (mIsBTenabled == true){
    		enabled.setEnabled(false);
    		discover.setEnabled(true);
    		discoverable.setEnabled(true);
    	}
    	else {
    		enabled.setEnabled(true);
    		discover.setEnabled(false);
    		discoverable.setEnabled(true);
    	}   
    	
    	if (mIsBTconnected == true){
    		disconnect.setEnabled(true);
    		discover.setEnabled(false);
    		discoverable.setEnabled(false);
    	}
    	else {
    		disconnect.setEnabled(false);
    	}   


		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()){
		case R.id.action_exit:
			finish();
			break;
		case R.id.enableBT:
			enableBT();
			break;
		case R.id.action_discover:
			Intent serverIntent = new Intent(this, BluetoothDeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			break;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        case R.id.disconnect:
            // 
        	if (mBTService != null) mBTService.stop();
            return true;            
        }
		
		return super.onMenuItemSelected(featureId, item);
	}

	private void enableBT() {
        if (mIsBTavailable == true){
			if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	        }else
	        	mIsBTenabled = true;
        }		
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// TODO Auto-generated method stub
		mGameView.setAcceleration(arg0.values);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        // Stop the Bluetooth services
        if (mBTService != null) mBTService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		
        if(D) Log.e(TAG, "+ ON RESUME +");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
            	mBTService.start();
            }
        }
	}
	
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
        if(D) Log.i(TAG, "Connecting to:" + address);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBTService.connect(device, true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data);
            }
            break;

        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a session
                setupBTService();
            	mIsBTenabled = true;
            } else {
                // User did not enable Bluetooth or an error occurred
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                mIsBTenabled = false;
            }
        }

    }
    
    private void setupBTService() {
    	if (D) Log.d(TAG, "setupBTService()");

        // Initialize the BluetoothService to perform bluetooth connections
        mBTService = new BluetoothService(this, mHandler);

    }
    public boolean isConnected() {
        return mIsBTconnected;
    }  
    
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    public void sendMessage(String message) {
    	// Check that we're actually connected before trying anything
    	if (mBTService != null){
    		if (mBTService.getState() != BluetoothService.STATE_CONNECTED) {
    			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
    			return;
    		}

    		// Check that there's actually something to send
    		if (message.length() > 0) {
    			// Get the message bytes and tell the BluetoothService to write
    			byte[] send = message.getBytes();
    			mBTService.write(send);
    		}
    	}
    }
    /*
    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }
    */
    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                mIsBTconnected 	= false;
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    //Toast.makeText(getApplicationContext(), R.string.title_connected, Toast.LENGTH_SHORT).show();                 
                    //mConversationArrayAdapter.clear();
                    mIsBTconnected 	= true;
                    break;
                case BluetoothService.STATE_CONNECTING:
                    //setStatus(R.string.title_connecting);
                    //Toast.makeText(getApplicationContext(), R.string.title_connecting, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    //setStatus(R.string.title_not_connected);
                    //Toast.makeText(getApplicationContext(), R.string.title_not_connected, Toast.LENGTH_SHORT).show();
                    break;
                }
                break;
            case MESSAGE_WRITE:
                //byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                //String writeMessage = new String(writeBuf);
                //Toast.makeText(getApplicationContext(), "Me:  " + writeMessage, Toast.LENGTH_SHORT).show();
             
               // mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //Toast.makeText(getApplicationContext(), mConnectedDeviceName+":  " + readMessage, Toast.LENGTH_SHORT).show();
                if (readMessage.equals(D_ADD_BALLON)){
                	mGameView.addBalloon();
                }
               // mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to: " + mConnectedDeviceName,
                				Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };    
}
