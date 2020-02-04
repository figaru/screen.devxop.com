package com.devxop.screen;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.devxop.screen.App.AppConfig;
import com.devxop.screen.App.ValidateServer;
import com.devxop.screen.Helper.StorageManager;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity {

    private Handler uiHandler;
    private String device_id;

    private VideoView videoView;
    private ImageView imageView;
    private WebView webView;

    private String url = "";

    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    AppService mAppService;
    boolean mServiceBound = false;

    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
    private static final String ACTION_STRING_UPDATE = "forceUpdate";

    //STEP1: Create a broadcast receiver
    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction().toString();

            //Toast.makeText(getApplicationContext(), action, Toast.LENGTH_SHORT).show();

            if (action.equals("forceUpdate")) {
                forceUpdate();
                //Toast.makeText(getApplicationContext(), "Forcing update...!", Toast.LENGTH_SHORT).show();
            }

        }
    };

    //private VideoView videoView;

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        ///STEP2: register the receiver
        /*if (activityReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_ACTIVITY);
            IntentFilter intentFilter2 = new IntentFilter(ACTION_STRING_UPDATE);
//Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
            registerReceiver(activityReceiver, intentFilter2);
        }*/

        forceUpdate();

    }

    @Override
    public void onPause(){
        super.onPause();
    //Log.d("ACTIVITY MAIN", "unregistering Receiver");
        try {

            //Register or UnRegister your broadcast receiver here
            //unregisterReceiver(activityReceiver);
        } catch(IllegalArgumentException e) {

            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        //Log.d("ACTIVITY MAIN", "unregistering Receiver");
        try {

            //Register or UnRegister your broadcast receiver here
            //unregisterReceiver(activityReceiver);
        } catch(IllegalArgumentException e) {

            e.printStackTrace();
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        String videoUrl = "file:///" +
                Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/video.mp4";

        /* SET INIT VIEWS */
        videoView = findViewById(R.id.myvideoview);
        imageView = findViewById(R.id.myimageview);
        webView = findViewById(R.id.mywebview);
        //mVV.setOnCompletionListener(this);

        /* SET VIDEO URL INIT */
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.setVideoURI(Uri.parse(videoUrl));

        /* SET WEBVIEW INIT */
        //myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebViewClient(new myWebClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().getAllowFileAccess();
        webView.getSettings().getAllowFileAccessFromFileURLs();
        webView.getSettings().getAllowUniversalAccessFromFileURLs();
        webView.getSettings().getAllowContentAccess();
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);



        device_id = StorageManager.Get(getApplicationContext(), "device_id");

        url = AppConfig.URL_DISPLAY + "?device_id=" + device_id;

        //uiHandler = new Handler();

        doPing();
        if(AppConfig.requires_restart){
            playVideo();
        }else{
            forceUpdate();
        }




        /*Intent intent = new Intent(this, AppService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else{
            startService(intent);
        }
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);*/
    }


    private void doPing() {

        //Log.d("PINGIN", "DOING PING");

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest syncRequest = new StringRequest(Request.Method.GET, AppConfig.URL_UPDATE + "?device_id=" + device_id,
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
                                    forceUpdate();
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

        syncRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        queue.add(syncRequest);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doPing();
            }
        }, 15 * 1000);
    }

    public void forceUpdate() {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest syncRequest = new StringRequest(Request.Method.GET, AppConfig.URL_DISPLAY + "?device_id=" + device_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj = new JSONObject(response);

                            Log.d("JSON PARSE", jObj.toString());

                            int code = jObj.getInt("code");



                            // Check for error node in json
                            if (code == 200) {
                                AppConfig.requires_restart = false;
                                // response
                                JSONObject data = new JSONObject(jObj.getString("data"));

                                Log.d("JSON PARSE", data.toString());


                                //final String orientation = data.getString("orientation");
                                final String display = data.getString("display");
                                final String url = data.getString("url");


                                if(display.equals("restart")){
                                    Log.d("SYSTEM FORCE RESTART", "---------------------------------------------------------");
                                    Intent mStartActivity = new Intent(getApplicationContext(), StartupActivity.class);
                                    int mPendingIntentId = 123456;
                                    PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                                    AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                                    System.exit(0);
                                }else if (display.equals("image")) {
                                    if(!url.isEmpty()){
                                        new DownloadImageFromUrl().execute(AppConfig.URL_API + url);
                                    }

                                }else if (display.equals("video")) {
                                    if(!url.isEmpty()){
                                        new DownloadFileFromURL().execute(AppConfig.URL_API + url);
                                    }

                                }else if(display.equals("webview")){

                                    if(url.isEmpty()){
                                        final String dataInject = data.getString("code");
                                        playWebview(dataInject, true);
                                    }else{
                                        playWebview(url, false);
                                    }

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
                        Log.d("ERROR REUQEST", error.toString());
                    }
                }
        );

        syncRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        queue.add(syncRequest);

        AppConfig.requires_restart = false;



    }

    public void playWebview(final String inject, final boolean isData) {
        Log.d("WEBVIEW", "PLAYING WEBVIEW");
        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
            }
        });

        videoView.post(new Runnable() {
            @Override
            public void run() {
                videoView.stopPlayback();
                videoView.setVisibility(View.INVISIBLE);
            }
        });

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.setVisibility(View.VISIBLE);
                if(isData){
                    webView.loadDataWithBaseURL(null, inject, "text/html", "UTF-8", null);
                }else{
                    webView.loadUrl(inject);
                }

            }
        });



        AppConfig.video_open = false;
    }

    public void playImage() {

        imageView.post(new Runnable() {
            @Override
            public void run() {

                String imgUrl = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/image.jpg";

                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(BitmapFactory.decodeFile(imgUrl));

            }
        });

        videoView.post(new Runnable() {
            @Override
            public void run() {
                videoView.stopPlayback();
                videoView.setVisibility(View.INVISIBLE);

            }
        });

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.setVisibility(View.INVISIBLE);
                webView.loadUrl("about:blank");
            }
        });


        AppConfig.video_open = false;
    }

    public void playVideo() {

        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
            }
        });

        videoView.post(new Runnable() {
            @Override
            public void run() {
                String videoUrl = "file:///" +
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/video.mp4";

                videoView.setVideoPath(videoUrl);
                videoView.setVisibility(View.VISIBLE);
                videoView.start();
            }
        });

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.setVisibility(View.INVISIBLE);
                webView.loadUrl("about:blank");
            }
        });


        AppConfig.video_open = true;
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

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            // Do somethinng
            AppConfig.requires_restart = true;
            playVideo();
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
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.setVisibility(View.VISIBLE);
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
    class DownloadImageFromUrl extends AsyncTask<String, String, String> {

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
            String storedVideo = StorageManager.Get(getApplicationContext(), "stored_image");

            Log.d("STORED VIDEO LINK", storedVideo);
            Log.d("NEW VIDEO LINK", checkUrl);
            if (storedVideo.equals(checkUrl)) {
                Log.d("Download Image", "Image already locally stored");
                playImage();
            } else {
                Log.d("Download Image", "Image download required...");

                try {
                    URL url = new URL(f_url[0]);
                    /*URLConnection conection = url.openConnection();
                    conection.connect();*/

                    File dir = Environment.getExternalStorageDirectory();
                    String path = dir.getAbsolutePath();

                    Log.d("PATH FILE: ", path);

                    if (dir.exists()) {
                        File from = new File(dir, "image.jpg");
                        Log.d("FILE CHECK", from.getAbsolutePath());
                        if (from.exists()) {
                            from.delete();
                        }

                    }

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);

                    FileOutputStream stream = new FileOutputStream(path.toString()
                            + "/image.jpg");

                    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                    byte[] byteArray = outstream.toByteArray();

                    stream.write(byteArray);
                    stream.close();

                    StorageManager.Set(getApplicationContext(), "stored_image", checkUrl);

                    playImage();

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

            Log.d("STORED VIDEO LINK", storedVideo);
            Log.d("NEW VIDEO LINK", checkUrl);
            if (storedVideo.equals(checkUrl)) {
                Log.d("Download Video", "Video already locally stored");
                playVideo();
            } else {
                Log.d("Download Video", "Video download required...");

                try {
                    URL url = new URL(f_url[0]);
                    URLConnection conection = url.openConnection();
                    conection.connect();

                    // this will be useful so that you can show a tipical 0-100%
                    // progress bar
                    int lenghtOfFile = conection.getContentLength();

                    if (lenghtOfFile < 1000) {
                        return "";
                    }

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    File dir = Environment.getExternalStorageDirectory();
                    String path = dir.getAbsolutePath();

                    Log.d("PATH FILE: ", path);

                    if (dir.exists()) {
                        File from = new File(dir, "video.mp4");
                        Log.d("FILE CHECK", from.getAbsolutePath());
                        if (from.exists()) {
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

                        final int progress = (int) ((total * 100) / lenghtOfFile);

                        if ((progress / increment) > 1) {
                            increment += 10;
                            Log.d("DOWNLOAD FILE", "" + progress);
                        }

                        /*myWebView.post(new Runnable() {
                            @Override
                            public void run() {


                                MainActivity.this.myWebView.evaluateJavascript("updateProgress('" + progress + "')", null);
                            }
                        });*/


                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                    StorageManager.Set(getApplicationContext(), "stored_video", checkUrl);


                    //Thread.sleep(3000);

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

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {

                //Register or UnRegister your broadcast receiver here
                unregisterReceiver(activityReceiver);
            } catch(IllegalArgumentException e) {

                e.printStackTrace();
            }
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AppService.MyBinder myBinder = (AppService.MyBinder) service;
            mAppService = myBinder.getService();
            mServiceBound = true;
        }
    };
}
