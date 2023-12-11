package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.dialogs.PaymentsDialog;
import org.toptaxi.taximeter.tools.MainUtils;

import java.util.ArrayList;
import java.util.List;

public class Preferences {
    protected static String TAG = "#########" + Preferences.class.getName();
    private final SharedPreferences sPref;
    private Integer curTheme;
    private final List<String> dispatcherTemplateMessages;
    private Boolean newOrderAlarmCheck;
    private Boolean dispatchingCommissionSummaViewType;
    private Integer newOrderAlarmDistance, newOrderAlarmCost;
    private String supportPhone = "";
    private String licenseAgreementLink = "";
    private String privacyPolicyLink = "";
    private String paymentInstructionLink;
    public String instructionLink = "";
    public String vkGroupLink = "";
    private String checkPriorErrorText = "";
    private String dispatcherPhone = "";
    private Integer systemDataTimer = 5;
    public Integer systemTemplateMessagesTimeout = 60;
    private String driverInviteCaption = "";
    private String driverInviteText = "";
    private String clientInviteCaption = "";
    private String clientInviteText = "";
    private Boolean useRating = false;
    private JSONObject guaranteedIncome;
    public Boolean corporateTaxi = false;
    public String corporateTaxiBalanceButtonDialog;
    public String corporateTaxiCheckOrderDialog;
    public String corporateTaxiContactPhone;
    public Long corporateTaxiCheckOrderDialogLastShow;
    public Boolean dispatcherMessages = false;
    public String hourInfoText = "";
    private boolean longDistanceMessage = false;
    private final List<TariffPlan> driverTariffPlans;




    public Preferences() {
        sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        curTheme = sPref.getInt("curTheme", 0);
        curTheme = 2;


        dispatcherTemplateMessages = new ArrayList<>();

        newOrderAlarmCheck = sPref.getBoolean("newOrderAlarmCheck", true);
        dispatchingCommissionSummaViewType = sPref.getBoolean("dispatchingCommissionSummaViewType", true);
        newOrderAlarmDistance = sPref.getInt("newOrderAlarmDistance", 2);
        newOrderAlarmCost = sPref.getInt("newOrderAlarmCost", 100);

        driverTariffPlans = new ArrayList<>();

    }

    public void parseData(JSONObject data) throws JSONException {
        this.supportPhone = JSONGetString(data, "support_phone");
        this.dispatcherPhone = JSONGetString(data, "dispatcher_phone");
        this.licenseAgreementLink = JSONGetString(data, "license_agreement_link");
        this.privacyPolicyLink = JSONGetString(data, "privacy_policy_link");
        this.systemDataTimer = JSONGetInteger(data, "system_data_timer", 5);
        this.systemTemplateMessagesTimeout = JSONGetInteger(data, "system_template_messages_timeout", 60);
        this.paymentInstructionLink = JSONGetString(data, "payment_instruction_link");
        this.instructionLink = JSONGetString(data, "instruction_link");
        this.vkGroupLink = JSONGetString(data, "vk_group_link");
        this.checkPriorErrorText = JSONGetString(data, "check_prior_error_text");
        this.driverInviteCaption = JSONGetString(data, "driver_invite_caption");
        this.driverInviteText = JSONGetString(data, "driver_invite_text");
        this.clientInviteCaption = JSONGetString(data, "client_invite_caption");
        this.clientInviteText = JSONGetString(data, "client_invite_text");
        this.useRating = MainUtils.JSONGetBool(data, "use_rating");
        this.dispatcherMessages = JSONGetBool(data, "dispatcher_messages");
        this.hourInfoText = JSONGetString(data, "hour_info_text");
        this.longDistanceMessage = JSONGetBool(data, "long_distance_message");

        if (data.has("guaranteed_income")){
            this.guaranteedIncome = data.getJSONObject("guaranteed_income");
        }

        dispatcherTemplateMessages.clear();
        driverTariffPlans.clear();

        if (data.has("dispatcher_template_messages")) {
            JSONArray templateMessagesJSON = data.getJSONArray("dispatcher_template_messages");
            for (int itemID = 0; itemID < templateMessagesJSON.length(); itemID++) {
                dispatcherTemplateMessages.add(templateMessagesJSON.getJSONObject(itemID).getString("message"));
            }
        }

        if (data.has("tariff_plans")) {
            JSONArray unlimitedTariffPlansJSONArray = data.getJSONArray("tariff_plans");
            for (int itemID = 0; itemID < unlimitedTariffPlansJSONArray.length(); itemID++) {
                driverTariffPlans.add(new TariffPlan(unlimitedTariffPlansJSONArray.getJSONObject(itemID)));
            }
        }

        if (data.has("rest_hosts")) {
            MainApplication.getInstance().getRestService().setRestHost(data.getJSONArray("rest_hosts"));
        }

        if (data.has("data_rest_hosts")) {
            MainApplication.getInstance().setDataRestService(data.getJSONArray("data_rest_hosts"));
        }

        if (data.has("push_topics")) {
            MainApplication.getInstance().getFirebaseService().checkTopics(data.getJSONArray("push_topics"));
        }

        if (data.has("corporate_taxi")) {
            this.corporateTaxi = true;
            JSONObject corporateTaxi = data.getJSONObject("corporate_taxi");
            this.corporateTaxiBalanceButtonDialog = JSONGetString(corporateTaxi, "balance_button_dialog");
            this.corporateTaxiContactPhone = JSONGetString(corporateTaxi, "contact_phone");
            this.corporateTaxiCheckOrderDialog = JSONGetString(corporateTaxi, "check_order_dialog");
            corporateTaxiCheckOrderDialogLastShow = sPref.getLong("corporateTaxiCheckOrderDialogLastShow", 0);
        }

        if (data.has("available_payments")) {
            PaymentsDialog.getInstance().setPreferences(data.getJSONObject("available_payments"));
        }
    }

    public boolean isShowCorporateTaxiCheckOrderDialog() {
        boolean result = false;
        if (corporateTaxi && !corporateTaxiCheckOrderDialog.equals("")) {
            // Проверяем, что с поледенго показа прошло более 24 часов
            if (MainUtils.passedTimeHour(corporateTaxiCheckOrderDialogLastShow) > 24) {
                result = true;
            }
        }
        return result;
    }

    public Boolean isGuaranteedIncome() {
        return guaranteedIncome != null;
    }

    public JSONObject getGuaranteedIncome() {
        return guaranteedIncome;
    }

    public boolean isLongDistanceMessage() {
        return longDistanceMessage;
    }

    public String getLicenseAgreementLink() {
        return licenseAgreementLink;
    }

    public String getPrivacyPolicyLink() {
        return privacyPolicyLink;
    }

    public void setLastShowCorporateTaxiCheckOrderDialog() {
        corporateTaxiCheckOrderDialogLastShow = System.currentTimeMillis();
        SharedPreferences.Editor editor = sPref.edit();
        editor.putLong("corporateTaxiCheckOrderDialogLastShow", this.corporateTaxiCheckOrderDialogLastShow);
        editor.apply();
    }

    public List<TariffPlan> getDriverTariffPlans() {
        return driverTariffPlans;
    }

    public boolean useUnlimitedTariffPlans() {
        return driverTariffPlans.size() != 0;
    }

    public Boolean useRating() {
        return useRating;
    }

    public String getSupportPhone() {
        return supportPhone;
    }

    public String getPaymentInstructionLink() {
        if (paymentInstructionLink == null)return null;
        if (paymentInstructionLink.equals(""))return null;
        return paymentInstructionLink;
    }

    public Integer getSystemDataTimer() {
        return systemDataTimer;
    }

    public String getDriverInviteCaption() {
        return driverInviteCaption;
    }

    public String getDriverInviteText() {
        return driverInviteText;
    }

    public Boolean isDriverInvite() {
        if (driverInviteText.equals("")) return false;
        return !driverInviteCaption.equals("");
    }

    public String getClientInviteCaption() {
        return clientInviteCaption;
    }

    public String getClientInviteText() {
        return clientInviteText;
    }

    public Boolean isClientInvite() {
        if (clientInviteText.equals("")) return false;
        return !clientInviteCaption.equals("");
    }


    public String getDispatcherPhone() {
        return dispatcherPhone;
    }


    public List<String> getDispatcherTemplateMessages() {
        return dispatcherTemplateMessages;
    }


    public String getCheckPriorErrorText() {
        return checkPriorErrorText;
    }


    public void changeTheme() {
        curTheme++;
        if (curTheme > 2) curTheme = 0;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("curTheme", curTheme);
        editor.apply();
    }

    public int getTheme() {
        Log.d(TAG, "curTheme = " + curTheme);
        return switch (curTheme) {
            case 0 -> AppCompatDelegate.MODE_NIGHT_NO;
            case 1 -> AppCompatDelegate.MODE_NIGHT_YES;
            default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        };
    }

    public String getThemeName() {
        return switch (curTheme) {
            case 0 -> "Дневная";
            case 1 -> "Ночная";
            default -> "Подсветка";
        };
    }

    public Boolean getNewOrderAlarmCheck() {
        return newOrderAlarmCheck;
    }

    public Boolean getDispatchingCommissionSummaViewType() {
        return dispatchingCommissionSummaViewType;
    }

    public Integer getNewOrderAlarmDistance() {
        return newOrderAlarmDistance;
    }

    public Integer getNewOrderAlarmCost() {
        return newOrderAlarmCost;
    }

    public void setNewPreferencesData(Boolean newOrderAlarmCheck, Integer newOrderAlarmDistance, Integer newOrderAlarmCost, Boolean dispatchingCommissionSummaViewType) {
        this.newOrderAlarmCheck = newOrderAlarmCheck;
        this.newOrderAlarmDistance = newOrderAlarmDistance;
        this.newOrderAlarmCost = newOrderAlarmCost;
        this.dispatchingCommissionSummaViewType = dispatchingCommissionSummaViewType;

        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean("newOrderAlarmCheck", this.newOrderAlarmCheck);
        editor.putInt("newOrderAlarmDistance", this.newOrderAlarmDistance);
        editor.putInt("newOrderAlarmCost", this.newOrderAlarmCost);
        editor.putBoolean("dispatchingCommissionSummaViewType", this.dispatchingCommissionSummaViewType);
        editor.apply();

    }
}
