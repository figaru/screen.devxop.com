package com.devxop.screen;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.devxop.screen.App.AppConfig;
import com.devxop.screen.App.ValidateServer;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {

    private Handler uiHandler;
    private WebView myWebView;
    private String device_id;

    private WebView myWebViewVideo;
    private VideoView videoView;

    private String url = "";

    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //videoView = (VideoView)findViewById(R.id.VideoView);
        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);

        device_id = StorageManager.Get(getApplicationContext(), "device_id");

        url = "http://devxop.ddns.net:3000/api/display?device_id=" + device_id;

        uiHandler = new Handler();
        myWebView = findViewById(R.id.webview);


        //SET MAIN WEBVIEW
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        myWebView.setWebViewClient(new myWebClient());
        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);

        myWebView.getSettings().getAllowFileAccess();
        myWebView.getSettings().getAllowFileAccessFromFileURLs();
        myWebView.getSettings().getAllowUniversalAccessFromFileURLs();
        myWebView.getSettings().getAllowContentAccess();

        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        forceUpdate();
        doPing();
    }

    private void doPing() {

        //Log.d("PINGIN", "DOING PING");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest syncRequest = new StringRequest(Request.Method.GET, AppConfig.URL_UPDATE + "?device_id=" + device_id,
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
                                    forceUpdate();
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doPing();
            }
        }, 15*1000);
    }

    public void forceUpdate(){
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl(url);
            }
        });
    }

    public void playVideo(){
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                String videoUrl = "file:///" +
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/video.mp4";

                String html = "<html style='padding: 0; margin: 0;'> <header></header> <body style='padding: 0; margin: 0;'> <video style='width: 100%; height: 100%; image-rendering: optimizeQuality; background-repeat: no-repeat; background-position: center; background-clip: content-box; background-size: cover; display: block; position: fixed; top: 0; bottom: 0;' autoplay loop muted src='" + videoUrl + "'> </video> </body> </html>";


                myWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

            }
        });
    }


    public class myWebClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError er) {
            handler.proceed();
            // Ignore SSL certificate errors
        }


    }

    /**
     * Showing Dialog
     */

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }


    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void downloadVideo(String url) {
            Log.d("JAVASCRIIPT", "REQUESTING VIDEO DOWNLOAD" + url);

            new DownloadFileFromURL().execute(url);
        }

        @JavascriptInterface
        public void resetWebview() {
            myWebView.post(new Runnable() {
                @Override
                public void run() {
                    myWebView.setVisibility(View.VISIBLE);
                }
            });
        }

        @JavascriptInterface
        public void requestVideo() {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {

                    playVideo();
                }
            });

        }


    }

    /**
     * Background Async Task to download file
     */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            //Toast.makeText(getApplicationContext(),"Download video...",Toast.LENGTH_LONG).show();
            int count;

            String checkUrl = f_url[0].toString();
            String storedVideo = StorageManager.Get(getApplicationContext(), "stored_video");

            if(storedVideo == checkUrl){
                Log.d("Download Video", "Video already locally stored");
                playVideo();
            }else{
                Log.d("Download Video", "Video download required...");

                try {
                    URL url = new URL(f_url[0]);
                    URLConnection conection = url.openConnection();
                    conection.connect();

                    // this will be useful so that you can show a tipical 0-100%
                    // progress bar
                    int lenghtOfFile = conection.getContentLength();

                    if(lenghtOfFile < 1000){
                        return "";
                    }

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    File dir = Environment.getExternalStorageDirectory();
                    String path = dir.getAbsolutePath();

                    Log.d("PATH FILE: ", path);

                    if(dir.exists()){
                        File from = new File(dir,"video.mp4");
                        Log.d("FILE CHECK", from.getAbsolutePath());
                        if(from.exists()){
                            from.delete();
                        }

                    }

                    // Output stream
                    OutputStream output = new FileOutputStream(path.toString()
                            + "/video.mp4");

                    Log.d("DOWNLOAD FILE", "LOC: " + path.toString()
                            + "/video.mp4");

                    byte data[] = new byte[1024];

                    long total = 0;
                    int increment = 10;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                        int progress = (int) ((total * 100) / lenghtOfFile);

                        if((progress / increment) > 1){
                            increment += 10;
                            Log.d("DOWNLOAD FILE", "" + progress);
                        }



                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                    StorageManager.Set(getApplicationContext(), "stored_video", checkUrl);

                    playVideo();

                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                    forceUpdate();
                }
            }



            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            //dismissDialog(progress_bar_type);

        }

    }
}
