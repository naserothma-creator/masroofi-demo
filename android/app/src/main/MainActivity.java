package com.masroofi.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());

        // ضروري لإظهار نوافذ التأكيد الخاصة بالحذف
        webView.setWebChromeClient(new WebChromeClient());

        webView.setBackgroundColor(Color.WHITE);

        webView.loadUrl(
                "https://naserothma-creator.github.io/masroofi-demo/"
        );

        setContentView(webView);
    }
}
