package org.toptaxi.taximeter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;

import java.util.ArrayList;

public class HisOrdersAdapters extends BaseAdapter {
    protected static String TAG = "#########" + HisOrdersAdapters.class.getName();
    private final LayoutInflater lInflater;
    private final ArrayList<Order> orders;
    private Integer LastID = 0;



    public HisOrdersAdapters(MainAppCompatActivity mainAppCompatActivity) {
        lInflater = (LayoutInflater) mainAppCompatActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        orders = new ArrayList<>();
    }

    public void AppendNewData(ArrayList<Order> data) {
        orders.addAll(data);
    }


    public ArrayList<Order> LoadMore() {
        ArrayList<Order> results = new ArrayList<>();
        try {
            JSONObject response = MainApplication.getInstance().getRestService().httpGet("/last/his_orders?last_id=" + LastID);
            if (response.getString("status").equals("OK")) {
                JSONArray hisOrdersArray = response.getJSONArray("result");
                for (int itemID = 0; itemID < hisOrdersArray.length(); itemID++) {
                    Order order = new Order();
                    order.setFromJSON(hisOrdersArray.getJSONObject(itemID));
                    results.add(order);
                    LastID = order.getID();
                }
            }
        } catch (JSONException ignored) {
        }
        return results;
    }


    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Order getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.card_view_cur_orders_list, parent, false);
        }

        Order order = orders.get(position);
        order.fillListOrderData(view);

        return view;
    }

}
