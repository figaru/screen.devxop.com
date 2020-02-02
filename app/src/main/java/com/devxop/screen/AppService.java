package com.devxop.screen;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
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

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

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
            while (true) {
                /*Log.d("SERVICE", "###############################################################################");
                Log.d("SERVICE", "SERVICE RUNNING");*/

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                StringRequest syncRequest = new StringRequest(Request.Method.GET, AppConfig.URL_UPDATE + "?device_id=" + StorageManager.Get(getApplicationContext(), "device_id"),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jObj = new JSONObject(response);
                                    int code = jObj.getInt("code");
                                    boolean update = jObj.getBoolean("data");
                                    // Check for error node in json
                                    if (code == 200) {
                                        // response

                                        if (update == true || AppConfig.requires_restart) {
                                            Log.d("FORCE UPDATE", "FORCING UPDATE -> Connection");
                                            //onConnectionChange(true);
                                            //forceUpdate();

                                            sendForceUpdate();
                                        }


                                    } else {

                                    }
                                } catch (Exception ex) {
                                    Log.d("EXCPETION PING", ex.toString());
                                    AppConfig.requires_restart = true;
                                }


                            }
                        },
                        new Response.ErrorListener() {
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
                try {
                    Thread.sleep(15000);
                } catch (Exception ex) {
                    Log.d("SERVICE", ex.toString());

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setContentText("Running devxop service")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        } else {

        }


        // start new thread and you your work there
        new Thread(runnable).start();

//        // prepare a notification for user and start service foreground
//        Notification notification = ...
//        // this will ensure your service won't be killed by Android
//        startForeground(R.id.notification, notification);

        //STEP2: register the receiver


        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        if (serviceReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_SERVICE"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
//Map the intent filter to the receiver
            registerReceiver(serviceReceiver, intentFilter);
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        if (serviceReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_SERVICE"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
//Map the intent filter to the receiver
            registerReceiver(serviceReceiver, intentFilter);
        }
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        try {

            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(serviceReceiver);
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }

        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(dialogIntent);

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
        //STEP3: Unregister the receiver
        try {

            //Register or UnRegister your broadcast receiver here
            unregisterReceiver(serviceReceiver);
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);

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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    public class MyBinder extends Binder {
        public AppService getService() {
            return AppService.this;
        }
    }
}