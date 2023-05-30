package org.toptaxi.taximeter.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.toptaxi.taximeter.MainApplication;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "#########" + MyFirebaseMessagingService.class.getName();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        MainApplication.getInstance().getFirebaseService().onNewPushToken(token);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        LogService.getInstance().log("sys", "onMessageReceived");
        Log.d(TAG, "onMessageReceived " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0){
            Log.d(TAG, "Message data payload: " + remoteMessage.getData().get("action"));
            if (Objects.equals(remoteMessage.getData().get("action"), "location")){
                MainApplication.getInstance().getRestService().httpGet("/location");
            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
