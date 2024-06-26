package org.toptaxi.taximeter;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.data.Account;
import org.toptaxi.taximeter.data.MainActionItem;
import org.toptaxi.taximeter.data.Messages;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.data.Orders;
import org.toptaxi.taximeter.data.Preferences;
import org.toptaxi.taximeter.data.Profile;
import org.toptaxi.taximeter.services.FirebaseService;
import org.toptaxi.taximeter.services.LocationService;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.services.MainService;
import org.toptaxi.taximeter.services.RestService;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.OnCompleteOrdersChange;
import org.toptaxi.taximeter.tools.OnMainDataChangeListener;
import org.toptaxi.taximeter.tools.OnPriorOrdersChange;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MainApplication extends Application {
    protected static String TAG = "#########" + MainApplication.class.getName();
    protected static MainApplication mainApplication;
    public static MainApplication getInstance() {
        return mainApplication;
    }
    private Integer MainActivityCurView;
    private OnMainDataChangeListener onMainDataChangeListener;
    private OnPriorOrdersChange onPriorOrdersChange;
    private OnCompleteOrdersChange onCompleteOrdersChange;
    private Account mainAccount;
    private Preferences preferences;
    private Profile profile;
    private Orders curOrders, priorOrders, completeOrders;
    MainActivity mainActivity;
    private Messages mainMessages;
    String curOrderData = "";
    Integer viewOrderID;
    private Order newOrder, curOrder, hisOrderView;
    final Handler uiHandler = new Handler(Looper.getMainLooper());
    public int lastRequestUID = 0;
    private Calendar ServerDate;
    private RestService restService;
    private RestService dataRestService;
    private LocationService locationService;
    private String appVersion;
    private Integer appVersionCode;
    HashMap<String, String> lastServerData = new HashMap<>();
    FirebaseService firebaseService;
    public boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        LogService.getInstance().log(this, "onCreate");
        mainApplication = this;
        MainActivityCurView = Constants.CUR_VIEW_CUR_ORDERS;

        locationService = new LocationService(this);
        ServerDate = Calendar.getInstance();
        restService = new RestService();
        profile = new Profile();
    }

    @Override
    public void onTerminate() {
        LogService.getInstance().log(this, "onTerminate");
        lastServerData.clear();
        super.onTerminate();
    }

    public RestService getRestService() {
        return restService;
    }

    public void setDataRestService(JSONArray restHosts) {
        if (dataRestService == null) {
            dataRestService = new RestService();
        }
        dataRestService.setRestHost(restHosts);
    }

    public RestService getDataRestService() {
        if (dataRestService == null) {
            return restService;
        }
        return dataRestService;
    }

    public boolean isMainServiceStart() {
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
        boolean run = false;
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if (rsi.service.getClassName().contains(getPackageName() + ".services.MainService")) {
                run = true;

            }
        }
        return run;
    }

    public void startMainService() {
        if (!isMainServiceStart()) {
            isRunning = true;
            Intent serviceIntent = new Intent(this, MainService.class);
            LogService.getInstance().log(this, "startForegroundService");
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
        }
    }

    public void stopMainService() {
        isRunning = false;
        stopService(new Intent(this, MainService.class));
    }

    public void setOnPriorOrdersChange(OnPriorOrdersChange onPriorOrdersChange) {
        this.onPriorOrdersChange = onPriorOrdersChange;
    }

    public void setOnCompleteOrdersChange(OnCompleteOrdersChange onCompleteOrdersChange) {
        this.onCompleteOrdersChange = onCompleteOrdersChange;
    }

    public Calendar getServerDate() {
        return ServerDate;
    }

    private boolean isNewData(JSONObject dataJSON, String fieldName) throws JSONException {
        if (dataJSON.has(fieldName)) {
            if (!lastServerData.containsKey(fieldName)) {
                lastServerData.put(fieldName, dataJSON.get(fieldName).toString());
                return true;
            } else if (!lastServerData.get(fieldName).equals(dataJSON.get(fieldName).toString())) {
                lastServerData.put(fieldName, dataJSON.get(fieldName).toString());
                return true;
            }
        }
        return false;
    }

    public Profile getProfile() {
        return profile;
    }

    public void parseData(JSONObject dataJSON) throws JSONException {
        if (dataJSON.has("preferences"))
            getPreferences().parseData(dataJSON.getJSONObject("preferences"));

        if (isNewData(dataJSON, "last_account")) {
            getMainAccount().parseData(dataJSON.getJSONObject("last_account"));
        }

        if (isNewData(dataJSON,"profile"))
            getProfile().parseData(dataJSON.getJSONObject("profile"));

        if (isNewData(dataJSON, "last_prior_orders")) {
            getPriorOrders().setFromJSONPrior(dataJSON.getJSONArray("last_prior_orders"));
            if ((onPriorOrdersChange != null) && (getPriorOrders() != null)) {
                uiHandler.post(() -> onPriorOrdersChange.OnPriorOrdersChange());
            }
        }

        if (isNewData(dataJSON, "last_orders_complete")){
            getCompleteOrders().setFromJSONPrior(dataJSON.getJSONArray("last_orders_complete"));
            LogService.getInstance().log(this, "parserData", "completeOrdersCount = " + getCompleteOrders().getCount());
            if ((onCompleteOrdersChange != null)) {
                uiHandler.post(() -> onCompleteOrdersChange.OnCompleteOrdersChange());
            }
            if (getMainActivity() != null){
                if ((getMainActivityCurView() == Constants.CUR_VIEW_CUR_ORDER) && (getCurOrder().getMainAction().equals("set_order_done"))){
                    getMainActivity().onCompleteOrdersChange(getCompleteOrders().getCount());
                }
            }
        }

        if (dataJSON.has("last_orders")){
            getCurOrders().setFromJSON(dataJSON.getJSONArray("last_orders"));
        }


        /*
        if (dataJSON.has("last_his_messages")) {
            getMainMessages().setFromJSON(dataJSON.getJSONArray("last_his_messages"));
        }

         */

        if (dataJSON.has("last_requestUID")) {
            lastRequestUID = dataJSON.getInt("last_requestUID");
        }
        if (dataJSON.has("last_date")) {
            ServerDate.setTimeInMillis(Timestamp.valueOf(dataJSON.getString("last_date")).getTime());
        }
        if (dataJSON.has("last_messages")) {
            getMainMessages().OnNewMessages(dataJSON.getJSONArray("last_messages"));
        }






        if (dataJSON.has("last_cur_order")) {
            if (!curOrderData.equals(dataJSON.getString("last_cur_order"))) {
                getCurOrder().setFromJSON(dataJSON.getJSONArray("last_cur_order").getJSONObject(0));
                if ((onMainDataChangeListener != null) && (getCurOrder() != null))
                    uiHandler.post(() -> onMainDataChangeListener.OnCurOrderDataChange(getCurOrder()));
                curOrderData = dataJSON.getString("last_cur_order");
            }
        }

        if (dataJSON.has("location_name")) {
            MainApplication.getInstance().getLocationService().setCurLocationName(dataJSON.getString("location_name"));
        }
    }

    public Order getCurOrder() {
        if (curOrder == null) {
            curOrder = new Order();
        }
        return curOrder;
    }


    public List<MainActionItem> getMainActions() {
        List<MainActionItem> mainActionItems = new ArrayList<>();
        if (getMainAccount().getStatus() != null) {
            if (getMainAccount().getStatus() != Constants.DRIVER_ON_ORDER) { // водитель не на заказе
                if (getMainAccount().getStatus() == Constants.DRIVER_OFFLINE)
                    mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_GO_ONLINE, "Встать на автораздачу"));
                if (getMainAccount().getStatus() == Constants.DRIVER_ONLINE)
                    mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_GO_OFFLINE, "Сняться с автораздачи"));
                if (getPreferences().useUnlimitedTariffPlans()) {
                    if (getProfile().showActivateTariffPlan())
                        mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_TARIFF_PLAN, "Покупка смены"));
                }
                mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_PRIOR_ORDER, "Предварительные заказы"));
            }

            // Если водитель на заказе и есть шаблоны сообщений, то показываем шаблоны
            if ((getMainAccount().getStatus() == Constants.DRIVER_ON_ORDER) && (getPreferences().getDispatcherTemplateMessages().size() > 0)) {
                for (int itemID = 0; itemID < getPreferences().getDispatcherTemplateMessages().size(); itemID++) {
                    mainActionItems.add(new MainActionItem(Constants.MENU_TEMPLATE_MESSAGE, getPreferences().getDispatcherTemplateMessages().get(itemID)));
                }
            }

            // Чат с диспетчером
            if (MainApplication.getInstance().getPreferences().dispatcherMessages) {
                mainActionItems.add(new MainActionItem(Constants.MAIN_ACTION_SEND_MESSAGE, "Написать диспетчеру"));
            }
            if (getMainAccount().getStatus() != Constants.DRIVER_ON_ORDER) { // водитель не на заказе
                mainActionItems.add(new MainActionItem(Constants.MENU_CLOSE_APPLICATION, "Сняться с линии"));
            }
        }

        return mainActionItems;
    }

    public Order getNewOrder() {
        return this.newOrder;
    }

    public void setNewOrder(Order curOrder) {
        this.newOrder = curOrder;
    }

    public void setViewOrderID(Integer viewOrderID) {
        this.viewOrderID = viewOrderID;
    }

    public Order getViewOrder() {
        Order result = null;
        if (viewOrderID != null) result = getCurOrders().getByOrderID(viewOrderID);
        return result;
    }

    public Messages getMainMessages() {
        if (mainMessages == null) mainMessages = new Messages();
        return mainMessages;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public void onDriverStatusChange(Integer driverStatus) {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> mainActivity.setDriverStatusIcon(driverStatus));
        }
    }

    public void onAccountDataChange() {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> mainActivity.mainActivityDrawer.updateDrawer());
            mainActivity.runOnUiThread(() -> mainActivity.setTitle(MainApplication.getInstance().getProfile().getMainActivityCaption()));
        }
    }

    public void onLocationDataChange() {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> mainActivity.onLocationDataChange());
        }
    }

    public Orders getCurOrders() {
        if (curOrders == null) curOrders = new Orders();
        return curOrders;
    }

    public Orders getPriorOrders() {
        if (priorOrders == null) priorOrders = new Orders();
        return priorOrders;
    }

    public Orders getCompleteOrders() {
        if (completeOrders == null) completeOrders = new Orders();
        return completeOrders;
    }

    public Order getHisOrderView() {
        return hisOrderView;
    }

    public void setHisOrderView(Order hisOrderView) {
        this.hisOrderView = hisOrderView;
    }

    public Account getMainAccount() {
        if (mainAccount == null) {
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
            mainAccount = new Account(sPref.getString("accountToken", ""));
        }
        return mainAccount;
    }

    public Preferences getPreferences() {
        if (preferences == null) preferences = new Preferences();
        return preferences;
    }

    public LocationService getLocationService() {
        return locationService;
    }

    public void setOnMainDataChangeListener(OnMainDataChangeListener onMainDataChangeListener) {
        this.onMainDataChangeListener = onMainDataChangeListener;
        if (onMainDataChangeListener != null) {
            onMainDataChangeListener.OnMainCurViewChange();
        }

    }

    public void setMainActivityCurView(Integer mainActivityCurView) {
        if (MainActivityCurView == null) return;
        if (mainActivityCurView == null) return;

        if (!mainActivityCurView.equals(MainActivityCurView)) {
            MainActivityCurView = mainActivityCurView;
            if (onMainDataChangeListener != null)
                uiHandler.post(() -> onMainDataChangeListener.OnMainCurViewChange());
        }
    }

    public int getMainActivityCurView() {
        return MainActivityCurView;
    }

    public FirebaseService getFirebaseService() {
        if (firebaseService == null) {
            firebaseService = new FirebaseService();
        }
        return firebaseService;
    }

    public void showToast(String message) {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, message, Toast.LENGTH_LONG).show());
        }
    }

    public void showProgressDialog() {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> mainActivity.showProgressDialog());
        }
    }

    public void dismissProgressDialog() {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> mainActivity.dismissProgressDialog());
        }
    }

    public String getAppVersion() {
        if (appVersion == null) {
            try {
                appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        if (appVersion == null) {
            return "000";
        }
        return appVersion;
    }

    public Integer getAppVersionCode() {
        if (appVersionCode == null) {
            try {
                appVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        if (appVersionCode == null) {
            return 0;
        }
        return appVersionCode;
    }


    public static Spanned fromHtml(String source) {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
    }


}
