package org.toptaxi.taximeter.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.Order;

public class CheckOrderOnCompleteDialog extends Dialog {
    public interface OnCheckOrderOnComplete {
        void OnCheckOrder(Order curOrder);
    }

    private Order curOrder;

    private OnCheckOrderOnComplete onCheckOrderOnComplete;

    public CheckOrderOnCompleteDialog(Context context) {
        super(context);
        this.setContentView(R.layout.dialog_check_order_on_complite);
        this.setCanceledOnTouchOutside(false);
        findViewById(R.id.btnCheckOrderOnComplete).setOnClickListener(view -> {
            if (onCheckOrderOnComplete != null) {
                onCheckOrderOnComplete.OnCheckOrder(curOrder);
                dismiss();
            }
        });
    }

    public void setOnCheckOrderOnComplete(OnCheckOrderOnComplete onCheckOrderOnComplete) {
        this.onCheckOrderOnComplete = onCheckOrderOnComplete;
    }

    public void SetOrderInfo(Order order) {
        curOrder = order;
        TextView tvOrder = findViewById(R.id.tvCheckOrderOnCompleteOrderInfo);
        String text = "Принять заказ по маршруту:<br><b>" + order.getRoute() + "</b>";
        if (!order.getNote().equals("")) text += "<br>" + order.getNote();
        text += "<br>на сумму <b>" + order.getCalcType() + "</b>";
        text += "<br><b><i>Убедитесь, что клиент не продолжает маршрут</i></b>";
        tvOrder.setText(MainApplication.fromHtml(text));
    }
}
