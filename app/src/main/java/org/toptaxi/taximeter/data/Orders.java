package org.toptaxi.taximeter.data;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Orders {
    protected static String TAG = "#########" + Orders.class.getName();
    private OnOrdersChangeListener onOrdersChangeListener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final MediaPlayer mp;
    private final List<Order> orderList;

    public interface OnOrdersChangeListener {
        void OnOrdersChange();
    }

    public Orders() {
        mp = MediaPlayer.create(MainApplication.getInstance(), R.raw.new_order_view);
        mp.setLooping(false);
        orderList = new ArrayList<>();
    }

    public void setOnOrdersChangeListener(OnOrdersChangeListener onOrdersChangeListener) {
        this.onOrdersChangeListener = onOrdersChangeListener;
    }

    public void setFromJSONPrior(JSONArray data) throws JSONException {
        orderList.clear();
        for (int itemID = 0; itemID < data.length(); itemID++) {
            JSONObject orderJSON = data.getJSONObject(itemID);
            Order order = new Order(orderJSON);
            orderList.add(order);
        }
    }

    public void setFromJSON(JSONArray data) throws JSONException {
        for (int itemID = 0; itemID < data.length(); itemID++) {
            JSONObject orderJSON = data.getJSONObject(itemID);
            Order sOrder = getByOrderID(orderJSON.getInt("id"));
            if (sOrder == null) {
                Order order = new Order(orderJSON);
                orderList.add(order);
                order.setNew(true);
                order.setLastRequestUID(MainApplication.getInstance().lastRequestUID);
            } else {
                sOrder.setFromJSON(orderJSON);
                sOrder.setNew(false);
                sOrder.setLastRequestUID(MainApplication.getInstance().lastRequestUID);
            }

        }

        for (int itemID = 0; itemID < orderList.size(); itemID++) {
            Order sOrder = orderList.get(itemID);
            if (sOrder.getLastRequestUID() != MainApplication.getInstance().lastRequestUID) {
                orderList.remove(sOrder);
            }
        }

        try {
            orderList.sort(Comparator.comparing(Order::getPickUpDistance));
        } catch (Throwable ignored) {
        }

        // Если по какой-либо причине данные по водителю не загружены, то загружаем их. Бывает такое, когда из спящего режима востанавливается приложение
        if (!MainApplication.getInstance().getMainAccount().isParsedData) {
            JSONObject result = MainApplication.getInstance().getRestService().httpGet("/profile/auth");
            JSONObject authData = result.getJSONObject("result");
            MainApplication.getInstance().parseData(authData);
        }


        if (MainApplication.getInstance().getMainAccount().getStatus() != null) {
            // Если водитель не на заказе и включено, что надо озвучивать поступление заказа
            if ((MainApplication.getInstance().getMainAccount().getStatus() != 2) & (MainApplication.getInstance().getPreferences().getNewOrderAlarmCheck())) {
                // Провеяем "новые" заказы на необходимость озвучивать
                boolean isNewOrderAlarm = false;
                for (int itemID = 0; itemID < orderList.size(); itemID++) {
                    Order order = orderList.get(itemID);
                    if (order.isNew()) {
                        if ((order.getCost() >= MainApplication.getInstance().getPreferences().getNewOrderAlarmCost()) &
                                (
                                        MainApplication.getInstance().getPreferences().getNewOrderAlarmDistance() == -1 |
                                                order.getPickUpDistance() <= (MainApplication.getInstance().getPreferences().getNewOrderAlarmDistance() * 1000)
                                )
                        ) {
                            isNewOrderAlarm = true;
                        }
                    }
                }
                if (isNewOrderAlarm) mp.start();
            }

        }
        if (onOrdersChangeListener != null) {
            uiHandler.post(() -> onOrdersChangeListener.OnOrdersChange());
        }
    }

    public Integer getOrderID(int itemID) {
        if (orderList.size() < itemID) return null;
        if (orderList.size() == 0) return null;
        if (itemID < 0) return null;
        return orderList.get(itemID).getID();
    }

    public Order getByOrderID(Integer OrderID) {
        for (int itemID = 0; itemID < orderList.size(); itemID++) {
            Order sOrder = orderList.get(itemID);
            if (sOrder.getID().equals(OrderID)) return sOrder;
        }
        return null;
    }

    public int getCount() {
        return orderList.size();
    }

    public Order getOrder(int position) {
        if (orderList.size() == 0) return null;
        if (orderList.size() < position) return null;
        return orderList.get(position);

    }
}
