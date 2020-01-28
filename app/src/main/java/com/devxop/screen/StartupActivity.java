package com.devxop.screen;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.devxop.screen.Helper.StorageManager;


public class StartupActivity extends Activity {
    boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppStartup();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("STARTING", "#####################################################################");

        AppStartup();
    }

    private void AppStartup(){

        Log.d("Screen Startup", "######################################");
        String device_id = StorageManager.Get(getApplicationContext(), "device_id");




        //device_id = "";
        //StorageManager.Set(getApplicationContext(),"device_id", "");

        if(device_id.isEmpty()){
            Log.d("device_id", "No device id -> generating one.");
            String guid = java.util.UUID.randomUUID().toString();
            StorageManager.Set(getApplicationContext(), "device_id", guid);
            device_id = guid;

            Intent intent = new Intent(StartupActivity.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            Log.d("Device_id", device_id);


                // Launching the Sync Activity
            Intent intent = new Intent(StartupActivity.this,
                    SyncActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
