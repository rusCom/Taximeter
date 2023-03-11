package org.toptaxi.taximeter.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.TariffPlan;

import java.util.List;


public class MainActionUnlimitedTariffPlanAdapter extends ArrayAdapter<TariffPlan> {
    Context mContext;
    int layoutResourceId;
    List<TariffPlan> mainActionItems;

    public MainActionUnlimitedTariffPlanAdapter(Context context, int resource, List<TariffPlan> objects) {
        super(context, resource, objects);
        mContext = context;
        layoutResourceId = resource;
        mainActionItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }
        TariffPlan mainActionItem = mainActionItems.get(position);
        TextView tvMainActionTitle = (TextView)convertView.findViewById(R.id.tvMainActionUnlimTitle);
        tvMainActionTitle.setText(mainActionItem.Name);
        tvMainActionTitle.setTag(mainActionItem);
        return convertView;
    }
}
