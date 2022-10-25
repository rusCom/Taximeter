package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;

public class Profile {
    public Boolean pushNotificationActive;
    public Integer taximeterFreeOrderCount;

    public void parseData(JSONObject data) {
        pushNotificationActive = JSONGetBool(data, "push_notification_active", true);
        taximeterFreeOrderCount = JSONGetInteger(data, "free_order_count", 20);
    }

    public void setData(Boolean newPushNotificationActive, Integer newTaximeterFreeOrderCount) {
        JSONObject newData = new JSONObject();
        if (newPushNotificationActive != pushNotificationActive) {
            try {
                newData.put("push_notification_active", newPushNotificationActive);
                pushNotificationActive = newPushNotificationActive;
            } catch (JSONException ignored) {
            }
        }
        if (!newTaximeterFreeOrderCount.equals(taximeterFreeOrderCount)) {
            try {
                newData.put("free_order_count", newTaximeterFreeOrderCount);
                taximeterFreeOrderCount = newTaximeterFreeOrderCount;
            } catch (JSONException ignored) {
            }
        }
        if (newData.length() != 0){
            MainApplication.getInstance().getRestService().httpPostThread("/profile/set", newData);
        }
    }
}
