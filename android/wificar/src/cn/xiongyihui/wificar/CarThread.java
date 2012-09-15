package cn.xiongyihui.wificar;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class CarThread extends HandlerThread {
	public final static String TAG = "WiFi Car";
	
	public final static int STATE_STOP  = 0;
	public final static int STATE_FORWARD = 1;
	public final static int STATE_BACKWARD = 2;
	public final static int STATE_TURN_LEFT = 3;
	public final static int STATE_TURN_RIGHT = 4;
	public final static int STATE_UNCONNECTED = -1;
	public final static int STATE_UNKOWN = -2;
	
	private Handler messageHandler;
	private int carState;
	private boolean engineStarted;
	private String targetAction;

	public CarThread(String name) {
		super(name);
		
		carState = STATE_STOP;
		engineStarted = false;	
		targetAction = "w";
		
		Log.v(TAG, "Create thread - " + name);
	}
	
	@Override
	public void onLooperPrepared() {
		messageHandler = new Handler(this.getLooper()) {
			@Override
	    	public void handleMessage(Message msg) {
				String url;
				int get;
	    		
	    		url = "http://192.168.1.1/cgi-bin/serial?" + (String) msg.obj;
	    		URI uri = URI.create(url);
	    		
	    		HttpResponse httpResponse;
	    		DefaultHttpClient httpClient = new DefaultHttpClient();
	    		try {
	    			httpResponse = httpClient.execute(new HttpGet(uri));
	    		} catch (IOException e) {
	    			Log.v(TAG, "Unable to connect to car.");
	    			
	    			carState = STATE_UNCONNECTED;
	    			
	    			return;
	    		}
	    		
	    		try {
	    			get = (httpResponse.getEntity().getContent()).read();
	    		} catch (IOException e) {
	    			Log.v(TAG, "Unkown situation when connecting car.");
	    			
	    			carState = STATE_UNKOWN;
	    			return;
	    		}
	    		
	    		switch (get) {
	    		case 'w':
	    			carState = STATE_FORWARD;
	    			break;
	    		case 's':
	    			carState = STATE_BACKWARD;
	    			break;
	    		case 'a':
	    			carState = STATE_TURN_LEFT;
	    			break;
	    		case 'd':
	    			carState = STATE_TURN_RIGHT;
	    			break;
	    		default:
	    			carState = STATE_UNKOWN;
	    		}
	    		
	    		Log.v(TAG, "Car's state: " + carState);
	    	}
		};
	}

    public void startEngine() {
        engineStarted = true;
        send(targetAction);
    }
        
    public void stopEngine() {
        send("z");
        engineStarted = false;
    }
	
	public void forward() {
		targetAction = "w";
		send("w");
	}
	
	public void backward() {
		targetAction = "s";
		send("s");
	}
	public void turnLeft() {
		targetAction = "a";
		send("a");
	}
	
	public void turnRight() {
		targetAction = "d";
        send("d");
	}
	
	public void park() {
        send("z");
	}
	
	public int checkState() {
		return carState;
	}
	
	private void send(String str) {
        if (!engineStarted) {
            Log.v(TAG, "engine is not started");
            
            return;
        }

		Log.v(TAG, "send(): " + str);
		
		Message msg = messageHandler.obtainMessage();
		msg.obj = str;
		messageHandler.sendMessage(msg);
	}
}
