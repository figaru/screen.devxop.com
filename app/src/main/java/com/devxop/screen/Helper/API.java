package com.devxop.screen.Helper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.devxop.screen.App.AppConfig;
import com.devxop.screen.LoginActivity;
import com.devxop.screen.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class API {

    private static RequestQueue requestQueue;

    public static void setRequestQueue(Context context, StringRequest s) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            //Build.logError("Setting a new request queue");
            System.out.println("setting s a new request queue...");
        }

        s.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(s);
    }

    public static void scheduleRequest(Context context, final String action, final String value, final String valueType){
        final String device_id = StorageManager.Get(context, "device_id");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SCHEDULE + "?device_id=" + device_id, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                /*try {
                    JSONObject jObj = new JSONObject(response);
                    int code = jObj.getInt("code");

                    // Check for error node in json
                    if (code == 200) {

                    } else {

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }*/

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("action", action);
                params.put("value", value);
                params.put("value_type", valueType);

                return params;
            }

        };

        // Adding request to request queue
        setRequestQueue(context, strReq);
    }

    private static long startTime = 0;
    private static long endTime = 0;

    public static void serverTime(final Context context){
        final String device_id = StorageManager.Get(context, "device_id");

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_TIME + "?device_id=" + device_id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                endTime = System.currentTimeMillis();

                final long delay = endTime - startTime;
                try {
                    JSONObject jObj = new JSONObject(response);
                    long serverTime = jObj.getLong("data");

                    // Check for error node in json
                    if (serverTime > 0) {
                        System.out.println("-> SERVER TIME (MS): " + serverTime);
                        final long realServerTimeDelay = serverTime - startTime;
                        System.out.println("-> SERVER REAL TIME DELAY (MS): " + realServerTimeDelay);

                        final long realServerTime = serverTime + realServerTimeDelay;
                        System.out.println("-> SERVER REAL TIME(MS): " + realServerTime);

                        final long realAppTime = (realServerTime - startTime);
                        System.out.println("-> REAL APP DELAY (MS): " + realAppTime);
                        //Toast.makeText(context,"APP TIME DIFF FROM SERVER: (MS)" + realAppTime,Toast.LENGTH_LONG).show();

                        StorageManager.Set(context, "time_delay", ""+realAppTime);
                        //System.out.println("-> APP DELAY (MS): " + startTime);
                    } else {

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        startTime = System.currentTimeMillis();
        // Adding request to request queue
        setRequestQueue(context, strReq);
    }
}
