package org.toptaxi.taximeter.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.StartApplicationActivity;

import java.util.concurrent.TimeUnit;

public class MainService extends Service {
    private static final int DEFAULT_NOTIFICATION_ID = 10242658;

    @Override
    public void onCreate() {
        super.onCreate();
        LogService.getInstance().log(this, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogService.getInstance().log(this, "onStartCommand");
        MainApplication.getInstance().getLocationService().startLocationListener();
        sendNotification("aТакси.Водитель");
        startDataTask();
        return START_STICKY;
    }


    public void sendNotification(String Text) {
        Intent notificationIntent = new Intent(this, StartApplicationActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "aTaxi.Водитель";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "aTaxi.Водитель", NotificationManager.IMPORTANCE_HIGH);

        // Configure the notification channel.
        notificationChannel.setDescription("aTaxi.Водитель");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
        notificationChannel.enableVibration(true);
        notificationManager.createNotificationChannel(notificationChannel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setContentIntent(contentIntent)
                .setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(MainApplication.getInstance().getResources().getString(R.string.app_name)) //Заголовок
                .setContentText(Text) // Текст уведомления
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(true);

        Notification notification;
        notification = notificationBuilder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(DEFAULT_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(DEFAULT_NOTIFICATION_ID, notification);
        }
    }


    private void startDataTask() {
        new Thread(() -> {
            String lastNotificationMessage = "";
            LogService.getInstance().log("MainService", "startDataTask");
            while (MainApplication.getInstance().isRunning) {

                try {
                    JSONObject data2 = MainApplication.getInstance().getDataRestService().httpGet("/last/data").getJSONObject("result");

                    if (MainApplication.getInstance().isRunning) {
                        MainApplication.getInstance().parseData(data2);
                        String notificationMessage = MainApplication.getInstance().getProfile().getNotificationMessageTitle();
                        if (!lastNotificationMessage.equals(notificationMessage)) {
                            lastNotificationMessage = notificationMessage;
                            sendNotification(notificationMessage);
                        }
                        LogService.getInstance().log("MainService", data2.toString());
                    }


                } catch (JSONException ignored) {
                }

                try {
                    TimeUnit.SECONDS.sleep(MainApplication.getInstance().getPreferences().getSystemDataTimer());
                } catch (InterruptedException ignored) {
                }
            }
            LogService.getInstance().log("MainService", "stop service isRunning = false");
            MainApplication.getInstance().getMainAccount().setNullStatus();
            MainApplication.getInstance().getRestService().httpGetThread("/driver/offline");
            MainApplication.getInstance().getLocationService().stopLocationListener();
            MainApplication.getInstance().onTerminate();
            stopSelf();
        }).start();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogService.getInstance().log(this, "onDestroy");
        restartService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        LogService.getInstance().log(this, "onTaskRemoved");
        restartService();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LogService.getInstance().log(this, "onLowMemory");
        restartService();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogService.getInstance().log(this, "onTrimMemory");
        restartService();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogService.getInstance().log(this, "onUnbind");
        restartService();
        return super.onUnbind(intent);
    }

    private void restartService() {
        if (MainApplication.getInstance().isRunning) {
            LogService.getInstance().log(this, "restartService");
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartservice");
            broadcastIntent.setClass(this, Restarter.class);
            this.sendBroadcast(broadcastIntent);
        }
    }
}
