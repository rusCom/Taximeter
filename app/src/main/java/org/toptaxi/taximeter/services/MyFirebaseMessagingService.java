package org.toptaxi.taximeter.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.toptaxi.taximeter.MainApplication;

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
        Log.d(TAG, "onMessageReceived " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0){
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
