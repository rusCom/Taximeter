package org.toptaxi.taximeter.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.tools.MainUtils;

public class RVCurOrdersAdapter extends RecyclerView.Adapter<RVCurOrdersAdapter.OrderViewHolder> {
    protected static String TAG = "#########" + RVCurOrdersAdapter.class.getName();
    private int mOrderType;

    public RVCurOrdersAdapter(int orderType) {
        mOrderType = orderType;
    }

    @Override
    public int getItemCount() {
        switch (mOrderType){
            case 0:return MainApplication.getInstance().getCurOrders().getCount();
            case 1:return MainApplication.getInstance().getPriorOrders().getCount();
            case 2:return MainApplication.getInstance().getCompleteOrders().getCount();
        }
        return MainApplication.getInstance().getCurOrders().getCount();

    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_cur_orders_list, viewGroup, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder orderViewHolder, final int position) {
        Order curOrder = null;
        switch (mOrderType){
            case 0:curOrder = MainApplication.getInstance().getCurOrders().getOrder(position);break;
            case 1:curOrder = MainApplication.getInstance().getPriorOrders().getOrder(position);break;
            case 2:curOrder = MainApplication.getInstance().getCompleteOrders().getOrder(position);break;
        }

        if (curOrder != null) {
            orderViewHolder.tvPayType.setText(curOrder.getPayTypeName());
            orderViewHolder.tvCalcType.setText(curOrder.getCalcType());
            orderViewHolder.tvDistance.setText(curOrder.getDistanceString());
            orderViewHolder.tvFirstPointInfo.setText(curOrder.getFirstPointInfo());

            MainUtils.TextViewSetTextOrGone(orderViewHolder.tvDispPay, curOrder.getDispatchingCommission());

            orderViewHolder.tvPointInfo.setVisibility(View.GONE);
            orderViewHolder.tvLastPointInfo.setVisibility(View.GONE);

            if (curOrder.getRouteCount() > 1){
                orderViewHolder.tvLastPointInfo.setVisibility(View.VISIBLE);
                orderViewHolder.tvLastPointInfo.setText(curOrder.getLastPointInfo());
            }
            if (curOrder.getRouteCount() == 3){
                orderViewHolder.tvPointInfo.setVisibility(View.VISIBLE);
                orderViewHolder.tvPointInfo.setText(curOrder.getSecondPointInfo());
            }
            if (curOrder.getRouteCount() > 3){
                orderViewHolder.tvPointInfo.setVisibility(View.VISIBLE);
            }
            if (curOrder.isNew() & mOrderType == 0){orderViewHolder.llTitle.setBackgroundResource(R.color.primaryRed);}
            else {
                switch (curOrder.getCheck()){
                    case 0:orderViewHolder.llTitle.setBackgroundResource(curOrder.getCaptionColor());break;
                    case 1:orderViewHolder.llTitle.setBackgroundResource(R.color.primaryGreen);break;
                    case 2:orderViewHolder.llTitle.setBackgroundResource(R.color.primaryGrayDark);break;
                }
            }

            MainUtils.TextViewSetTextOrGone(orderViewHolder.tvPrior, curOrder.getPriorInfo());

            MainUtils.TextViewSetTextOrGone(orderViewHolder.tvOrderNote,  orderViewHolder.tvOrderNoteDivider, curOrder.getNote());
            MainUtils.TextViewSetTextOrGone(orderViewHolder.tvDispatchingName, orderViewHolder.tvDispatchingNameDivider, curOrder.dispatchingName);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tvPayType, tvCalcType, tvDistance, tvFirstPointInfo, tvLastPointInfo, tvPointInfo, tvOrderNote, tvPrior, tvDispatchingName, tvDispPay;
        LinearLayout llTitle;
        View tvDispatchingNameDivider, tvOrderNoteDivider;



        OrderViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cvCurOrdersList);
            tvPayType           = itemView.findViewById(R.id.tvCurOrdersListPayType);
            tvDispPay           = itemView.findViewById(R.id.tvCurOrdersListPayPercent);
            tvCalcType          = itemView.findViewById(R.id.tvCurOrdersListCalcType);
            tvDistance          = itemView.findViewById(R.id.tvCurOrdersListDistance);
            tvFirstPointInfo    = itemView.findViewById(R.id.tvCurOrdersListRouteFirstPoint);
            tvLastPointInfo     = itemView.findViewById(R.id.tvCurOrdersListRouteLastPoint);
            tvPointInfo         = itemView.findViewById(R.id.tvCurOrdersListRoutePoint);
            tvPrior             = itemView.findViewById(R.id.tvCurOrdersListPriorInfo);
            llTitle             = itemView.findViewById(R.id.llCurOrdersListTitle);

            tvDispatchingName           = itemView.findViewById(R.id.tvDispatchingName);
            tvDispatchingNameDivider    = itemView.findViewById(R.id.tvDispatchingNameDivider);
            tvOrderNote                 = itemView.findViewById(R.id.tvOrderNote);
            tvOrderNoteDivider          = itemView.findViewById(R.id.tvOrderNoteDivider);
        }
    }
}
