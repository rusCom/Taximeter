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
    public void log(String className, String method){
        this.log(className, method, "");
    }

    public void log(String className, String method, String data){
        if (isLog(className)) {
            if (data.equals("")) {
                Log.d("##### " + className, method);
            } else {
                Log.d("##### " + className + ": " + method, data);
            }
        }
    }

    private boolean isLog(String className) {
        switch (className) {
            case "SettingsAlarmFragment":
                return false;
            case "LoginActivity":
                return false;
            case "Payment":
                return true;
            case "BalanceActivity":
                return false;
            case "MainActivity":
                return false;
            case "PriorOrderActivity":
                return false;
            case "FirebaseService":
                return false;
            case "MainActivityDrawer":
                return false;
            case "LocationService":
                return false;
            case "StartApplicationActivity":
                return false;
            case "RestService":
                return false;
            case "MainService":
                return false;
            case "sys":
                return false;
        }
        return false;
    }
}
