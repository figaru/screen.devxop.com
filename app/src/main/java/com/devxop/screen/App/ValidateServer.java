package com.devxop.screen.App;

import android.os.AsyncTask;
import android.util.Log;

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


public class ValidateServer extends AsyncTask<Void, Void, Boolean> {

    public interface AsyncResponse {
        void processFinish(Boolean output);
    }

    public AsyncResponse delegate = null;//Call back interface

    public ValidateServer(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }


    @Override
    protected Boolean doInBackground(Void... params) {
        //do your work here
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        // use the result
        super.onPostExecute(result);
    };

}
