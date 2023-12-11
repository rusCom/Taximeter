package org.toptaxi.taximeter.activities.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.services.LogService;


public class SettingsAlarmFragment extends Fragment {
    SeekBar seekBarNewOrderDistance;
    SeekBar seekBarNewOrderCost;
    SeekBar seekBarFreeOrderCount;
    SwitchCompat switchNewOrder;
    SwitchCompat switchPushNotifications;
    TextView tvNewOrderDistance;
    TextView tvNewOrderCost;
    TextView tvFreeOrderCountValue;

    Integer seekBarFreeOrderCountValue;


    public SettingsAlarmFragment() {
        // Required empty public constructor
    }

    public static SettingsAlarmFragment newInstance() {
        SettingsAlarmFragment fragment = new SettingsAlarmFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_alarm, container, false);
        seekBarNewOrderDistance = view.findViewById(R.id.seekBarNewOrderDistance);
        seekBarNewOrderCost = view.findViewById(R.id.seekBarNewOrderCost);
        seekBarFreeOrderCount = view.findViewById(R.id.seekBarFreeOrderCount);
        switchNewOrder = view.findViewById(R.id.switchNewOrder);
        switchPushNotifications = view.findViewById(R.id.switchPushNotifications);
        tvNewOrderDistance = view.findViewById(R.id.tvNewOrderDistance);
        tvNewOrderCost = view.findViewById(R.id.tvNewOrderCost);
        tvNewOrderCost = view.findViewById(R.id.tvNewOrderCost);
        tvFreeOrderCountValue = view.findViewById(R.id.tvFreeOrderCountValue);

        switchNewOrder.setChecked(MainApplication.getInstance().getPreferences().getNewOrderAlarmCheck());
        switchPushNotifications.setChecked(MainApplication.getInstance().getProfile().pushNotificationActive);
        switch (MainApplication.getInstance().getPreferences().getNewOrderAlarmDistance()) {
            case 1 -> seekBarNewOrderDistance.setProgress(0);
            case 2 -> seekBarNewOrderDistance.setProgress(1);
            case 3 -> seekBarNewOrderDistance.setProgress(2);
            case 5 -> seekBarNewOrderDistance.setProgress(3);
            case 10 -> seekBarNewOrderDistance.setProgress(4);
            case 15 -> seekBarNewOrderDistance.setProgress(5);
            case -1 -> seekBarNewOrderDistance.setProgress(6);
        }

        switch (MainApplication.getInstance().getPreferences().getNewOrderAlarmCost()) {
            case 100 -> seekBarNewOrderCost.setProgress(0);
            case 300 -> seekBarNewOrderCost.setProgress(1);
            case 500 -> seekBarNewOrderCost.setProgress(2);
            case 1500 -> seekBarNewOrderCost.setProgress(3);
            case 3000 -> seekBarNewOrderCost.setProgress(4);
        }
        seekBarFreeOrderCountValue = MainApplication.getInstance().getProfile().taximeterFreeOrderCount;
        switch (MainApplication.getInstance().getProfile().taximeterFreeOrderCount) {
            case 10 -> seekBarFreeOrderCount.setProgress(0);
            case 15 -> seekBarFreeOrderCount.setProgress(1);
            case 20 -> seekBarFreeOrderCount.setProgress(2);
            case 30 -> seekBarFreeOrderCount.setProgress(3);
        }

        seekBarNewOrderDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newOrderSetData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarNewOrderCost.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newOrderSetData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarFreeOrderCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newOrderSetData();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        switchNewOrder.setOnCheckedChangeListener((buttonView, isChecked) -> newOrderSetData());
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> newOrderSetData());

        newOrderSetData();
        return view;
    }


    @SuppressLint("SetTextI18n")
    void newOrderSetData() {
        seekBarNewOrderDistance.setEnabled(switchNewOrder.isChecked());
        seekBarNewOrderCost.setEnabled(switchNewOrder.isChecked());
        int distance = -1, cost = 100;
        switch (seekBarNewOrderDistance.getProgress()) {
            case 0 -> {
                tvNewOrderDistance.setText("1 км.");
                distance = 1;
            }
            case 1 -> {
                tvNewOrderDistance.setText("2 км.");
                distance = 2;
            }
            case 2 -> {
                tvNewOrderDistance.setText("3 км.");
                distance = 3;
            }
            case 3 -> {
                tvNewOrderDistance.setText("5 км.");
                distance = 5;
            }
            case 4 -> {
                tvNewOrderDistance.setText("10 км.");
                distance = 10;
            }
            case 5 -> {
                tvNewOrderDistance.setText("15 км.");
                distance = 15;
            }
            case 6 -> tvNewOrderDistance.setText("Все");
        }
        switch (seekBarNewOrderCost.getProgress()) {
            case 0:
                break;
            case 1:
                cost = 300;
                break;
            case 2:
                cost = 500;
                break;
            case 3:
                cost = 1500;
                break;
            case 4:
                cost = 3000;
                break;
        }
        tvNewOrderCost.setText(cost + " руб.");

        switch (seekBarFreeOrderCount.getProgress()) {
            case 0 -> {
                tvFreeOrderCountValue.setText("10");
                seekBarFreeOrderCountValue = 10;
            }
            case 1 -> {
                tvFreeOrderCountValue.setText("15");
                seekBarFreeOrderCountValue = 15;
            }
            case 2 -> {
                tvFreeOrderCountValue.setText("20");
                seekBarFreeOrderCountValue = 20;
            }
            case 3 -> {
                tvFreeOrderCountValue.setText("30");
                seekBarFreeOrderCountValue = 30;
            }
        }

        MainApplication.getInstance().getPreferences().setNewPreferencesData(switchNewOrder.isChecked(), distance, cost, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogService.getInstance().log(this, "onPause");
        MainApplication.getInstance().getProfile().setData(switchPushNotifications.isChecked(), seekBarFreeOrderCountValue);
    }
}
