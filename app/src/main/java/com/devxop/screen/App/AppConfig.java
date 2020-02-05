package com.devxop.screen.App;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by admin on 18/05/2017.
 */

public class AppConfig {

    // Server generic api url
    public static String URL_API = "http://10.0.2.2:3000";
    // Server app authenticate url
    public static String URL_REGISTER = URL_API + "/api/device/register";
    // Server app authenticate url
    public static String URL_LOGIN = URL_API + "/api/device/register";
    // Server user get details
    public static String URL_SYNC = URL_API + "/api/device/sync";

    public static String URL_DISPLAY = URL_API + "/api/display";

    public static String URL_UPDATE = URL_API + "/api/device/update";

    public static String URL_SCHEDULE = URL_API + "/api/device/schedule";

    public static Boolean requires_restart = false;

    public static Boolean video_open = false;

    private boolean connected;

    public static boolean IsReachable(Context context) {
        // First, check we have any sort of connectivity
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        boolean isReachable = false;

        if (netInfo != null && netInfo.isConnected()) {
            // Some sort of connection is open, check if server is reachable
            try {
                URL url = new URL("http://devxop.ddns.net:3000/");
                //URL url = new URL("http://10.0.2.2");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "Android Application");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(2 * 1000);
                urlc.connect();
                isReachable = (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                //Log.e(TAG, e.getMessage());
            } catch (NetworkOnMainThreadException e){

            }
        }

        return isReachable;
    }

    public static boolean isConnected() {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://devxop.ddns.net:3000/api/device/update").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (IOException e) {
            Log.e("Connected AppConfig:49", "Error: ", e);
        }
        return false;
    }

}

