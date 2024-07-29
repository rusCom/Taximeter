package org.toptaxi.taximeter.adapters;


import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.data.RoutePoint;

public class RoutePointsAdapter extends RecyclerView.Adapter<RoutePointsAdapter.RouteViewHolder> {
    private final Order viewOrder;
    private boolean isItemClick = true;

    public RoutePointsAdapter(Order order, boolean isItemClick) {
        this.viewOrder = order;
        this.isItemClick = isItemClick;
    }



    @Override
    public int getItemCount() {
        if (viewOrder == null) return 0;
        return viewOrder.getRouteCount();
    }

    @NonNull
    @Override
    public RoutePointsAdapter.RouteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_route_point, viewGroup, false);
        return new RoutePointsAdapter.RouteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutePointsAdapter.RouteViewHolder routeViewHolder, final int position) {
        RoutePoint routePoint = viewOrder.getRoutePoint(position);
        if (routePoint != null) {
            routeViewHolder.tvName.setText(viewOrder.getRoutePoint(position).getName());
            if (position == 0) {
                routeViewHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_conformation_pickup, 0, 0, 0);
            } else if (position == (viewOrder.getRouteCount() - 1)) {
                routeViewHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_conformation_destination, 0, 0, 0);
            } else {
                routeViewHolder.tvName.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_conformation_address, 0, 0, 0);
            }
            routeViewHolder.tvName.setOnClickListener(view -> onItemClick(routePoint));
        }

    }

    private void onItemClick(RoutePoint routePoint) {
        if (!isItemClick)return;
        final CharSequence[] items = {"Показать маршрут до " + routePoint.getName()};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
        builder.setItems(items, (dialog, item) -> {
            if (item == 0) {
                try {
                    Uri uri = Uri.parse("dgis://2gis.ru/routeSearch/rsType/car/to/" + routePoint.getLongitude() + "," + routePoint.getLatitude());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    MainApplication.getInstance().getMainActivity().startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=ru.dublgis.dgismobile"));
                    MainApplication.getInstance().getMainActivity().startActivity(intent);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        RouteViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemRoutePointName);
        }
    }
}
