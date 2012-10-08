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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class WifiCarActivity extends Activity {
	public final static String TAG = "WiFi Car";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
        
        if (!isNetworkConnected()) {
        	Log.v(TAG, "network is not available.");
        	
            Toast.makeText(this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
        }
    }
    
    public void onGoButtonClick(View view) {
    	Intent intent = new Intent(this, DashboardActivity.class);
    	startActivity(intent);
    }
    
    public void onSettingsButtonClick(View view) {
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
    }
    
    public void onHelpButtonClick(View view) {
    	Intent intent = new Intent(this, HelpActivity.class);
    	startActivity(intent);
    }
    
    public void onAboutButtonClick(View view) {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
    }
    
    public boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        
        return ((networkInfo != null && networkInfo.isConnected()) ? true : false);
    }
}
