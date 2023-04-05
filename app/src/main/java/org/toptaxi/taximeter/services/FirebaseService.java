package org.toptaxi.taximeter.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.tools.MainUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FirebaseService {
    private final SharedPreferences sharedPreferences;

    public FirebaseService() {
        sharedPreferences = MainApplication.getInstance().getSharedPreferences("firebase", Context.MODE_PRIVATE);
        String pushToken = sharedPreferences.getString("pushToken", "");
        if (pushToken.equals("")) {
            getNewPushToken();
        }
    } // public FirebaseService()

    public void getNewPushToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        LogService.getInstance().log("FirebaseService", "Fetching FCM registration token failed " + task.getException());
                        return;
                    }
                    String token = task.getResult();
                    onNewPushToken(token);
                });
    }

    public void clearData() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(
                task -> {
                    LogService.getInstance().log("FirebaseService", "deleteToken " + task.isSuccessful());
                }
        );
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.remove("pushTopics");
        sharedPreferencesEditor.remove("pushToken");
        sharedPreferencesEditor.apply();
        LogService.getInstance().log("FirebaseService", "clear PushToken");
    }

    void onNewPushToken(String token) {
        LogService.getInstance().log("FirebaseService", "onNewPushToken", token);
        MainApplication.getInstance().getRestService().httpGetThread("/push?push_token=" + token);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString("pushToken", token);
        sharedPreferencesEditor.remove("pushTopics");
        sharedPreferencesEditor.apply();
    }


    public void checkTopics(JSONArray topics) {
        LogService.getInstance().log("FirebaseService", "CheckTopics", topics.toString());
        String oldTopicsString = sharedPreferences.getString("pushTopics", "[]");
        try {
            JSONArray oldTopics = new JSONArray(oldTopicsString);
            if (oldTopics.equals(topics)) {
                return;
            }
            LogService.getInstance().log("FirebaseService", "equals topics = " + oldTopics.equals(topics));
            LogService.getInstance().log("FirebaseService", "oldTopics = " + oldTopics);
            for (int itemID = 0; itemID < oldTopics.length(); itemID++) {
                if (!MainUtils.isJSONArrayHaveValue(topics, oldTopics.getString(itemID))) {
                    LogService.getInstance().log("FirebaseService", "unsubscribeFromTopic " + oldTopics.getString(itemID));
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(oldTopics.getString(itemID));
                }
            }
            for (int itemID = 0; itemID < topics.length(); itemID++) {
                if (!MainUtils.isJSONArrayHaveValue(oldTopics, topics.getString(itemID))) {
                    LogService.getInstance().log("FirebaseService", "subscribeToTopic " + topics.getString(itemID));
                    FirebaseMessaging.getInstance().subscribeToTopic(topics.getString(itemID));
                }
            }

            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString("pushTopics", topics.toString());
            sharedPreferencesEditor.apply();

        } catch (JSONException ignored) {
        }
    }
}
