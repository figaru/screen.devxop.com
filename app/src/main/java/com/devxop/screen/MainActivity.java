package com.devxop.screen;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity implements NetworkChangeReceiver.ConnectionChangeCallback {

    private Handler uiHandler;
    private WebView myWebView;

    private WebView myWebViewVideo;

    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        uiHandler = new Handler();
        myWebView = findViewById(R.id.webview);
        myWebViewVideo = findViewById(R.id.webviewVideo);


        //SET WEBVIEW FOR VIDEO
        myWebViewVideo.setWebViewClient(new myWebClientVideo());
        myWebViewVideo.setWebChromeClient(new WebChromeClient());
        myWebViewVideo.getSettings().setJavaScriptEnabled(true);
        myWebViewVideo.getSettings().setDomStorageEnabled(true);

        //myWebView.getSettings().setAllowFileAccess(true);
        myWebViewVideo.getSettings().getAllowFileAccess();
        myWebViewVideo.getSettings().getAllowFileAccessFromFileURLs();
        myWebViewVideo.getSettings().getAllowUniversalAccessFromFileURLs();
        myWebViewVideo.getSettings().getAllowContentAccess();

        myWebViewVideo.getSettings().setMediaPlaybackRequiresUserGesture(false);

        //SET MAIN WEBVIEW
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        myWebView.setWebViewClient(new myWebClient());
        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);

        //myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().getAllowFileAccess();
        myWebView.getSettings().getAllowFileAccessFromFileURLs();
        myWebView.getSettings().getAllowUniversalAccessFromFileURLs();
        myWebView.getSettings().getAllowContentAccess();

        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        IntentFilter intentFilter = new
                IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

        registerReceiver(networkChangeReceiver, intentFilter);

        networkChangeReceiver.setConnectionChangeCallback(this);

    }

    @Override
    public void onConnectionChange(boolean isConnected) {

        if(isConnected){

            myWebViewVideo.post(new Runnable() {
                @Override
                public void run() {
                    myWebViewVideo.loadUrl("");
                    myWebViewVideo.setVisibility(View.GONE);
                }
            });

            myWebView.post(new Runnable() {
                @Override
                public void run() {
                    myWebView.setVisibility(View.VISIBLE);
                    myWebView.reload();
                    myWebView.loadUrl("http://devxop.com:3000/display");
                }
            });

        }
        else{
            // will be called when internet is gone.

            //myWebView.setVisibility();
            myWebView.setVisibility(View.GONE);

            String videoUrl = "file:///" +
                    Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/video.mp4";

            String html = "<html style='background-color: #222;'> <header></header> <body> <video style='width: 100%; height: 100%; image-rendering: optimizeQuality; background-repeat: no-repeat; background-position: center; background-clip: content-box; background-size: cover; display: block; position: fixed; top: 0; bottom: 0; position: fixed; right: 0; bottom: 0; height: 100%; width: 100%; top: 0; left: 0; object-fit: fill;' autoplay muted loop src='" + videoUrl + "' poster='file:///android_asset/dev_banner.jpg'> </video> </body> </html>";

            //myWebViewVideo.loadUrl(videoUrl);
            myWebViewVideo.setVisibility(View.VISIBLE);
            myWebViewVideo.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }


    public class myWebClientVideo extends WebViewClient {

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
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            view.loadUrl("about:blank");

        }


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

            myWebViewVideo.post(new Runnable() {
                @Override
                public void run() {
                    myWebViewVideo.loadUrl("");
                    myWebViewVideo.setVisibility(View.GONE);
                }
            });
        }

        @JavascriptInterface
        public void requestVideo() {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {

                    //myWebView.setVisibility();
                    myWebView.setVisibility(View.GONE);

                    String videoUrl = "file:///" +
                            Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/video.mp4";

                    String html = "<html> <header></header> <body> <video style='width: 100%; height: 100%; image-rendering: optimizeQuality; background-repeat: no-repeat; background-position: center; background-clip: content-box; background-size: cover; display: block; position: fixed; top: 0; bottom: 0; position: fixed; right: 0; bottom: 0; height: 100%; width: 100%; top: 0; left: 0; object-fit: fill;' autoplay muted loop src='" + videoUrl + "'> </video> </body> </html>";

                    //myWebViewVideo.loadUrl(videoUrl);
                    myWebViewVideo.setVisibility(View.VISIBLE);
                    myWebViewVideo.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
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
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
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

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

                myWebView.post(new Runnable() {
                    @Override
                    public void run() {

                        String videoUrl = "file:///" +
                                Environment.getExternalStorageDirectory().getAbsolutePath() +
                                "/video.mp4";

                        MainActivity.this.myWebView.evaluateJavascript("videoComplete('" + videoUrl + "');", null);
                    }
                });

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());

                myWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        myWebView.setVisibility(View.VISIBLE);
                        myWebView.loadUrl("http://devxop.com:3000/display");
                    }
                });

                myWebViewVideo.post(new Runnable() {
                    @Override
                    public void run() {
                        myWebViewVideo.loadUrl("");
                        myWebViewVideo.setVisibility(View.GONE);
                    }
                });
            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

        }

    }
}
