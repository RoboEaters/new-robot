package com.example.aroboeater;


import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class RoboEaterMain extends Activity implements IOIOLooperProvider{
	private static final String TAG = "Sample::Activity";

	private final IOIOAndroidApplicationHelper helper_ = new IOIOAndroidApplicationHelper(
			this, this);

	RoboEaterMain app;


	//UI stuff
	TextView mountX, mountY, motorPW, wheelPW,frontIR,backIR,sideRIR,sideLIR,diagRIR,diagLIR;
	FrameLayout frame;
	LinearLayout UI;
	
	//Threadings
	IOIOThread ioio_thread;

	
	//BLUETOOTH
	
	// Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mChatService = null;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private String mConnectedDeviceName = null;
    int menuSelection;
    
   
  	
	public RoboEaterMain() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}		

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = this;
		
		Log.i(TAG, "onCreate");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setLayout();

		helper_.create();
		
		
		//BLUETOOTH METHODS
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ensureDiscoverable(); //makes the device discoverable
        menuSelection = 0; //set the inital selection        
	}
	
	//0:MountX
	//1:MountY
	//2:MotorPW
	//3:WheelPW
	//4:Front IR
	//5:Diag Left IR
	//6:Diag Right IR
	//7:Side Left IR
	//8:Side Right IR
	//9:Back IR
	public void setTextFields(double[] values)
	{
		
		frontIR.setText("" + values[4]);
		diagLIR.setText("" + values[5]);
		diagRIR.setText("" + values[6]);
		sideLIR.setText("" + values[7]);
		sideRIR.setText("" + values[8]);
		backIR.setText("" + values[9]);
	}
	
	public void setLayout()
	{
		 frame = new FrameLayout(this);
		 //viewScreen = new Controller(this, mTts);

		 UI = new LinearLayout (this);
		 UI.setOrientation(LinearLayout.VERTICAL);
		 LinearLayout.LayoutParams lp;
		 lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		 UI.setLayoutParams(lp);

		 mountX = new TextView(this);
		 mountY = new TextView(this);
		 motorPW = new TextView(this);
		 wheelPW = new TextView(this);
         frontIR = new TextView(this);
         backIR = new TextView(this);
         sideRIR = new TextView(this);
         sideLIR = new TextView(this);
         diagRIR = new TextView(this);
         diagLIR = new TextView(this);

         mountX.setText("Mount-X PW");
         mountY.setText("Mount-Y PW");
         motorPW.setText("Motor PW");
         wheelPW.setText("Wheel PW");
         frontIR.setText("Front IR");
         backIR.setText("Back IR");
         sideRIR.setText("Side Right IR");
         sideLIR.setText("Side Left IR");
         diagRIR.setText("Diag Right IR");
         diagLIR.setText("Diag Left IR");
        
//        hueL.setMaxHeight(10);
//        hueL.setWidth(50);
//        hueH.setMaxHeight(10);
//        hueH.setWidth(50);
//        satL.setMaxHeight(10);
//        satL.setWidth(50);
//        satH.setMaxHeight(10);
//        satH.setWidth(50);
//        valueL.setMaxHeight(10);
//        valueL.setWidth(50);
//        valueH.setWidth(50);
//        valueH.setMaxHeight(10);

         UI.addView(mountX);
         UI.addView(mountY);
         UI.addView(motorPW);
         UI.addView(wheelPW);
         UI.addView(frontIR);
         UI.addView(backIR);
         UI.addView(sideRIR);
         UI.addView(sideLIR);
         UI.addView(diagRIR);
         UI.addView(diagLIR);

		// frame.addView(viewScreen);
		 frame.addView(UI);
		 setContentView(frame);	
	}
	
	/****************************************************** functions from IOIOActivity *********************************************************************************/

	protected IOIOLooper createIOIOLooper() {
		
		//perhaps pass in a reference to the main application so the IOIO thread can post the results
		//of the IR and PW calculations to the textViews in the main applications UI
		ioio_thread = new IOIOThread(app);
		return ioio_thread;//send them the viewscreen thread
	}

	@Override
	public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
		return createIOIOLooper();
	}

	  private void setupChat() {
	        Log.d(TAG, "setupChat()");
	        // Initialize the BluetoothService to perform bluetooth connections
	        mChatService = new BluetoothService(this, mHandler);
	        // Initialize the buffer for outgoing messages
	        mOutStringBuffer = new StringBuffer("");
	    }
	  
	  private final Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MESSAGE_STATE_CHANGE:
	                if(true) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
	                switch (msg.arg1) {
	                case BluetoothService.STATE_CONNECTED:           
	                    break;
	                case BluetoothService.STATE_CONNECTING:
	                    break;
	                case BluetoothService.STATE_LISTEN:
	                case BluetoothService.STATE_NONE:
	                    break;
	                }
	                break;
	            case MESSAGE_READ:
	                byte[] readBuf = (byte[]) msg.obj;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                try
	                {
	                	 if (!(readMessage == null)) 
	                		 menuSelection = Integer.parseInt(readMessage);
	                }
	                catch (NumberFormatException e)
	                {
	                	Log.i("bluetooth pass", "Not a valid number");
	                }
	                Log.d("REMOVED CODE", "was trying to use bluetoothSelect method");
	                //bluetoothSelect();
	                Log.i("Them: " + mConnectedDeviceName, readMessage);
	                break;
	            case MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to "
	                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	            case MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	            }
	        }
	    };
	    
	    private void connectDevice(Intent data, boolean secure) {
	        // Get the device MAC address
	        String address = data.getExtras()
	            .getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
	        // Get the BLuetoothDevice object
	        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	        // Attempt to connect to the device
	        mChatService.connect(device, secure);
	    }
	    
	 private void ensureDiscoverable() {
	        if(true) Log.d(TAG, "ensure discoverable");
	        if (mBluetoothAdapter.getScanMode() !=
	            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
	            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	            startActivity(discoverableIntent);
	        }
	    }
	    @Override
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if(true) Log.d(TAG, "onActivityResult " + resultCode);
	        switch (requestCode) {
	        case REQUEST_CONNECT_DEVICE_SECURE:
	            // When BluetoothDeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                connectDevice(data, true);
	            }
	            break;
	        case REQUEST_CONNECT_DEVICE_INSECURE:
	            // When BluetoothDeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                connectDevice(data, false);
	            }
	            break;
	        case REQUEST_ENABLE_BT:
	            // When the request to enable Bluetooth returns
	            if (resultCode == Activity.RESULT_OK) {
	                // Bluetooth is now enabled, so set up a chat session
	                setupChat();
	            } else {
	                // User did not enable Bluetooth or an error occured
	                Log.d(TAG, "BT not enabled");
	                //Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
	                finish();
	            }
	        }
	    }
	    
		//BLUETOOTH
		   @Override
		public synchronized void onResume() {
		        super.onResume();
		        if(true) Log.e(TAG, "+ ON RESUME +");

		        // Performing this check in onResume() covers the case in which BT was
		        // not enabled during onStart(), so we were paused to enable it...
		        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		        if (mChatService != null) {
		            // Only if the state is STATE_NONE, do we know that we haven't started already
		            if (mChatService.getState() == BluetoothService.STATE_NONE) {
		              // Start the Bluetooth chat services
		              mChatService.start();
		            }
		        }
		    }
		   
	@Override
	protected void onStop() {
		helper_.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		helper_.destroy();
		
		super.onDestroy();
		 if (mChatService != null) mChatService.stop();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
        if(true) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
	        }
		helper_.start();
		}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
			helper_.restart();
		}
	}
		
}
