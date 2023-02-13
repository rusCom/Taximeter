package org.toptaxi.taximeter.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RVCurOrdersAdapter;
import org.toptaxi.taximeter.adapters.RecyclerItemClickListener;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;
import org.toptaxi.taximeter.tools.OnPriorOrdersChange;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriorOrderActivity extends MainAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, OnPriorOrdersChange {
    RecyclerView rvPriorOrders;
    RVCurOrdersAdapter curOrdersAdapter;
    Calendar ServerDate;
    SimpleDateFormat simpleDateFormat;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prior_order);

        rvPriorOrders = (RecyclerView)findViewById(R.id.rvPriorOrders);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvPriorOrders.setLayoutManager(llm);
        curOrdersAdapter = new RVCurOrdersAdapter(1);
        rvPriorOrders.setAdapter(curOrdersAdapter);
        rvPriorOrders.addOnItemTouchListener(new RecyclerItemClickListener(this, this));

        ServerDate = MainApplication.getInstance().getServerDate();
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd.MM:yyyy", Locale.getDefault());
        setTitle(simpleDateFormat.format(ServerDate.getTime()));
    }

    @Override
    public void onItemClick(View view, int position) {
        final Order curOrder = MainApplication.getInstance().getPriorOrders().getOrder(position);
        if (curOrder != null){
            String alertText = "Принять предварительный заказ " + curOrder.getPriorInfo() + " по маршруту " + curOrder.getRoute();
            if (!curOrder.getNote().equals("")){
                alertText += "(" + curOrder.getNote() + ")";
            }
            LogService.getInstance().log(this, alertText);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Внимание");
            alertDialog.setMessage(alertText);
            alertDialog.setPositiveButton("Да", (dialogInterface, i) -> {
                checkOrder(curOrder.getID());
            });
            alertDialog.setNegativeButton("Нет" , null);
            alertDialog.create();
            alertDialog.show();
        }
    }

    private void checkOrder(Integer orderID){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            JSONObject response = MainApplication.getInstance().getRestService().httpGet(this, "/last/orders/check?order_id=" + orderID);
            if (MainUtils.JSONGetString(response, "status").equals("OK")){
                this.finish();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            LogService.getInstance().log(this, "stopTimer");
        }
        MainApplication.getInstance().setOnPriorOrdersChange(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTimer != null) {
            mTimer.cancel();
        }

        // re-schedule timer here
        // otherwise, IllegalStateException of
        // "TimerTask is scheduled already"
        // will be thrown
        ServerDate = MainApplication.getInstance().getServerDate();
        mTimer = new Timer();
        MyTimerTask mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 1000, 1000);
        LogService.getInstance().log(this, "startTimer");
        OnPriorOrdersChange();
        MainApplication.getInstance().setOnPriorOrdersChange(this);
    }

    @Override
    public void OnPriorOrdersChange() {
        LogService.getInstance().log(this, "OnPriorOrdersChange count = " + MainApplication.getInstance().getPriorOrders().getCount());
        ServerDate = MainApplication.getInstance().getServerDate();
        curOrdersAdapter.notifyDataSetChanged();
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            ServerDate.add(Calendar.SECOND, 1);
            final String strDate = simpleDateFormat.format(ServerDate.getTime());
            runOnUiThread(() -> setTitle(strDate));
        }
    }
}
