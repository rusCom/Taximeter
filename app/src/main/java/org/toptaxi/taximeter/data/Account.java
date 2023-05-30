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

import java.text.DecimalFormat;

public class Account {
    private String Token;
    public Double balanceCorporateTaxi;
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


    public void setNullStatus() {
        status = null;
        isParsedData = false;
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


    public String getBalanceCorporateTaxiString() {
        return new DecimalFormat("###,#00.00").format(balanceCorporateTaxi);
    }

    public String getSerName() {
        return serName;
    }

    public String getToken() {
        return Token;
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
