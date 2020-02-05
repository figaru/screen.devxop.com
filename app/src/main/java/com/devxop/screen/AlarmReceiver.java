package com.devxop.screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALUM_SCREEN_ON = "screenOn";
    public static final String ALUM_SCREEN_OFF = "screenOff";
    private static final String TAG = "AlarmReceiver";
    public static final String ACTION_ALARM = "en.proft.alarms.ACTION_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "get braodcast action:" + intent.getAction());

        context.sendBroadcast(new Intent("FORCE_UPDATE"));
    }
}