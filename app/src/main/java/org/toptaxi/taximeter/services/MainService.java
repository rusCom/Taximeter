package org.toptaxi.taximeter.services;


import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.StartApplicationActivity;

import java.util.concurrent.TimeUnit;

public class MainService extends Service {
    private boolean isRunning = false;
    private static final int DEFAULT_NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    PowerManager m_powerManager = null;
    PowerManager.WakeLock m_wakeLock = null;


    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogService.getInstance().log(this, "onCreate");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogService.getInstance().log(this, "onStartCommand");
        MainApplication.getInstance().getLocationService().startLocationListener();
        isRunning = true;
        sendNotification("aTaxi.Водитель");
        getDataTask();
        if (m_powerManager == null) {
            m_powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }

        if (m_wakeLock == null) {
            m_wakeLock = m_powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            m_wakeLock.acquire(60 * 60 * 1000L /*60 minutes*/);
        }

        return START_STICKY;
    }

    //Send custom notification
    public void sendNotification(String Text) {

        //These three lines makes Notification to open main activity after clicking on it
        Intent notificationIntent = new Intent(this, StartApplicationActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent contentIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "aTaxi.Водитель";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "aTaxi.Водитель", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("aTaxi.Водитель");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

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
        startForeground(DEFAULT_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        LogService.getInstance().log(this, "onDestroy");
        isRunning = false;
        MainApplication.getInstance().getMainAccount().setNullStatus();
        MainApplication.getInstance().getRestService().httpGetThread("/driver/offline");
        MainApplication.getInstance().getLocationService().stopLocationListener();
        if (m_wakeLock != null) {
            m_wakeLock.release();
            m_wakeLock = null;
        }
    }

    void getDataTask() {
        new Thread(() -> {

            String lastNotificationMessage = "";
            String driverToken = MainApplication.getInstance().getMainAccount().getToken();

            while (isRunning) {

                try {
                    JSONObject data2 = MainApplication.getInstance().getDataRestService().httpGet("/last/data").getJSONObject("result");
                    if (!JSONGetString(data2, "driver_token").equals(driverToken)) {
                        MainApplication.getInstance().getRestService().httpGetThread("/server_error?method=data_driver_token");
                        data2 = MainApplication.getInstance().getDataRestService().httpGet("/last/data").getJSONObject("result");
                    }
                    if (JSONGetString(data2, "driver_token").equals(driverToken)) {
                        MainApplication.getInstance().parseData(data2);
                        String notificationMessage = MainApplication.getInstance().getMainAccount().getBalanceString() + " " + MainApplication.getInstance().getMainAccount().getStatusName() + " " + MainApplication.getInstance().getMainAccount().getName();
                        if (!lastNotificationMessage.equals(notificationMessage)) {
                            lastNotificationMessage = notificationMessage;
                            sendNotification(notificationMessage);
                        }
                    }

                } catch (JSONException ignored) {
                }

                try {
                    TimeUnit.SECONDS.sleep(MainApplication.getInstance().getPreferences().getSystemDataTimer());
                } catch (InterruptedException ignored) {
                }
            }
            //Removing any notifications
            notificationManager.cancel(DEFAULT_NOTIFICATION_ID);
            MainApplication.getInstance().getMainAccount().setNullStatus();
            MainApplication.getInstance().getRestService().httpGetThread("/driver/offline");
            MainApplication.getInstance().onTerminate();
            stopSelf();
        }).start();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
