/**
 * Copyright (C) 2012 Yihui Xiong
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110, USA
 */

package cn.xiongyihui.wificar;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class Car implements Runnable {
    public final String TAG = WifiCarActivity.TAG;
    
    public final static int STATE_STOP  = 0;
    public final static int STATE_FORWARD = 1;
    public final static int STATE_BACKWARD = 2;
    public final static int STATE_TURN_LEFT = 3;
    public final static int STATE_TURN_RIGHT = 4;
    public final static int STATE_UNCONNECTED = -2;
    public final static int STATE_UNKOWN = -1;
    
    /*
     * Car's IP address
     */
    private final static String DEFAULT_IP = "192.168.1.1";
    private String mIp;
    
    private int mTargetState;
    private int mState;
    private boolean mStarted;
    
    private ArrayList<String> mCommandList;
    
    private Handler mHandler;
    
    public Car() {
        this(DEFAULT_IP);  
    }
    
    public Car(String ip) {
        mIp = ip;
        
        mState = STATE_UNCONNECTED;
        mTargetState = STATE_FORWARD;
        mStarted = false;
        
        mCommandList = new ArrayList<String>();
        
        mCommandList.add("z");
        mCommandList.add("w");
        mCommandList.add("s");
        mCommandList.add("a");
        mCommandList.add("d"); 
        
        new Thread(this).start();
    }
    
    public void start() {
        mStarted = true;
        changeTo(mTargetState);
    }
    
    public void stop() {
        changeTo(STATE_STOP);
        mStarted = false;
    }
    
    public void forward() {
        mTargetState = STATE_FORWARD;
        changeTo(STATE_FORWARD);
    }
    
    public void backward() {
        mTargetState = STATE_BACKWARD;
        changeTo(STATE_BACKWARD);
    }
    
    public void turnLeft() {
        mTargetState = STATE_TURN_LEFT;
        changeTo(STATE_TURN_LEFT);
    }
    
    public void turnRight() {
        mTargetState = STATE_TURN_RIGHT;
        changeTo(STATE_TURN_RIGHT);
    }
    
    public void pause() {
        changeTo(STATE_STOP);
    }
    
    public void setIp(String ip) {
        mIp = ip;
    }
    
    public String getIp() {
        return mIp;
    }
    
    public int getState() {
        return mState;
    }

    public void run() {
        Looper.prepare();
        
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                String url = "http://" + mIp + "/cgi-bin/serial?" + (String) msg.obj;
                URI uri = URI.create(url);
                
                HttpResponse httpResponse;
                DefaultHttpClient httpClient = new DefaultHttpClient();
                try {
                    httpResponse = httpClient.execute(new HttpGet(uri));
                } catch (IOException e) {
                    Log.v(TAG, "Unable to connect to car.");
                    
                    mState = STATE_UNCONNECTED;
                    
                    return;
                }
                
                char get;
                try {
                    get = (char)httpResponse.getEntity().getContent().read();
                } catch (IOException e) {
                    Log.v(TAG, "Unkown situation when connecting car.");
                    
                    mState = STATE_UNKOWN;
                    return;
                }
                
                mState = mCommandList.indexOf(Character.toString(get));
            }
        };
        
        changeTo(STATE_STOP);
        
        Looper.loop();
    }
    
    private void changeTo(int state) {
        if (!mStarted) {
            
            return;
        }
        
        if (state != mState) {
            String str = mCommandList.get(state);
            Message msg = mHandler.obtainMessage();
            msg.obj = str;
            mHandler.sendMessage(msg);
        }
    }
    
}
