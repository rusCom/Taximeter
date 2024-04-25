package org.toptaxi.taximeter.services;

import android.util.Log;

public class LogService {

    private static LogService logService;

    public static synchronized LogService getInstance() {
        if (logService == null) {
            logService = new LogService();
        }
        return logService;
    }

    public void log(Object object, String method) {
        this.log(object, method, "");
    }


    public void log(Object object, String method, String data) {
        String className = object.getClass().getSimpleName();
        this.log(className, method, data);

    }

    public void log(String className, String method) {
        this.log(className, method, "");
    }

    public void log(String className, String method, String data) {
        if (isLog(className)) {
            if (data.isEmpty()) {
                Log.d("#######", className + "->" + method);
            } else {
                Log.d("#######",  className + "->" + method + ": " + data);
            }
        }
    }

    private boolean isLog(String className) {
        return switch (className) {
            case "SettingsAlarmFragment" -> false;
            case "Preferences" -> false;
            case "Messages" -> true;
            case "LoginActivity" -> false;
            case "Profile" -> false;
            case "BalanceActivity" -> false;
            case "MainActivity" -> false;
            case "MainApplication" -> false;
            case "PriorOrderActivity" -> false;
            case "FirebaseService" -> false;
            case "MainActivityDrawer" -> false;
            case "LocationService" -> false;
            case "StartApplicationActivity" -> false;
            case "RestService" -> false;
            case "MainService" -> false;
            case "InviteActivity" -> false;
            case "sys" -> true;

            default -> false;
        };
    }
}
