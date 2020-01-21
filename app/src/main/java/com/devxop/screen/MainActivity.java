package com.devxop.screen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

public class MainActivity extends Activity {
    private Handler uiHandler;
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        uiHandler = new Handler();
        myWebView = findViewById(R.id.webview);


        //setContentView(myWebView);

        simulate();


        //myWebView.loadUrl("http://devxop.ddns.net:3000/display");
    }

    // This method is to be executed on the new thread.
    public void simulate() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                myWebView.setWebViewClient(new myWebClient());
                myWebView.setWebChromeClient(new WebChromeClient());
                myWebView.getSettings().setJavaScriptEnabled(true);
                myWebView.getSettings().setDomStorageEnabled(true);

                myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

                // This is run on the UI thread.
                myWebView.loadUrl("http://devxop.ddns.net:3000/display");
            }
        });
    }

    public class myWebClient extends WebViewClient
    {

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
}
