package org.toptaxi.taximeter.adapters;


import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Message;

public class ListViewMessageAdapter extends BaseAdapter {
    protected static String TAG = "#########" + ListViewMessageAdapter.class.getName();
    Context mContext;
    LayoutInflater lInflater;

    public ListViewMessageAdapter(Context mContext) {
        this.mContext = mContext;
        lInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return MainApplication.getInstance().getMainMessages().getCount();
    }

    @Override
    public Message getItem(int position) {
        return MainApplication.getInstance().getMainMessages().getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_messages, parent, false);
        }
        Message message = MainApplication.getInstance().getMainMessages().getItem(position);

        LinearLayout layout = (LinearLayout) view
                .findViewById(R.id.bubble_layout);
        /*
        LinearLayout parent_layout = (LinearLayout) view
                .findViewById(R.id.bubble_layout_parent);
                */
        //Log.d(TAG, "getView position=" + position + ";message.ID=" + message.ID +";message.Route=" + message.Route);

        if (message.Route == 0){
            layout.setBackgroundResource(R.drawable.out_message_bg);
        }
        else {
            layout.setBackgroundResource(R.drawable.in_message_bg);
        }


        ((TextView)view.findViewById(R.id.tvItemMessageText)).setText(message.Text);
        ((TextView)view.findViewById(R.id.tvItemMessageRegDate)).setText(message.RegDate);
        ((TextView)view.findViewById(R.id.tvItemMessageType)).setText(message.Type);
        if (message.Status == 5){
            ((TextView)view.findViewById(R.id.tvItemMessageText)).setTypeface(((TextView)view.findViewById(R.id.tvItemMessageText)).getTypeface(), Typeface.NORMAL);
        }
        else {
            ((TextView)view.findViewById(R.id.tvItemMessageText)).setTypeface(((TextView)view.findViewById(R.id.tvItemMessageText)).getTypeface(), Typeface.BOLD_ITALIC);
            MainApplication.getInstance().getMainMessages().setRead(message.ID);
        }

        return view;
    }


}
