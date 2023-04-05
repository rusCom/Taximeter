package org.toptaxi.taximeter.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import org.toptaxi.taximeter.MainApplication;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        new Handler(Looper.getMainLooper())
                .postDelayed(() -> {
                    MainApplication.getInstance().startMainService();
                }, 1000);
    }
}
