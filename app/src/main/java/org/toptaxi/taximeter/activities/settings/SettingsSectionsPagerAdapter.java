package org.toptaxi.taximeter.activities.settings;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SettingsSectionsPagerAdapter extends FragmentPagerAdapter {

    public SettingsSectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).

        switch (position){
            case 0:return SettingsAlarmFragment.newInstance();
            case 1:return SettingsProfileFragment.newInstance();
        }

        return SettingsAlarmFragment.newInstance();
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