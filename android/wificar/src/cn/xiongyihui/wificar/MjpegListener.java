package cn.xiongyihui.wificar;

import android.graphics.Bitmap;


public interface MjpegListener {
	public void onFrameRead(Bitmap bitmap);
}
