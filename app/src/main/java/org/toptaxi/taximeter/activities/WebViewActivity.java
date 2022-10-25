package org.toptaxi.taximeter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebView;

import org.toptaxi.taximeter.R;

public class WebViewActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Bundle webViewParams = getIntent().getExtras();
        String link = webViewParams.getString("link");
        webView = findViewById(R.id.webView);
        // String link = "http://lk.toptaxi.org:57773/csp/ataxi/start.csp";
        // указываем страницу загрузки
        webView.loadUrl(link);
    }
}
