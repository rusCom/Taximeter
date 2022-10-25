package org.toptaxi.taximeter.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;

public class RoutePointsAdapter extends RecyclerView.Adapter<RoutePointsAdapter.RouteViewHolder>{
    //protected static String TAG = "#########" + RoutePointsAdapter.class.getName();
    private Order viewOrder = null;
    public RoutePointsAdapter() {

    }

    public void setOrder(Order order){
        viewOrder = order;
    }

    @Override
    public int getItemCount(){
        if (viewOrder == null)return 0;
        return viewOrder.getRouteCount();
    }

    @Override
    public RoutePointsAdapter.RouteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_route_point, viewGroup, false);
        return new RoutePointsAdapter.RouteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RoutePointsAdapter.RouteViewHolder routeViewHolder, final int position) {
        if (viewOrder != null)
            if (position < getItemCount())
                if (viewOrder.getRoutePoint(position) != null)
                routeViewHolder.tvName.setText(viewOrder.getRoutePoint(position).getName());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        RouteViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView)itemView.findViewById(R.id.tvItemRoutePointName);
        }
    }
}
