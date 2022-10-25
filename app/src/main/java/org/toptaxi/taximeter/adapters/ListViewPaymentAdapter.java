package org.toptaxi.taximeter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Payment;

import java.util.ArrayList;

public class ListViewPaymentAdapter extends BaseAdapter {
    protected static String TAG = "#########" + ListViewMessageAdapter.class.getName();
    Context mContext;
    LayoutInflater lInflater;
    private final ArrayList<Payment> payments;
    String LastID = "0";
    String viewType;
    String path = "/payments?last_id=";


    public ListViewPaymentAdapter(Context mContext, String viewType) {
        this.mContext = mContext;
        this.viewType = viewType;
        lInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        payments = new ArrayList<>();
        if (viewType.equals("corporate")){
            path = "/payments/corporate?last_id=";
        }
    }

    public void AppendNewData(ArrayList<Payment> data) {
        payments.addAll(data);
    }


    public ArrayList<Payment> LoadMore() {
        ArrayList<Payment> results = new ArrayList<>();
        try {
            JSONObject response = MainApplication.getInstance().getRestService().httpGet(path + LastID);
            if (response.getString("status").equals("OK")) {
                JSONArray paymentsJSON = response.getJSONArray("result");
                for (int itemID = 0; itemID < paymentsJSON.length(); itemID++) {
                    Payment payment = new Payment(paymentsJSON.getJSONObject(itemID));
                    results.add(payment);
                    LastID = payment.ID;
                }
            }
        } catch (JSONException ignored) {
        }
        return results;
    }


    @Override
    public int getCount() {
        return payments.size();
    }

    @Override
    public Payment getItem(int position) {
        return payments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_balance, parent, false);
        }
        Payment payment = payments.get(position);

        LinearLayout layout = view
                .findViewById(R.id.bubble_layout);


        if (payment.Summa < 0) {
            layout.setBackgroundResource(R.drawable.out_message_bg);
        } else {
            layout.setBackgroundResource(R.drawable.in_message_bg);
        }

        if (payment.Name.equals("")) {
            view.findViewById(R.id.tvItemBalanceName).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.tvItemBalanceName).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.tvItemBalanceName)).setText(payment.Name);
        }

        if (payment.Note.equals("")) {
            view.findViewById(R.id.tvItemBalanceComment).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.tvItemBalanceComment).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.tvItemBalanceComment)).setText(payment.Note);
        }

        ((TextView) view.findViewById(R.id.tvItemBalanceDate)).setText(payment.Date);

        String balance = "Сумма: " + payment.getSummaString() + " руб. Баланс: " + payment.getBalanceString() + " руб.";
        ((TextView) view.findViewById(R.id.tvItemBalanceSumma)).setText(balance);
        return view;
    }
}
