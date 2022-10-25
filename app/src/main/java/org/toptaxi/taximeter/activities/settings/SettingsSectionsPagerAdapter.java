package org.toptaxi.taximeter.activities.settings;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.settings.PlaceholderFragment;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SettingsSectionsPagerAdapter extends FragmentPagerAdapter {
    private final Context mContext;

    public SettingsSectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        switch (position){
            case 0:return SettingsAlarmFragment.newInstance("0","1");
            case 1:return SettingsProfileFragment.mewInstance();
        }


        return PlaceholderFragment.newInstance(position + 1);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:return "Оповещения";
            case 1:return "Профиль";
        }

        return "";
    }

    @Override
    public int getCount() {
        return 2;
    }
}