package org.toptaxi.taximeter.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;

public class RVCurOrdersAdapter extends RecyclerView.Adapter<RVCurOrdersAdapter.OrderViewHolder> {
    protected static String TAG = "#########" + RVCurOrdersAdapter.class.getName();
    private final int mOrderType;

    public RVCurOrdersAdapter(int orderType) {
        mOrderType = orderType;
    }

    @Override
    public int getItemCount() {
        return switch (mOrderType) {
            case 0 -> MainApplication.getInstance().getCurOrders().getCount();
            case 1 -> MainApplication.getInstance().getPriorOrders().getCount();
            case 2 -> MainApplication.getInstance().getCompleteOrders().getCount();
            default -> MainApplication.getInstance().getCurOrders().getCount();
        };

    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_cur_orders_list, viewGroup, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder orderViewHolder, final int position) {
        Order curOrder = switch (mOrderType) {
            case 0 -> MainApplication.getInstance().getCurOrders().getOrder(position);
            case 1 -> MainApplication.getInstance().getPriorOrders().getOrder(position);
            case 2 -> MainApplication.getInstance().getCompleteOrders().getOrder(position);
            default -> null;
        };

        if (curOrder != null) {
            curOrder.fillListOrderData(orderViewHolder.cardView);

            if (curOrder.isNew() & mOrderType == 0) {
                orderViewHolder.llTitle.setBackgroundResource(R.color.primaryRed);
            } else {
                switch (curOrder.getCheck()) {
                    case 0:
                        orderViewHolder.llTitle.setBackgroundResource(curOrder.getCaptionColor());
                        break;
                    case 1:
                        orderViewHolder.llTitle.setBackgroundResource(R.color.primaryGreen);
                        break;
                    case 2:
                        orderViewHolder.llTitle.setBackgroundResource(R.color.primaryGrayDark);
                        break;
                }
            }

        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        View cardView;

        LinearLayout llTitle;

        OrderViewHolder(View itemView) {
            super(itemView);
            cardView = itemView;

            llTitle = itemView.findViewById(R.id.llOrdersListTitle);

        }
    }
}
