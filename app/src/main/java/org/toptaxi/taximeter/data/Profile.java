package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetCalendar;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.services.FirebaseService;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.DateTimeTools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class Profile {
    public Boolean pushNotificationActive;
    public Integer taximeterFreeOrderCount;
    public Integer tariffPlanID;
    public Calendar tariffPlanEndDate;

    public void parseData(JSONObject data) throws JSONException {
        pushNotificationActive = JSONGetBool(data, "push_notification_active", true);
        taximeterFreeOrderCount = JSONGetInteger(data, "free_order_count", 20);

        tariffPlanID = 1;
        if (data.has("tariff_plan")){
            JSONObject tariff = data.getJSONObject("tariff_plan");
            tariffPlanID = JSONGetInteger(tariff, "id");
            tariffPlanEndDate = JSONGetCalendar(tariff, "date");
        }
        MainApplication.getInstance().onAccountDataChange();
    }

    public void setData(Boolean newPushNotificationActive, Integer newTaximeterFreeOrderCount) {
        JSONObject newData = new JSONObject();
        if (newPushNotificationActive != pushNotificationActive) {
            try {
                newData.put("push_notification_active", newPushNotificationActive);
                pushNotificationActive = newPushNotificationActive;
            } catch (JSONException ignored) {
            }
            LogService.getInstance().log(this, "setData pushNotificationActive = " + pushNotificationActive);
            // Если отписываемся от всех рассылок, то стабильнее это сделать удалением аккуанта FireBase с телефона
            if (!pushNotificationActive){
                MainApplication.getInstance().getFirebaseService().clearData();
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

    public String getTariffPlanCaption(){
        if (showActivateTariffPlan()){
            return "На процентах";
        }
        String result = "Смена до ";
        result += DateTimeTools.getTime(tariffPlanEndDate) + " ";
        if (DateTimeTools.isCurDate(tariffPlanEndDate)){
            result += "сегодня";
        }
        else if (DateTimeTools.isTomorrow(tariffPlanEndDate)){
            result += "завтра";
        } else if (DateTimeTools.isAfterTomorrow(tariffPlanEndDate)) {
            result += "послезавтра";
        } else {
            result = tariffPlanEndDate.get(Calendar.DAY_OF_MONTH) + " " + DateTimeTools.getSklonMonthName(tariffPlanEndDate);
        }

        return result;
    }
}
