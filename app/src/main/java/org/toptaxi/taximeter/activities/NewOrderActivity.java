package org.toptaxi.taximeter.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewOrderActivity extends MainAppCompatActivity implements OnMapReadyCallback {
    private static String TAG = "#########" + NewOrderActivity.class.getName();
    Button btnApplyOrder, btnDenyOrder;
    MediaPlayer mp;
    TextView tvTimer;
    MyTimer timer;
    Order viewOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogService.getInstance().log(this, "onCreate");
        setContentView(R.layout.activity_new_order);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapNewOrder);
        mapFragment.getMapAsync(this);

        Order viewOrder = MainApplication.getInstance().getNewOrder();



        btnApplyOrder = findViewById(R.id.btnOrderMainAction);
        btnDenyOrder = findViewById(R.id.btnOrderAction);
        tvTimer = findViewById(R.id.tvOrderTimer);

        tvTimer.setVisibility(View.VISIBLE);
        btnApplyOrder.setText("Принять");
        btnDenyOrder.setText("Отказаться");

        btnApplyOrder.setVisibility(View.VISIBLE);
        btnDenyOrder.setVisibility(View.VISIBLE);

        btnApplyOrder.setOnClickListener(view -> {
            httpGetResult("/last/orders/apply");
        });

        btnDenyOrder.setOnClickListener(view -> {
            httpGetResult("/last/orders/deny");
        });

        viewOrder.fillCurOrderViewData(this, getWindow().getDecorView().findViewById(android.R.id.content));



        mp = MediaPlayer.create(this, R.raw.new_order_activity);
        mp.setLooping(true);
        mp.start();
        timer = new MyTimer(viewOrder.getNewOrderTimer(), 1000);
        timer.start();
    }

    @Override
    protected void httpGetResult(String path) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            MainApplication.getInstance().getRestService().httpGet(this, path);
            finish();
        });
    }

    public class MyTimer extends CountDownTimer {

        MyTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            httpGetResult("/last/orders/deny");
        }

        public void onTick(long millisUntilFinished) {
            tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            MainApplication.getInstance().getNewOrder().setNewOrderTimer(millisUntilFinished);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogService.getInstance().log(this, "onDestroy");
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
        // viewOrder.fillCurOrderViewData(this, getWindow().getDecorView().findViewById(android.R.id.content));
    }
}
