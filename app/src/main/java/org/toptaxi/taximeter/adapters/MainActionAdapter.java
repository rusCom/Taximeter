package org.toptaxi.taximeter.adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.MainActionItem;

import java.util.List;

public class MainActionAdapter extends ArrayAdapter<MainActionItem>{
    Context mContext;
    int layoutResourceId;
    List<MainActionItem> mainActionItems;

    public MainActionAdapter(Context context, int resource, List<MainActionItem> objects) {
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
        MainActionItem mainActionItem = mainActionItems.get(position);
        TextView tvMainActionTitle = (TextView)convertView.findViewById(R.id.tvMainActionTitle);
        tvMainActionTitle.setText(mainActionItem.getActionName());
        tvMainActionTitle.setTag(mainActionItem);
        return convertView;
    }
}
