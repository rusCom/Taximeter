package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.DateTimeTools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Profile {
    public Boolean pushNotificationActive;
    public Integer taximeterFreeOrderCount;
    public Integer tariffPlanID;
    public LocalDateTime tariffPlanEndDate;

    public void parseData(JSONObject data) throws JSONException {
        pushNotificationActive = JSONGetBool(data, "push_notification_active", true);
        taximeterFreeOrderCount = JSONGetInteger(data, "free_order_count", 20);

        if (data.has("tariff_plan")){
            JSONObject tariff = data.getJSONObject("tariff_plan");
            tariffPlanID = JSONGetInteger(tariff, "id");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tariffPlanEndDate = LocalDateTime.parse(tariff.getString("date"));
            }
        }
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

    public boolean showActivateTariffPlan(){
        if (tariffPlanID == null)return true;
        if (tariffPlanID == 0)return true;
        if (tariffPlanID == 1)return true;
        return false;
    }

    public boolean showTariffPlanCaption(){
        if (tariffPlanID == null)return false;
        if (tariffPlanID == 0)return false;
        if (tariffPlanID == 1)return false;
        if (tariffPlanEndDate == null)return false;
        return true;
    }

    public String getTariffPlanCaption(){
        String result = "Смена до ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result += tariffPlanEndDate.getHour() + ":" + tariffPlanEndDate.getMinute();
            if (DateTimeTools.isToday(tariffPlanEndDate)){
                result += " сегодня";
            } else if (DateTimeTools.isTomorrow(tariffPlanEndDate)) {
                result += " завтра";
            }

        }

        return result;
    }
}
