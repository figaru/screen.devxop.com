package com.devxop.screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.devxop.screen.App.AppConfig;
import com.devxop.screen.App.AppController;
import com.devxop.screen.App.ValidateServer;
import com.devxop.screen.Helper.StorageManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class SyncActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sync);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest syncRequest = new StringRequest(Request.Method.GET, AppConfig.URL_UPDATE,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("SyncActivity", "IS CONNECTED");
                        Toast.makeText(getApplicationContext(), "Server reached!", Toast.LENGTH_LONG).show();
                        try{
                            Sync();
                        }catch (Exception ex){
                            Intent intent = new Intent(SyncActivity.this,
                                    LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                },
                new Response.ErrorListener()
                {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        AppConfig.requires_restart = true;
                        //no internet connection to server
                        Toast.makeText(getApplicationContext(),"No connection to server. Playing backup video.",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SyncActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );

        // Adding request to request queue
        queue.add(syncRequest);




    }


    //JSONObject auth_data = null;
    private void Sync() throws JSONException {
        // Tag used to cancel the request
        String tag_string_req = "req_sync";
        final String device_id = StorageManager.Get(getApplicationContext(), "device_id");

        Log.d("Synchornizing setting","Logging in ...");
        //showDialog();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest syncRequest = new StringRequest(Request.Method.POST, AppConfig.URL_SYNC,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jObj = new JSONObject(response);
                            int code = jObj.getInt("code");

                            Log.d("Response", response);

                            // Check for error node in json
                            if (code == 200) {
                                // response
                                Log.d("Response", response);

                                // Launch main activity
                                Intent intent = new Intent(SyncActivity.this,
                                        MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Intent intent = new Intent(SyncActivity.this,
                                        LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }catch (Exception ex){
                            Log.d("Sync", ex.toString());
                            Intent intent = new Intent(SyncActivity.this,
                                    LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR","error => "+error.toString());
                        Intent intent = new Intent(SyncActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("device_id", device_id);
                return params;
            }
        };

        // Adding request to request queue
        queue.add(syncRequest);
    }

}
