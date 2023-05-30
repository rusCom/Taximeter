package org.toptaxi.taximeter.services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.MainUtils;

public class LocationService implements LocationListener {
    private final LocationManager locationManager;
    // private OnLocationDataChange onLocationDataChange;
    private Location location;
    private String curLocationName;
    private final int curLocationStatus = GPS_OFF;

    public static final int GPS_FIXED       = 0;
    public static final int GPS_NOT_FIXED   = 1;
    public static final int GPS_OFF         = 2;
    public static final int GPS_ACCURACY    = 30;


    public LocationService(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        curLocationName = "";
    }

    public void startLocationListener(){
        if (ContextCompat.checkSelfPermission(MainApplication.getInstance(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LogService.getInstance().log(this, "startLocationListener");
            int locationTimeUpdate = 100 * 10;
            int locationMinDistance = 100;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationTimeUpdate, locationMinDistance, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationTimeUpdate, locationMinDistance, this);
        }
    }

    public void stopLocationListener(){
        LogService.getInstance().log(this, "stopLocationListener");
        locationManager.removeUpdates(this);
    }
    /*
    public void setOnLocationDataChange(OnLocationDataChange onLocationDataChange) {
        this.onLocationDataChange = onLocationDataChange;
    }

     */

    public String getCurLocationName() {
        if (curLocationName.equals("")){
            return MainUtils.round(location.getLatitude(), 5) + ";" +MainUtils.round(location.getLongitude(), 5) + ";" + location.getProvider();
        }
        return curLocationName + ";" + location.getProvider();
    }

    public void setCurLocationName(String curLocationName) {
        if (!this.curLocationName.equals(curLocationName)){
            this.curLocationName = curLocationName;
            MainApplication.getInstance().onLocationDataChange();
            // if (onLocationDataChange != null){onLocationDataChange.onLocationDataChange();}
        }
    }

    public int getGPSStatus(Context context){
        int result = GPS_OFF;
        if (((LocationManager)context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            result = GPS_NOT_FIXED;
            if (location != null){
                if (location.getProvider().equals("gps")){
                    if (location.getAccuracy() <= GPS_ACCURACY)
                        result = GPS_FIXED;

                }

            }
        }
        return result;
    }

    public Location getLocation() {
        if (location == null){
            if (ContextCompat.checkSelfPermission(MainApplication.getInstance(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location lastKnownLocationPASSIVE = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if ((lastKnownLocationGPS != null) && (lastKnownLocationPASSIVE != null)){
                    if (lastKnownLocationGPS.getTime() > lastKnownLocationPASSIVE.getTime()){
                        return lastKnownLocationGPS;
                    }
                    else {
                        return lastKnownLocationPASSIVE;
                    }
                } else if (lastKnownLocationGPS != null ) {
                    return lastKnownLocationGPS;
                }
                else {
                    return lastKnownLocationPASSIVE;
                }

            }


        }
        return location;
    }



    public JSONObject toJSON(){
        JSONObject data = new JSONObject();
        try {
            Location location1 = getLocation();
            if (location1 != null){
                data.put("lt", location1.getLatitude());
                data.put("ln", location1.getLongitude());
                data.put("accuracy", location1.getAccuracy());
                data.put("bearing", location1.getBearing());
                data.put("speed", location1.getSpeed());
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getLocationData(){
        String Data = "";
        if (location != null){
            Data += location.getLatitude() + ";";
            Data += location.getLongitude() + ";";
            Data += location.getAccuracy() + ";";
            Data += location.getSpeed() + ";";
            Data += location.getBearing() + ";";
        }
        return Data;
    }

    public LatLng getLatLng(){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
        MainApplication.getInstance().onLocationDataChange();
        // if (onLocationDataChange != null){onLocationDataChange.onLocationDataChange();}
        LogService.getInstance().log(this, "onLocationChanged", location.toString());
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LogService.getInstance().log(this, "onProviderDisabled", provider);
        MainApplication.getInstance().onLocationDataChange();
        // if (onLocationDataChange != null){onLocationDataChange.onLocationDataChange();}
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LogService.getInstance().log(this, "onProviderEnabled", provider);
        MainApplication.getInstance().onLocationDataChange();
        // if (onLocationDataChange != null){onLocationDataChange.onLocationDataChange();}
        // checkEnabled();
        // showLocation(locationManager.getLastKnownLocation(provider));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LogService.getInstance().log(this, "onStatusChanged", provider + ";" + status);
        MainApplication.getInstance().onLocationDataChange();
        // if (onLocationDataChange != null){onLocationDataChange.onLocationDataChange();}
        /*
        if (provider.equals(LocationManager.GPS_PROVIDER)) {

            tvStatusGPS.setText("Status: " + String.valueOf(status));
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            LogService.getInstance().log(this, "onStatusChanged", provider);
            tvStatusNet.setText("Status: " + String.valueOf(status));
        }

         */
    }
}

