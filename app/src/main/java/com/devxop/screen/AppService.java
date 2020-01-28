package com.devxop.screen;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.devxop.screen.App.AppConfig;
import com.devxop.screen.Helper.StorageManager;

import org.json.JSONObject;

import java.util.List;

public class AppService extends Service {
    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new AppService.MyBinder();
    private Chronometer mChronometer;

    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
    private static final String ACTION_STRING_UPDATE = "forceUpdate";

    private ActivityManager activityManager;

    //STEP1: Create a broadcast receiver
    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "received message in service..!", Toast.LENGTH_SHORT).show();
            Log.d("Service", "Sending broadcast to activity");
            sendBroadcast();
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(true){
                /*Log.d("SERVICE", "###############################################################################");
                Log.d("SERVICE", "SERVICE RUNNING");*/

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                StringRequest syncRequest = new StringRequest(Request.Method.GET, AppConfig.URL_UPDATE + "?device_id=" + StorageManager.Get(getApplicationContext(), "device_id"),
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                try{
                                    JSONObject jObj = new JSONObject(response);
                                    int code = jObj.getInt("code");
                                    boolean update = jObj.getBoolean("data");
                                    // Check for error node in json
                                    if (code == 200) {
                                        // response

                                        if(update == true || AppConfig.requires_restart){
                                            Log.d("FORCE UPDATE", "FORCING UPDATE -> Connection");
                                            //onConnectionChange(true);
                                            //forceUpdate();

                                            sendForceUpdate();
                                        }


                                    }else{

                                    }
                                }catch (Exception ex){
                                    Log.d("EXCPETION PING", ex.toString());
                                    AppConfig.requires_restart = true;
                                }


                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO Auto-generated method stub
                                AppConfig.requires_restart = true;
                            }
                        }
                );

                // Adding request to request queue
                queue.add(syncRequest);


                sendBroadcast();
                try{
                    Thread.sleep(15000);
                }catch(Exception ex){
                    Log.d("SERVICE",ex.toString());

                }

            }
        }
    };

    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
        return tasks.get(0).processName;

        /*for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;*/
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");

        // start new thread and you your work there
        new Thread(runnable).start();

//        // prepare a notification for user and start service foreground
//        Notification notification = ...
//        // this will ensure your service won't be killed by Android
//        startForeground(R.id.notification, notification);

        //STEP2: register the receiver
        if (serviceReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_SERVICE"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
//Map the intent filter to the receiver
            registerReceiver(serviceReceiver, intentFilter);
        }

        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
        //STEP3: Unregister the receiver
        unregisterReceiver(serviceReceiver);
        mChronometer.stop();
    }

    private void sendBroadcast() {
        Intent new_intent = new Intent();
        new_intent.setAction(ACTION_STRING_ACTIVITY);
        sendBroadcast(new_intent);
    }

    private void sendForceUpdate() {
        Intent new_intent = new Intent();
        new_intent.setAction(ACTION_STRING_UPDATE);
        sendBroadcast(new_intent);
    }

    public class MyBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }
}