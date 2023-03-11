package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetDouble;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.MainUtils;

import java.text.DecimalFormat;

public class Account {
    private String Token;
    private Double Balance;
    public Double balanceCorporateTaxi;
    private String Name;
    private String serName;
    private Integer status = 0, lastStatus = -1;
    private String NotReadMessageCount;
    private Boolean isCheckPriorOrder = true;
    private Boolean isGetOnLine = false;
    public boolean isParsedData = false;



    public Account(String token) {
        Token = token;
    }

    public void parseData(JSONObject data) {
        LogService.getInstance().log(this, data.toString());
        Balance = JSONGetDouble(data, "balance");
        Name = JSONGetString(data, "name");
        serName = JSONGetString(data, "ser_name");
        status = JSONGetInteger(data, "status", 0);
        NotReadMessageCount = JSONGetString(data, "nrmc");
        isCheckPriorOrder = JSONGetBool(data, "check_prior");
        isGetOnLine = JSONGetBool(data, "get_on_line");
        balanceCorporateTaxi = JSONGetDouble(data, "balance_corporate_taxi");

        if (!status.equals(lastStatus)) {
            MainApplication.getInstance().onDriverStatusChange(status);

            lastStatus = status;
            if (status == Constants.DRIVER_ON_ORDER)
                MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDER);
            else
                MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
        }

        MainApplication.getInstance().onAccountDataChange();
        isParsedData = true;
    }

    public String getMainActivityCaption() {
        String result = new DecimalFormat("###,##0.00").format(Balance);
        result += " " + MainUtils.getRubSymbol();
        result += " " + getStatusName();
        return result;
    }

    public void setNullStatus() {
        status = null;
    }

    public Boolean getOnLine() {
        if (status == Constants.DRIVER_OFFLINE) return isGetOnLine;
        return true;
    }

    public Boolean getCheckPriorOrder() {
        return isCheckPriorOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public String getBalanceString() {
        if (Balance == null){
            return "0.00";
        }
        try {
            return new DecimalFormat("###,##0.00").format(Balance);
        }
        catch (Exception ignored){
            MainApplication.getInstance().getRestService().serverError("Account.getBalanceString", Balance.toString());
        }
        return Balance.toString();
    }

    public String getBalanceCorporateTaxiString() {
        return new DecimalFormat("###,#00.00").format(balanceCorporateTaxi);
    }

    public Double getBalance() {
        return Balance;
    }

    public String getName() {
        return Name;
    }

    public String getSerName() {
        return serName;
    }

    public String getToken() {
        return Token;
    }


    public String getStatusName() {
        String result = "";
        if (status == null){return "";}
        switch (status) {
            case 0:
                result = "Занят";
                break;
            case 1:
                result = "На автораздаче";
                break;
            case 2:
                result = "На заказе";
                break;
        }
        return result;
    }

    public void setToken(String token) {
        Token = token;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString("accountToken", token);
        editor.apply();

    }

    public String getNotReadMessageCount() {
        return NotReadMessageCount;
    }
}
