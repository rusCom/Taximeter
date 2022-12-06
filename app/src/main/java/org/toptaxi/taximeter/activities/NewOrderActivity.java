package org.toptaxi.taximeter.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RoutePointsAdapter;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.tools.LockOrientation;
import org.toptaxi.taximeter.tools.MainUtils;

public class NewOrderActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static String TAG = "#########" + NewOrderActivity.class.getName();
    Button btnApplyOrder, btnDenyOrder;
    MediaPlayer mp;
    TextView tvTimer;
    MyTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_new_order);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapNewOrder);
        mapFragment.getMapAsync(this);

        Order viewOrder = MainApplication.getInstance().getNewOrder();
        btnApplyOrder = (Button) findViewById(R.id.btnOrderMainAction);
        btnDenyOrder = (Button) findViewById(R.id.btnOrderAction);
        tvTimer = (TextView) findViewById(R.id.tvOrderTimer);
        tvTimer.setText("!!!!");
        tvTimer.setVisibility(View.VISIBLE);
        btnApplyOrder.setText("Принять");
        btnDenyOrder.setText("Отказаться");
        btnDenyOrder.setBackgroundResource(R.drawable.btn_yellow);

        btnApplyOrder.setVisibility(View.VISIBLE);
        btnDenyOrder.setVisibility(View.VISIBLE);

        btnApplyOrder.setOnClickListener(view -> {
            MainApplication.getInstance().getRestService().httpGetResult("/last/orders/apply");
            finish();
        });

        btnDenyOrder.setOnClickListener(view -> {
            MainApplication.getInstance().getRestService().httpGetResult("/last/orders/deny");
            finish();
        });

        RecyclerView rvViewOrderRoutePoints = findViewById(R.id.rvViewOrderRoutePoints);
        rvViewOrderRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        RoutePointsAdapter viewOrderPointsAdapter = new RoutePointsAdapter();
        rvViewOrderRoutePoints.setAdapter(viewOrderPointsAdapter);
        viewOrderPointsAdapter.setOrder(MainApplication.getInstance().getNewOrder());
        viewOrderPointsAdapter.notifyItemRangeInserted(0, MainApplication.getInstance().getNewOrder().getRouteCount());
        viewOrderPointsAdapter.notifyDataSetChanged();

        findViewById(R.id.llViewOrderTitle).setBackgroundResource(viewOrder.getCaptionColor());

        ((TextView) findViewById(R.id.tvViewOrderDistance)).setText(viewOrder.getDistanceString());
        ((TextView) findViewById(R.id.tvViewOrderPayType)).setText(viewOrder.getPayTypeName());
        ((TextView) findViewById(R.id.tvViewOrderCalcType)).setText(viewOrder.getCalcType());
        MainUtils.TextViewSetTextOrGone(findViewById(R.id.tvViewOrderDispatchingName), viewOrder.dispatchingName);

        if (viewOrder.getPriorInfo().equals("")) {
            (findViewById(R.id.llViewOrderPriorInfo)).setVisibility(View.GONE);
        } else {
            (findViewById(R.id.llViewOrderPriorInfo)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvViewOrderPriorInfo)).setText(viewOrder.getPriorInfo());
        }

        (findViewById(R.id.llViewOrderNote)).setVisibility(View.GONE);
        if (viewOrder.getNote() != null)
            if (!viewOrder.getNote().equals("")) {
                (findViewById(R.id.llViewOrderNote)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tvViewOrderNote)).setText(viewOrder.getNote());
            }

        mp = MediaPlayer.create(this, R.raw.new_order_activity);
        mp.setLooping(true);
        mp.start();
        //tvTimer.setText("60");
        timer = new MyTimer(viewOrder.getNewOrderTimer(), 1000);
        timer.start();
    }

    public class MyTimer extends CountDownTimer {

        MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            MainApplication.getInstance().getRestService().httpGetThread("/last/orders/deny");
            finish();
        }

        public void onTick(long millisUntilFinished) {
            //Log.d(TAG, "onTimerTick");
            tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            MainApplication.getInstance().getNewOrder().setNewOrderTimer(millisUntilFinished);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
        mp.stop();
        timer.cancel();
        // Если основное окно не запущено, то запускаем
        if (MainApplication.getInstance().getMainActivity() == null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng coord = MainApplication.getInstance().getNewOrder().getPoint();
        googleMap.addMarker(new MarkerOptions().position(coord));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 15));
    }
}
