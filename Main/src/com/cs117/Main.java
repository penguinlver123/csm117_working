package com.cs117;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.cs117.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class Main extends Activity implements SensorEventListener {
	
	private float mLastX, mLastY, mLastZ;
	private final int timeout = 500;
	private boolean mInitialized;
	private String moveDir;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float pHorNOISE = (float) 6.0;
    private final float pVerNOISE = (float) 15.0;
    private final float nHorNOISE = (float) -6.0;
    private final float nVerNOISE = (float) -10.0;
    private final String up = "u\n";
    private final String down = "j\n";
    private final String left = "a\n";
    private final String right = "d\n";
    private final String forward = "w\n";
    private final String backward = "s\n";
    private final String takeoff = "t\n";
    private final String land = "l\n";
    private final String none = "x\n";

	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null; 
	
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestampPrev;
	private float timestampCurr;
	private float[] mValuesMagnet      = new float[3];
    private float[] mValuesAccel       = new float[3];
    private float[] mValuesOrientation = new float[3];
    private float[] mRotationMatrix    = new float[9];
    private final float leftRange = -0.5f;
    private final float rightRange = 0.5f;
    private final float backRange = -0.5f;
    private final float forwardRange = 0.5f;
    private final float leftRangeU = (float) -(5.0f*Math.PI/12.0f);
    private final float rightRangeU = (float) (5.0f*Math.PI/12.0f);
    private final float rightRangeL = (float) (3.0f*Math.PI/12.0f);
    private Button upB;
    private Button downB;
    private Button flipB;
    private Button stopB;
    private Button landB;
    private Button takeoffB;
    private Switch switch_button;
    private TextView direction;
    private boolean firstTimeLeft = true;
    private boolean firstTimeRight = true;
    private boolean isGyroMode = true;
    private boolean isFlip = false;
    private boolean isUpOrDown = false;
    private String cmd = "";	
	
	// Well known SPP UUID
	private static final UUID MY_UUID =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	// Insert your server's MAC address
	private static String address = "C4:85:08:5A:1B:E3";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        
        moveDir = none;
        
        timestampPrev = 0.0f;
		timestampCurr = 0.0f;
		this.upB = (Button)this.findViewById(R.id.up);
		  this.upB.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		      System.out.println("UP");
		      isUpOrDown = true;
		      cmd = "u\n";
		    }
		  });
		  this.upB.setOnTouchListener(new OnTouchListener() {
			  @Override
			    public boolean onTouch(View v, MotionEvent event) {
	                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
	                	System.out.println("UP");
	                	isUpOrDown = true;
	                	cmd = "u\n";
	                    return true;
	                }
	                return false;
			    }
			  });
		  this.downB = (Button)this.findViewById(R.id.down);
		  this.downB.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		      System.out.println("DOWN");
		      isUpOrDown = true;
		      cmd = "j\n";
		    }
		  });
		  this.downB.setOnTouchListener(new OnTouchListener() {
			  @Override
			    public boolean onTouch(View v, MotionEvent event) {
	                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
	                	System.out.println("DOWN");
	                	isUpOrDown = true;
	                	cmd = "j\n";
	                    return true;
	                }
	                return false;
			    }
			  });
		  this.switch_button = (Switch) findViewById(R.id.switch_button);
		  this.switch_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		          if (isChecked) {
		              System.out.println("ACCL");
		              isGyroMode = false;
		          } else {
		              System.out.println("GYRO");
		              isGyroMode = true;
		          }
		      }
		  });
		  this.direction= (TextView)findViewById(R.id.direction);
		  this.flipB = (Button)this.findViewById(R.id.flip);
		  this.flipB.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		      if(isFlip) isFlip = false;
		      else isFlip = true;
		    }
		  });
		  this.stopB = (Button)this.findViewById(R.id.stop);
		  this.stopB.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		      System.out.println("STOP");
		      isUpOrDown = false;
		      cmd = "p\n";
		    }
		  });       
		  this.landB = (Button)this.findViewById(R.id.land);
		  this.landB.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		      System.out.println("LAND");
		      isUpOrDown = true;
		      cmd = "l\n";
		    }
		  });  
		  this.takeoffB = (Button)this.findViewById(R.id.takeoff);
		  this.takeoffB.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		      System.out.println("TAKEOFF");
		      isUpOrDown = true;
		      cmd = "t\n";
		    }
		  });  
        
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		CheckBTState();
		
		
        System.out.println("\n...In onStart...\n...Attempting client connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
            System.out.println("\n...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                if(btSocket.isConnected())
                    btSocket.close();
            } catch (IOException e2) {
                AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }
		
		
        //takeoff
        if(btSocket.isConnected())
        {     
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                AlertBox("Fatal Error", "In onStart() and output stream creation failed:" + e.getMessage() + ".");
            }

            // Create a data stream so we can talk to server.
            System.out.println("\n...Sending message to server...");
            
            byte[] msgBuffer = takeoff.getBytes();
            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
                msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                AlertBox("Fatal Error", msg);
            }
        }
    }
    
	private void CheckBTState() {
		// Check for Bluetooth support and then check to make sure it is turned on
		
		// Emulator doesn't support Bluetooth and will return null 
		if(btAdapter==null) {
			AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
		}
		else if(!btAdapter.isEnabled()) { 
			//Prompt user to turn on Bluetooth
			Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); 
		}

	}
	
	public void AlertBox( String title, String message ){
		new AlertDialog.Builder(this)
		.setTitle( title )
		.setMessage( message + " Press OK to exit." )
		.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
				} 
			}).show();
	}

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
  	    mSensorManager.registerListener(this, mSensorManager
  	  		    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
  	  		    SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        
        /*if (btSocket.isConnected()) {

            try {
                outStream.flush();
            } catch (IOException e) {
                AlertBox("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }

            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d("30","--Pause failed to close socket");
                AlertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
            }
        }*/
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(isGyroMode) {
			switch (event.sensor.getType()) {
	        case Sensor.TYPE_ACCELEROMETER:
	            System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
	            break;

	        case Sensor.TYPE_MAGNETIC_FIELD:
	            System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
	            break;
			}
			if(cmd != "p\n" && !isUpOrDown) cmd = "";
			TextView tvMove= (TextView)findViewById(R.id.move);
			SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet);
	        SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);
	        if(!isFlip) {
	        if(mValuesOrientation[2] <= leftRange && mValuesOrientation[2] >= leftRangeU) {
	        	System.out.println("LEFT");
	        	direction.setText("LEFT");
	        	tvMove.setText("LEFT");
	        	cmd = "a\n";
	        }
	        else if(mValuesOrientation[2] >= rightRange && mValuesOrientation[2] <= rightRangeU) {
	        	System.out.println("RIGHT");
	        	direction.setText("RIGHT");
	        	tvMove.setText("RIGHT");
	        	cmd = "d\n";
	        }
	        else if(mValuesOrientation[1] <= backRange) {
	        	System.out.println("BACK");
	        	direction.setText("BACK");
	        	tvMove.setText("BACK");
	        	cmd = "s\n";
	        }
	        else if(mValuesOrientation[1] >= forwardRange) {
	        	System.out.println("FORWARD");
	        	direction.setText("FORWARD");
	        	tvMove.setText("FORWARD");
	        	cmd = "w\n";
	        }
	        } else {
	        if(mValuesOrientation[2] < leftRangeU) {
	        	if(firstTimeLeft) {
	        		timestampPrev = event.timestamp * NS2S;
	        		firstTimeLeft = false;
	        	}
	        }
	        if(mValuesOrientation[2] < rightRangeU  && mValuesOrientation[2] > rightRangeL) {
	        	if(firstTimeRight) {
	        		timestampCurr = event.timestamp * NS2S;
	        		firstTimeRight = false;
	        	}
	        }
	        if(timestampPrev != 0.0f && timestampCurr != 0.0f && timestampCurr - timestampPrev < 10.0f) {
	        	System.out.println("FLIP");
	        	direction.setText("FLIP");
	        	tvMove.setText("FLIP");
	        	cmd = "f\n";
	        	firstTimeLeft = true;
	        	firstTimeRight = true;
	        	timestampPrev = 0.0f;
	        	timestampCurr = 0.0f;
	        }
	        if(timestampPrev != 0.0f && timestampCurr != 0.0f && timestampCurr - timestampPrev >= 10.0f) {
	        	firstTimeLeft = true;
	        	firstTimeRight = true;
	        	timestampPrev = 0.0f;
	        	timestampCurr = 0.0f;
	        }
	   }
	        TextView tvX= (TextView)findViewById(R.id.x_axis);
			TextView tvY= (TextView)findViewById(R.id.y_axis);
			TextView tvZ= (TextView)findViewById(R.id.z_axis);
			tvX.setText(Float.toString(mValuesOrientation[2]));
			tvY.setText(Float.toString(mValuesOrientation[1]));
			tvZ.setText(Float.toString(mValuesOrientation[0]));
			
	        if(cmd != "" && btSocket.isConnected())
	        {  
	            try {
	                outStream = btSocket.getOutputStream();
	            } catch (IOException e) {
	                AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
	            }

	            // Create a data stream so we can talk to server.
	            System.out.println("\n...Sending message to server...");
	            
	            byte[] msgBuffer = cmd.getBytes();
	            try {
	                outStream.write(msgBuffer);
	            } catch (IOException e) {
	                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
	                if (address.equals("00:00:00:00:00:00"))
	                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
	                msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

	                AlertBox("Fatal Error", msg);
	            }
	        }
		}else {
			
		TextView tvX= (TextView)findViewById(R.id.x_axis);
		TextView tvY= (TextView)findViewById(R.id.y_axis);
		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		TextView tvMove= (TextView)findViewById(R.id.move);
		ImageView iv = (ImageView)findViewById(R.id.image);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			tvX.setText("0.0");
			tvY.setText("0.0");
			tvZ.setText("0.0");
			mInitialized = true;
		} else {
			float deltaX = (x - mLastX); 
			float deltaY = (y - mLastY);
			float deltaZ = (z - mLastZ);
			
			if ((deltaX < pHorNOISE) && (deltaX > nHorNOISE)) deltaX = (float)0.0;
			if ((deltaY < pHorNOISE) && (deltaY > nHorNOISE)) deltaY = (float)0.0;
			if ((deltaZ < pVerNOISE) && (deltaZ > nVerNOISE)) deltaZ = (float)0.0;
			
			moveDir = none;
			
			
			if (x > pHorNOISE) {
				// send RIGHT
				moveDir = right;
				System.out.println("got right");
			}
			if (x < nHorNOISE) {
				// send LEFT
				moveDir = left;
				System.out.println("got left");
			}
			if (y > pHorNOISE) {
				// send FORWARD
				moveDir = forward;
				System.out.println("got forward");
			}
			if (y < nHorNOISE) {
				// send BACKWARD
				moveDir = backward;
				System.out.println("got backward");
			}
			if ((z - 9.8) > pVerNOISE) {
				// send UP
				moveDir = takeoff;
				System.out.println("got up");
			}
			if ((z - 9.8) < nVerNOISE) {
				// send DOWN
				moveDir = down;
				System.out.println("got down");
			}
			
			
	        //if(btSocket.isConnected())
	        if(moveDir != none && btSocket.isConnected())
	        {

	            
	            try {
	                outStream = btSocket.getOutputStream();
	            } catch (IOException e) {
	                AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
	            }

	            // Create a data stream so we can talk to server.
	            System.out.println("\n...Sending message to server...");
	            
	            byte[] msgBuffer = moveDir.getBytes();
	            try {
	                outStream.write(msgBuffer);
	            } catch (IOException e) {
	                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
	                if (address.equals("00:00:00:00:00:00"))
	                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
	                msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
	                
	                AlertBox("Fatal Error", msg);
	            }
	        }
			
			/* Method 2: 
			
			if (deltaX >= 0) {
				if ((deltaZ > 0) && (deltaX > deltaZ)){
					// SEND "RIGHT"
					moveDir = right;
					System.out.println("got right");
				}
				else if ((deltaZ > 0) && (deltaX < deltaZ)){
					// SEND "UP"
					moveDir = up;
					System.out.println("got up");
				}
				else if ((deltaZ < 0) && (Math.abs(deltaZ) > deltaX)){
					// SEND "DOWN"
					moveDir = down;
					System.out.println("got down");
				}
			}			
			else if (deltaX < 0) {
				if ((deltaZ < 0) && (deltaX > deltaZ)){
					// SEND "DOWN"
					moveDir = down;
					System.out.println("got down!");
				}
				else if ((deltaZ < 0) && (deltaX < deltaZ)){
					// SEND "LEFT"
					moveDir = left;
					System.out.println("got right!");
				}
				else if ((deltaZ > 0) && (deltaZ > Math.abs(deltaX))){
					// SEND "UP"
					moveDir = up;
					System.out.println("got up!");
				}
			}
			*/
			
			mLastX = x;
			mLastY = y;
			mLastZ = z;
	
			tvX.setText(Float.toString(x));
			tvY.setText(Float.toString(y));
			tvZ.setText(Float.toString(z));
			tvMove.setText(moveDir);
			iv.setVisibility(View.VISIBLE);
			if (Math.abs(deltaX) > Math.abs(deltaZ)) {
				iv.setImageResource(R.drawable.horizontal);  // X direction DOMINATES
			} else if (Math.abs(deltaY) > Math.abs(deltaX)) {
				iv.setImageResource(R.drawable.vertical);  // Y direction DOMINATES
			} else {
				iv.setVisibility(View.INVISIBLE);
			}
		}
		}
	}
}