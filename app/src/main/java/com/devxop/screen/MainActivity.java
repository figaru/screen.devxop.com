package com.devxop.screen;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import com.devxop.screen.Helper.StorageManager;

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

            if(action.equals("forceUpdate")){
                forceUpdate();
                //Toast.makeText(getApplicationContext(), "Forcing update...!", Toast.LENGTH_SHORT).show();
            }

        }
    };

    //private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        String videoUrl = "file:///" +
                Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/video.mp4";

        videoView = (VideoView) findViewById(R.id.myvideoview);
        //mVV.setOnCompletionListener(this);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        videoView.setVideoURI(Uri.parse(videoUrl));


        device_id = StorageManager.Get(getApplicationContext(), "device_id");

        url = AppConfig.URL_DISPLAY + "?device_id=" + device_id;

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
        myWebView.getSettings().setGeolocationEnabled(true);


        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);


        forceUpdate();

        //doPing();

        //STEP2: register the receiver
        if (activityReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_ACTIVITY);
            IntentFilter intentFilter2 = new IntentFilter(ACTION_STRING_UPDATE);
//Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
            registerReceiver(activityReceiver, intentFilter2);
        }

        Intent intent = new Intent(this, AppService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
        //finish();

        //VideoPlayerActivity.this.finish();

        /*if(AppConfig.video_open){

            finish();
            AppConfig.video_open = false;
        }*/

        //finish();

        AppConfig.requires_restart = false;
        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.setVisibility(View.VISIBLE);
                myWebView.loadUrl(url);
            }
        });


        videoView.post(new Runnable() {
            @Override
            public void run() {
                videoView.setVisibility(View.INVISIBLE);
                videoView.stopPlayback();
            }
        });

        //Intent videoPlaybackActivity = new Intent(this, VideoPlayerActivity.class);

    }

    public void playVideo() {

        myWebView.post(new Runnable() {
            @Override
            public void run() {
                myWebView.setVisibility(View.INVISIBLE);
            }
        });

        videoView.post(new Runnable() {
            @Override
            public void run() {
                videoView.setVisibility(View.VISIBLE);
                videoView.start();
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

                        myWebView.post(new Runnable() {
                            @Override
                            public void run() {


                                MainActivity.this.myWebView.evaluateJavascript("updateProgress('" + progress + "')", null);
                            }
                        });


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

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
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
