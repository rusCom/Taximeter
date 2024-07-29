package org.toptaxi.taximeter.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.RoutePointsAdapter;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.data.RoutePoint;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;

public class HisOrderActivity extends MainAppCompatActivity {
    Order viewOrder;
    RecyclerView rvRoutePoints;
    RoutePointsAdapter routePointsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_his_order);

        viewOrder = MainApplication.getInstance().getHisOrderView();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Заказ № " + viewOrder.getID());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHisOrder);

        mapFragment.getMapAsync(this::init);

        /*
        rvRoutePoints = findViewById(R.id.rvOrderDataRoutePoints);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvRoutePoints.setLayoutManager(linearLayoutManager);
        routePointsAdapter = new RoutePointsAdapter(viewOrder);
        rvRoutePoints.setAdapter(routePointsAdapter);

         */
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void init(GoogleMap googleMap) {
        if ((googleMap != null) && (viewOrder != null)) {
            googleMap.clear();
            if (viewOrder.getRouteCount() == 1) {
                RoutePoint routePoint = viewOrder.getRoutePoint(0);
                googleMap.addMarker(new MarkerOptions().
                        position(routePoint.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_onboard_from))
                );
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routePoint.getLatLng(), 15));
            } else if (viewOrder.getRouteCount() == 0) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainApplication.getInstance().getLocationService().getLatLng(), 15));
            } else {
                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                int size = this.getResources().getDisplayMetrics().widthPixels;
                for (int itemID = 0; itemID < viewOrder.getRouteCount(); itemID++) {
                    RoutePoint routePoint = viewOrder.getRoutePoint(itemID);
                    if (itemID == 0)
                        googleMap.addMarker(new MarkerOptions().position(routePoint.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_onboard_pick_up)));
                    else if (itemID == (viewOrder.getRouteCount() - 1))
                        googleMap.addMarker(new MarkerOptions().position(routePoint.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_onboard_destination)));
                    else googleMap.addMarker(new MarkerOptions().position(routePoint.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_onboard_address)));
                    latLngBuilder.include(routePoint.getLatLng());
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), size, size, 200));
            }

            // routePointsAdapter.setOrder(viewOrder);
            // routePointsAdapter.notifyItemRangeInserted(0, viewOrder.getRouteCount());
            // routePointsAdapter.notifyDataSetChanged();
            // findViewById(R.id.llCurOrderTitleEx).setVisibility(View.GONE);
            viewOrder.fillCurOrderViewData(this, getWindow().getDecorView().findViewById(android.R.id.content), false);
        }

    }
}
