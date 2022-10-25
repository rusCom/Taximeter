package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetDouble;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;

public class RoutePoint {
    private String Name;
    private final Double Latitude;
    private final Double Longitude;

    RoutePoint(JSONObject data)  {
        this.Name = JSONGetString(data, "last_name");
        this.Latitude = JSONGetDouble(data, "lt");
        this.Longitude = JSONGetDouble(data, "ln");

        if (this.Name.equals("")){
            this.Name = JSONGetString(data, "name");
        }
    }


    public Double getLatitude() {
        return Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public LatLng getLatLng(){
        return new LatLng(Latitude, Longitude);
    }

    public String getName() {
        return Name;
    }


}
