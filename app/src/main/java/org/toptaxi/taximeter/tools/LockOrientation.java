package org.toptaxi.taximeter.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Created by ilya on 30.08.16.
 */
public class LockOrientation {
    private Activity act;

    public LockOrientation(Activity act) {
        this.act = act;
    }

    @SuppressLint("InlinedApi")
    public void lock() {
        switch (act.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                int rotation = act.getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == android.view.Surface.ROTATION_90 || rotation == android.view.Surface.ROTATION_180) {
                    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else {
                    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE:
                int rotation = act.getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90) {
                    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
                break;
        }
    }
}
