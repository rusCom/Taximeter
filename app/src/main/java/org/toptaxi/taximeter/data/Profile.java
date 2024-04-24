package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetCalendar;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetDouble;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.DateTimeTools;
import org.toptaxi.taximeter.tools.MainUtils;

import java.text.DecimalFormat;
import java.util.Calendar;

public class Profile {
    public Boolean pushNotificationActive;
    public Integer taximeterFreeOrderCount;
    public Integer tariffPlanID;
    public Calendar tariffPlanEndDate;
    private Double balance;
    private Integer status;
    private String callSign;
    private String fullName;

    public void parseData(JSONObject data) throws JSONException {
        pushNotificationActive = JSONGetBool(data, "push_notification_active", true);
        taximeterFreeOrderCount = JSONGetInteger(data, "free_order_count", 10);
        balance = JSONGetDouble(data, "balance");
        status = JSONGetInteger(data, "status", 0);
        callSign = JSONGetString(data, "callsign");
        fullName = JSONGetString(data, "name");


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

    public Boolean isBalanceShow(){
        return balance != null;
    }

    public Boolean isShowNullBalanceDialog(){
        if (balance == null)return false;
        return balance <= 50;
    }

    public String getNotificationMessageTitle(){
        if (balance == null)return getStatusName() + " [" + callSign + "]" + fullName;
        return getBalanceFormat() + MainUtils.getRubSymbol() + " " + getStatusName() + " [" + callSign + "]" + fullName;
    }

    public String getMainActivityCaption() {
        if (balance == null)return getStatusName();
        String result = getBalanceFormat();
        result += " " + MainUtils.getRubSymbol();
        result += " " + getStatusName();
        return result;
    }

    public String getDrawerName(){
        return "[" + callSign + "] " + fullName;
    }

    public String getBalanceFormat(){
        if (balance == null){
            return "0.00";
        }
        try {
            return new DecimalFormat("###,##0.00").format(balance);
        }
        catch (Exception ignored){
        }
        return balance.toString();
    }

    public String getStatusName() {
        String result = "";
        if (status == null){return "";}
        switch (status) {
            case 0 -> result = "Занят";
            case 1 -> result = "На автораздаче";
            case 2 -> result = "На заказе";
        }
        return result;
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
