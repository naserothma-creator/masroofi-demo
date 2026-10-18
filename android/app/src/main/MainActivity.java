package com.masroofi.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.Toast;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;

    private static final String PREFS_NAME = "MasroofiData";
    private static final String STATE_KEY = "app_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(
                new AndroidBridge(),
                "AndroidBridge"
        );

        webView.setBackgroundColor(0xFFFFFFFF);

        // تشغيل التطبيق من داخل APK دون إنترنت
        webView.loadUrl("file:///android_asset/index.html");

        setContentView(webView);
    }

    public class AndroidBridge {

        @JavascriptInterface
        public void saveState(String data) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(STATE_KEY, data)
                    .apply();
        }

        @JavascriptInterface
        public String getState() {
            return getSharedPreferences(
                    PREFS_NAME,
                    MODE_PRIVATE
            ).getString(STATE_KEY, "");
        }

        @JavascriptInterface
        public void saveBackup(
                String base64Data,
                String originalFileName
        ) {
            try {
                byte[] fileBytes = Base64.decode(
                        base64Data,
                        Base64.DEFAULT
                );

                String time = new SimpleDateFormat(
                        "yyyy-MM-dd-HHmmss",
                        Locale.US
                ).format(new Date());

                String fileName =
                        "masroofi-backup-" + time + ".json";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveWithMediaStore(fileBytes, fileName);
                } else {
                    saveForOldAndroid(fileBytes, fileName);
                }

                showMessage(
                        "تم حفظ النسخة في Downloads/Masroofi/Backups"
                );

            } catch (Exception error) {
                showMessage(
                        "تعذر حفظ النسخة الاحتياطية"
                );
            }
        }

        private void saveWithMediaStore(
                byte[] fileBytes,
                String fileName
        ) throws Exception {

            ContentResolver resolver = getContentResolver();

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
                            + "/Masroofi/Backups"
            );

            values.put(
                    MediaStore.Downloads.IS_PENDING,
                    1
            );

            Uri fileUri = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
            );

            if (fileUri == null) {
                throw new Exception(
                        "تعذر إنشاء ملف النسخة"
                );
            }

            OutputStream outputStream =
                    resolver.openOutputStream(fileUri);

            if (outputStream == null) {
                throw new Exception(
                        "تعذر فتح ملف النسخة"
                );
            }

            outputStream.write(fileBytes);
            outputStream.flush();
            outputStream.close();

            ContentValues completed = new ContentValues();
            completed.put(
                    MediaStore.Downloads.IS_PENDING,
                    0
            );

            resolver.update(
                    fileUri,
                    completed,
                    null,
                    null
            );
        }

        private void saveForOldAndroid(
                byte[] fileBytes,
                String fileName
        ) throws Exception {

            File baseDirectory =
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                    );

            File backupDirectory = new File(
                    baseDirectory,
                    "Masroofi/Backups"
            );

            if (!backupDirectory.exists()
                    && !backupDirectory.mkdirs()) {
                throw new Exception(
                        "تعذر إنشاء مجلد النسخ"
                );
            }

            File backupFile = new File(
                    backupDirectory,
                    fileName
            );

            FileOutputStream outputStream =
                    new FileOutputStream(backupFile);

            outputStream.write(fileBytes);
            outputStream.flush();
            outputStream.close();
        }
    }

    private void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show()
        );
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
