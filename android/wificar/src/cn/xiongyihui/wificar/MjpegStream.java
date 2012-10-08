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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MjpegStream implements Runnable {
    public final static String TAG = WifiCarActivity.TAG;

    private final String CONTENT_TYPE_PREFIX = "multipart/x-mixed-replace;boundary=";
    private final String CONTENT_LENGTH = "Content-Length";
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    
    private String mBoundary;
    private String mUrl;
    private boolean mRun;
    private Callback onFrameReadCallback;
    
    public interface Callback {
        public void onFrameRead(Bitmap bitmap);
    }
    
    public MjpegStream(String url) {
        mUrl = url;
        mRun = false;
        onFrameReadCallback = null;
    }
    
    public void start() {
        mRun = true;
        new Thread(this, "MJPEG").start();
    }
    
    public void stop() {
        mRun = false;
    }
    
    public void setCallback(Callback callback) {
        onFrameReadCallback = callback;
    }
    
    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c;
        for(int i=0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if(c == sequence[seqIndex]) {
                seqIndex++;
                if(seqIndex == sequence.length) 
                    return i + 1;
            } else seqIndex = 0;
        }
        return -1;
    }

    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }   

    public Bitmap readFrame(DataInputStream in) throws IOException {
        int mContentLength = -1;
        
        in.mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(in, SOI_MARKER);
        in.reset();
        byte[] header = new byte[headerLen];
        in.readFully(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) { 
            mContentLength = getEndOfSeqeunce(in, EOF_MARKER); 
        }
        in.reset();
        byte[] frameData = new byte[mContentLength];
        in.skipBytes(headerLen);
        in.readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
    
    public void run() {
        URI uri = URI.create(mUrl);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(new HttpGet(uri));
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
            
            return;
        }
        
        HttpEntity httpEntity = httpResponse.getEntity();
        String contentType = httpEntity.getContentType().getValue();
        if (!contentType.startsWith(CONTENT_TYPE_PREFIX)) {
            Log.v(TAG, "Content-Type: " + contentType);
            Log.v(TAG, mUrl + " is not MJPEG format");
            
            return;
        }
        
        mBoundary = contentType.substring(CONTENT_TYPE_PREFIX.length());
        
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(httpEntity.getContent(), FRAME_MAX_LENGTH);
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
            
            return;
        }
        
        DataInputStream mjpeg = new DataInputStream(in);
        
        while (mRun) {
            Bitmap bitmap = null;
            
            try {
                bitmap = readFrame(mjpeg);
            } catch (IOException e) {
                Log.v(TAG, e.getMessage());
                
                break;
            }
            
            if (onFrameReadCallback != null) {
                onFrameReadCallback.onFrameRead(bitmap);
            }
        }
        
        try {
            mjpeg.close();
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
        }
    }
}
