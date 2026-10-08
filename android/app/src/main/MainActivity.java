package com.masroofi.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            WebView webView = new WebView(this);

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);

            webView.setWebViewClient(new WebViewClient());

            webView.loadUrl(
                "https://naserothma-creator.github.io/masroofi-demo/"
            );

            setContentView(webView);

        } catch (Exception e) {

            TextView error = new TextView(this);
            error.setText(
                "Masroofi\n\nحدث خطأ أثناء تشغيل التطبيق.\n\n" +
                "يرجى إعادة فتح التطبيق."
            );
            error.setTextSize(20);
            error.setTextColor(Color.DKGRAY);
            error.setGravity(Gravity.CENTER);
            error.setPadding(40, 40, 40, 40);

            setContentView(error);
        }
    }
}
