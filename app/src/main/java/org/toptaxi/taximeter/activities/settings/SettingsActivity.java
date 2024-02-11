package org.toptaxi.taximeter.activities.settings;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.settings.SettingsSectionsPagerAdapter;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;

public class SettingsActivity extends MainAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsSectionsPagerAdapter settingsSectionsPagerAdapter = new SettingsSectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(settingsSectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

}