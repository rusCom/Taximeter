package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RoutePointsAdapter;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.DateTimeTools;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Order {
    protected static String TAG = "#########" + Order.class.getName();
    private Integer IsFree = 0, Timer, ID, lastRequestUID = 0, State, Check, pickUpDistance;
    private String Note = "", ClientPhone = "", MainAction = "", StateName = "";
    private Double Cost;
    private Boolean IsNew = false;
    private Calendar WorkDate;
    private long NewOrderTimer = 15000;
    String GUID;
    public Boolean corporateTaxi = false;
    private Boolean isHour;
    public String payment = "", dispatchingName;
    public Integer dispatchingCommission;
    private List<RoutePoint> routePoints;
    public String JSONDataToCheckNew = "";

    public Order() {
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        this.IsFree = 0;

        GUID = JSONGetString(data, "guid");
        ID = JSONGetInteger(data, "id");
        corporateTaxi = JSONGetBool(data, "corporate_taxi");
        payment = JSONGetString(data, "payment");
        dispatchingName = JSONGetString(data, "dispatching_name");
        dispatchingCommission = JSONGetInteger(data, "dispatching_commission");
        isHour = JSONGetBool(data, "is_hour");
        pickUpDistance = JSONGetInteger(data, "pick_up_distance");


        if (data.has("phone")) this.ClientPhone = data.getString("phone");
        if (data.has("cost")) this.Cost = data.getDouble("cost");
        if (data.has("state")) this.State = data.getInt("state");
        if (data.has("check")) this.Check = data.getInt("check");
        if (data.has("note")) this.Note = data.getString("note");
        if (data.has("main_action")) this.MainAction = data.getString("main_action");
        if (data.has("state_name")) this.StateName = data.getString("state_name");
        if (data.has("timer")) this.Timer = data.getInt("timer");
        if (data.has("is_free")) this.IsFree = data.getInt("is_free");

        if (data.has("date")) {
            WorkDate = Calendar.getInstance();
            WorkDate.setTimeInMillis(Timestamp.valueOf(data.getString("date")).getTime());
        } else {
            WorkDate = null;
        }
        if (data.has("new_order_timer")) {
            this.NewOrderTimer = data.getInt("new_order_timer") * 1000L;
        }

        routePoints = new ArrayList<>();
        if (data.has("route")) {
            JSONArray routePointsJSON = data.getJSONArray("route");
            for (int itemID = 0; itemID < routePointsJSON.length(); itemID++) {
                RoutePoint routePoint = new RoutePoint(routePointsJSON.getJSONObject(itemID));
                routePoints.add(routePoint);
            }
        }

        if (data.has("timer")) {
            data.remove("timer");
        }


        JSONDataToCheckNew = data.toString();
    }

    public String getDispatchingCommission() {
        if (dispatchingCommission == null) {
            return "";
        }
        return dispatchingCommission + "%";
    }

    public Double getCost() {
        return Cost;
    }

    public Integer getState() {
        return State;
    }

    public Integer getCheck() {
        return Check;
    }


    public Integer getID() {
        return ID;
    }


    public String getNote() {
        if (Note == null) {
            return "";
        }
        return Note;
    }

    Integer getLastRequestUID() {
        return lastRequestUID;
    }

    void setLastRequestUID(Integer lastRequestUID) {
        this.lastRequestUID = lastRequestUID;
    }

    public int getCaptionColor() {
        int result = R.color.orderFree;
        if (IsFree == 1) result = R.color.orderFreePercent;
        if (payment.equals("corporation")) result = R.color.orderCashless;
        return result;
    }

    public void fillCurOrderViewData(MainAppCompatActivity mainAppCompatActivity, View view) {
        LogService.getInstance().log("MainActivity", JSONDataToCheckNew);
        view.findViewById(R.id.llCurOrderTitleEx).setVisibility(View.GONE);
        // Заголовок
        view.findViewById(R.id.llOrderDataTitle).setBackgroundResource(getCaptionColor());
        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataDistance), getDistanceString());
        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataPayment), getPayTypeName());
        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataDispatchingCommission), getDispatchingCommission());
        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataCost), getCostString());

        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataWorkDate), getPriorInfo());

        if (isHour) {
            view.findViewById(R.id.tvOrderDataIsHour).setVisibility(View.VISIBLE);
            view.findViewById(R.id.tvOrderDataIsHour).setOnClickListener(v -> mainAppCompatActivity.showSimpleDialog(MainApplication.getInstance().getPreferences().hourInfoText));
        } else view.findViewById(R.id.tvOrderDataIsHour).setVisibility(View.GONE);

        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataNote), Note);
        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataClientPhone), ClientPhone);
        MainUtils.TextViewSetTextOrGone(view.findViewById(R.id.tvOrderDataDispatchingName), dispatchingName);

        RoutePointsAdapter viewOrderPointsAdapter = new RoutePointsAdapter(this);

        RecyclerView rvViewOrderRoutePoints = view.findViewById(R.id.rvOrderDataRoutePoints);
        rvViewOrderRoutePoints.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvViewOrderRoutePoints.setAdapter(viewOrderPointsAdapter);
        viewOrderPointsAdapter.notifyItemRangeInserted(0, getRouteCount());

    }


    public Order(JSONObject data) throws JSONException {
        setFromJSON(data);
    }

    public long getNewOrderTimer() {
        return NewOrderTimer;
    }

    public void setNewOrderTimer(long newOrderTimer) {
        NewOrderTimer = newOrderTimer;
    }

    public String getPriorInfo() {
        String result = "";
        if (WorkDate != null) {
            String hour = String.valueOf(WorkDate.get(Calendar.HOUR_OF_DAY));
            if (WorkDate.get(Calendar.HOUR_OF_DAY) < 10) {
                hour = "0" + WorkDate.get(Calendar.HOUR_OF_DAY);
            }
            String minute = String.valueOf(WorkDate.get(Calendar.MINUTE));
            if (WorkDate.get(Calendar.MINUTE) < 10) {
                minute = "0" + WorkDate.get(Calendar.MINUTE);
            }
            result = hour + ":" + minute;
            if (DateTimeTools.isTomorrow(WorkDate)) {
                result = "Завтра на " + result;
            } else if (DateTimeTools.isAfterTomorrow(WorkDate)) {
                result = "Послезавтра на " + result;
            } else if (!DateTimeTools.isCurDate(WorkDate)) {
                result = WorkDate.get(Calendar.DAY_OF_MONTH) + " " + DateTimeTools.getSklonMonthName(WorkDate) + " на " + result;
            } else {
                result = "Сегодня на " + result;
            }

        }
        return result;
    }

    public String getStateName() {
        return StateName;
    }

    public String getMainAction() {
        return MainAction;
    }


    public String getDate() {
        return new SimpleDateFormat("HH:mm dd.MM", Locale.getDefault()).format(WorkDate.getTime());
    }

    public String getTimer() {
        int hour = Timer / 3600;
        int min = (Timer - hour / 3600) / 60;
        int sek = Timer - (min * 60);
        min = min - (hour * 60);

        String s_min = ":" + min;
        if (min < 10) s_min = ":0" + min;
        String s_sek = ":" + sek;
        if (sek < 10) s_sek = ":0" + sek;

        if (hour < 10) return "0" + hour + s_min + s_sek;
        return hour + s_min + s_sek;
    }

    public boolean isNew() {
        return IsNew;
    }

    public void setNew(boolean aNew) {
        IsNew = aNew;
    }

    public String getPayTypeName() {
        if (payment.equals("corporation")) return "Безнал";
        return "Нал";
    }

    public String getCostString() {
        return new DecimalFormat("###,###").format(Cost) + " " + MainUtils.getRubSymbol();
    }

    public Integer getPickUpDistance() {
        if (pickUpDistance == null)return 0;
        return pickUpDistance;
    }

    public String getDistanceString() {
        if (pickUpDistance == null){
            return "";
        }
        if (pickUpDistance == 0) {
            return "";
        }
        String result = "~";
        if (pickUpDistance < 1000) result += new DecimalFormat("##0").format(pickUpDistance) + " м";
        else {
            result += new DecimalFormat("##0.0").format(pickUpDistance / 1000.0) + " км";
        }
        return result;
    }

    public LatLng getPoint() {
        if (routePoints != null)
            if (routePoints.size() > 0) {
                return routePoints.get(0).getLatLng();
            }
        return null;
    }

    public String getFirstPointInfo() {
        if (routePoints != null)
            if (routePoints.size() > 0) {
                return routePoints.get(0).getName();
            }
        return null;
    }

    public String getSecondPointInfo() {
        if (routePoints != null)
            if (routePoints.size() > 1) {
                return routePoints.get(1).getName();
            }
        return null;
    }

    public String getLastPointInfo() {
        if (routePoints != null)
            if (routePoints.size() > 1) {
                return routePoints.get(routePoints.size() - 1).getName();
            }
        return null;
    }

    public String getRoute() {
        StringBuilder result = new StringBuilder();
        for (int itemID = 0; itemID < routePoints.size(); itemID++) {
            result.append(routePoints.get(itemID).getName());
            if (itemID != (routePoints.size() - 1)) {
                result.append("->");
            }
        }
        return result.toString();
    }

    public int getRouteCount() {
        return routePoints.size();
    }

    public RoutePoint getRoutePoint(int itemID) {
        if (itemID == routePoints.size()) return null;
        if (routePoints.size() < itemID) return null;
        if (routePoints.size() == 0) return null;
        return routePoints.get(itemID);
    }


}
