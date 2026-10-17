package com.masroofi.app;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private WebView webView;

    public class AndroidBridge {
        @JavascriptInterface
        public void saveBackup(String base64, String fileName) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();

                    values.put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            fileName
                    );

                    values.put(
                            MediaStore.Downloads.MIME_TYPE,
                            "application/json"
                    );

                    values.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS
                    );

                    android.net.Uri uri =
                            getContentResolver().insert(
                                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                    values
                            );

                    if (uri == null) {
                        throw new Exception("Could not create download");
                    }

                    try (
                            OutputStream out =
                                    getContentResolver().openOutputStream(uri)
                    ) {
                        if (out == null) {
                            throw new Exception("Could not open download");
                        }

                        out.write(bytes);
                    }

                } else {
                    File dir =
                            getExternalFilesDir(
                                    Environment.DIRECTORY_DOWNLOADS
                            );

                    if (dir == null) {
                        throw new Exception("Could not access downloads");
                    }

                    File file = new File(dir, fileName);

                    try (FileOutputStream out =
                                 new FileOutputStream(file)) {
                        out.write(bytes);
                    }
                }

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "تم حفظ النسخة في مجلد التنزيلات",
                                Toast.LENGTH_LONG
                        ).show()
                );

            } catch (Exception error) {
                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "تعذر حفظ النسخة الاحتياطية",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteDatabase("webview.db");

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(
                WebSettings.LOAD_NO_CACHE
        );

        webView.addJavascriptInterface(
                new AndroidBridge(),
                "AndroidBridge"
        );

        webView.clearCache(true);

        webView.loadUrl(
                "https://naserothma-creator.github.io/masroofi-demo/?v=1.3"
        );

        setContentView(webView);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
