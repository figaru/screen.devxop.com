package com.devxop.screen.Helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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

public class API {

    public static void scheduleRequest(Context context, final String action, final String value, final String valueType){
        final String device_id = StorageManager.Get(context, "device_id");
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SCHEDULE + "?device_id=" + device_id, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    int code = jObj.getInt("code");

                    // Check for error node in json
                    if (code == 200) {

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
        queue.add(strReq);
    }
}
