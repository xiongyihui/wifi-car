package cn.xiongyihui.wificar;

import java.io.IOException;

import android.graphics.Bitmap;
import android.util.Log;


public class MjpegThread extends Thread {
	public final static String TAG = "WiFi Car";
	
	private String mjpegUrl;
	private Callback onFrameReadCallback;
	
	private boolean run;
	
	public interface Callback {
		public void onFrameRead(Bitmap bitmap);
	}
	
	public MjpegThread(String name) {
		super(name);
		
//		mjpegUrl = null;
		mjpegUrl = "http://192.168.1.1:8080/?action=stream";
		onFrameReadCallback = null;
		run = false;
	}
	
	@Override
	public void start() {
		super.start();
		
		run = true;
	}
	
	public void exit() {
		run = false;
		
	}
	
	public void setMjpegSource(String url) {
		mjpegUrl = url;
	}
	
	public void setCallback(Callback callback) {
		onFrameReadCallback = callback;
	}
	
	public void run() {
		MjpegInputStream mjpegStream = null;
		
		try {
			mjpegStream = MjpegInputStream.get(mjpegUrl);
		} catch (Exception e) {
			Log.v(TAG, e.getMessage());
			
			return;
		}
		
		while (run) {
			Bitmap bitmap = null;
			
			try {
				bitmap = mjpegStream.readFrame();
			} catch (IOException e) {
				Log.v(TAG, e.getMessage());
				
				return;
			}
			
			if (onFrameReadCallback != null) {
				onFrameReadCallback.onFrameRead(bitmap);
			}
		}
		
		try {
			mjpegStream.close();
		} catch (IOException e) {
			Log.v(TAG, e.getMessage());
		}
	}

}
