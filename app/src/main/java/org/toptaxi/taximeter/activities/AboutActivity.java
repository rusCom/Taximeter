package org.toptaxi.taximeter.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;

public class AboutActivity extends MainAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar mainToolbar = findViewById(R.id.activity_about_toolbar);
        setSupportActionBar(mainToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("О приложении");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mainToolbar.setNavigationOnClickListener(view -> onBackPressed());
        }
        ((TextView)findViewById(R.id.tvAboutVersion)).setText("Версия: " + MainApplication.getInstance().getAppVersion());

        findViewById(R.id.tvAboutLicenseAgreement).setOnClickListener(v -> goToURL(MainApplication.getInstance().getPreferences().getLicenseAgreementLink()));
        findViewById(R.id.tvAboutPrivacyPolicy).setOnClickListener(v -> goToURL(MainApplication.getInstance().getPreferences().getPrivacyPolicyLink()));

    }
}