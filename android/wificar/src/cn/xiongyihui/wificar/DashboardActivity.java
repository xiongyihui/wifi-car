package cn.xiongyihui.wificar;

import android.app.Activity;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DashboardActivity extends Activity 
			implements SensorEventListener, SurfaceHolder.Callback,
			OnTouchListener, MjpegThread.Callback {
	public final String TAG = "WiFi Car";
	
	private RelativeLayout layout;
	
	private SurfaceHolder surfaceHolder;
	
	private TextView hintTextView;
	
	private final int ANIMATION_DURATION = 500;
	private final float ROTATE_THRES_DEGREES = 15;
	private final float BACK_ROTATE_THRES_DEGREES = 30;
	private final float ROTATE_DEGREES = 60;
	private final float BACK_ROTATE_DEGREES = 180;
	
	private float wheelRotateDegrees;
	private ImageView wheelImageView;
	
	private GestureOverlayView gestureView;
	
	private SensorManager sensorManager;
	private Sensor gravitySensor;
	
	private CarThread car;
	
	private MjpegThread mjpegThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	SurfaceView surfaceView;
    	
        super.onCreate(savedInstanceState);
        
        /* Set window with no title bar */
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        		| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        		| WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_dashboard);
        
        layout = (RelativeLayout) findViewById(R.id.dashboardLayout);
        layout.setOnTouchListener(this);
        
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        
        hintTextView = (TextView) findViewById(R.id.hintTextView);
        
        /* get the ImageView which holds a wheel */
        wheelImageView = (ImageView) findViewById(R.id.wheelImageView);
        
        /* Initially, wheel with on rotation */
        wheelRotateDegrees = 0;
        
        gestureView = (GestureOverlayView) findViewById(R.id.gestureOverlayView);
        gestureView.setVisibility(View.INVISIBLE);
        
        /* Get gravity sensor */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        
        mjpegThread = new MjpegThread("Mjpeg Thread");
    	mjpegThread.setCallback(this);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
        
        car = new CarThread("Car Control Thread");
        car.start();										/* start car thread */
    	
    	sensorManager.registerListener(this, gravitySensor,
    									SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	sensorManager.unregisterListener(this);
    	
    	car.quit();											/* quit car thread */
    }
    
    public boolean onTouch(View view, MotionEvent event) {
//    	Log.v(TAG, "onTouch(): action - " + event.getAction());
    	
    	switch (event.getAction()) {
    	case MotionEvent.ACTION_DOWN:
    		car.startEngine();
    		
    		hintTextView.setText(R.string.rotate_hint);
    		break;
    	case MotionEvent.ACTION_UP:
    		car.stopEngine();
    		
    		hintTextView.setText(R.string.start_hint);
    		break;
    	default:
    		;
    	}
    	
    	return true;
    }
  
    public void onSensorChanged(SensorEvent event) {
    	float xValue;
    	float yValue;
    	float zValue;
    	float horizontalDegrees;
    	float verticalDegrees;
    	
//    	Log.v(TAG, "onSensorChanged()");
    	
    	xValue = event.values[0];
    	yValue = event.values[1];
    	zValue = event.values[2];
    	
    	if (zValue <= 0) {
    		/* Phone is turned over, stop car */
    		car.park();
    		
    		if (wheelRotateDegrees != 0) {
	    		steerWheel(wheelRotateDegrees, 0);
	    		wheelRotateDegrees = 0;
    		}
    		
    		return;
    	}
    	
    	/* Phone's orientation is landscape */
    	horizontalDegrees = (float) Math.toDegrees(Math.atan(yValue / zValue));
    	verticalDegrees = (float) Math.toDegrees(Math.atan(xValue / zValue));
    	
		if ((verticalDegrees > BACK_ROTATE_THRES_DEGREES)
				&& (verticalDegrees > horizontalDegrees)) {
			if (wheelRotateDegrees != BACK_ROTATE_DEGREES) {
				car.backward();
				
				steerWheel(wheelRotateDegrees, BACK_ROTATE_DEGREES);
				wheelRotateDegrees = BACK_ROTATE_DEGREES;
			}
			
			return;
    	} 
    	
    	if (horizontalDegrees > ROTATE_THRES_DEGREES) {
    		if (wheelRotateDegrees != ROTATE_DEGREES) {
	    		car.turnRight();
	    		
	    		steerWheel(wheelRotateDegrees, ROTATE_DEGREES);
	    		wheelRotateDegrees = ROTATE_DEGREES;
    		}
    	} else if (horizontalDegrees < -ROTATE_THRES_DEGREES) {
    		if (wheelRotateDegrees != -ROTATE_DEGREES) {
	    		car.turnLeft();
	    		
	    		steerWheel(wheelRotateDegrees, -ROTATE_DEGREES);
	    		wheelRotateDegrees = -ROTATE_DEGREES;
    		}
    	} else {
    		if (wheelRotateDegrees != 0) {
	    		car.forward();
	    		
	    		steerWheel(wheelRotateDegrees, 0);
	    		wheelRotateDegrees = 0;
	    	}
    	}
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	Log.v(TAG, sensor.getName() + "'s accuracy is changed.");
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.v(TAG, "surfaceCreated()");
        
    	mjpegThread.start();
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format,
    						   int width, int height) {
    	Log.v(TAG, "surfaceChanged(): format - " + format
    			+ ", width - " + width + ", height - " + height);
    	
    	
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.v(TAG, "surfaceDestroyed");
    	
    	mjpegThread.exit();
    }
    
    public void onFrameRead(Bitmap bitmap) {
    	Canvas canvas = null;
    	
    	try {
	    	canvas = surfaceHolder.lockCanvas();
	        if (canvas != null) {
	        	try {
	        		
	        		canvas.drawBitmap(bitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
	        	} catch (Exception e) {
	        		
	        	}
	        }
	        
	    }finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
    
    private void steerWheel(float fromDegrees, float toDegrees) {
    	/* Create a animation of rotation */
    	Animation rotateAnimation = new RotateAnimation(fromDegrees,toDegrees,
    			Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    	rotateAnimation.setDuration(ANIMATION_DURATION);
    	
    	/* animation stays on last image */
    	rotateAnimation.setFillAfter(true);
    	
    	wheelImageView.startAnimation(rotateAnimation);
    }
}
