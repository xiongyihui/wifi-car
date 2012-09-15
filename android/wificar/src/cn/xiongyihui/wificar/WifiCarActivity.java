package cn.xiongyihui.wificar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class WifiCarActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_wifi_car);
        
        if (!isNetworkConnected()) {
        	Toast.makeText(this, "Network is not connected.", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void onGoButtonClick(View view) {
    	Intent intent;
    	
    	intent = new Intent(this, DashboardActivity.class);
    	startActivity(intent);
    }
    
    public boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		return ((networkInfo != null && networkInfo.isConnected()) ? true : false);
    }
}
