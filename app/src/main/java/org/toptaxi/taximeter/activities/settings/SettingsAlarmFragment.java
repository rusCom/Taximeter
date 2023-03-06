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
    protected static String TAG = "#########" + SettingsAlarmFragment.class.getName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsAlarmFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsAlarmFragment newInstance(String param1, String param2) {
        SettingsAlarmFragment fragment = new SettingsAlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
            case 1:
                seekBarNewOrderDistance.setProgress(0);
                break;
            case 2:
                seekBarNewOrderDistance.setProgress(1);
                break;
            case 3:
                seekBarNewOrderDistance.setProgress(2);
                break;
            case 5:
                seekBarNewOrderDistance.setProgress(3);
                break;
            case 10:
                seekBarNewOrderDistance.setProgress(4);
                break;
            case 15:
                seekBarNewOrderDistance.setProgress(5);
                break;
            case -1:
                seekBarNewOrderDistance.setProgress(6);
                break;
        }

        switch (MainApplication.getInstance().getPreferences().getNewOrderAlarmCost()) {
            case 100:
                seekBarNewOrderCost.setProgress(0);
                break;
            case 300:
                seekBarNewOrderCost.setProgress(1);
                break;
            case 500:
                seekBarNewOrderCost.setProgress(2);
                break;
            case 1500:
                seekBarNewOrderCost.setProgress(3);
                break;
            case 3000:
                seekBarNewOrderCost.setProgress(4);
                break;
        }
        seekBarFreeOrderCountValue = MainApplication.getInstance().getProfile().taximeterFreeOrderCount;
        switch (MainApplication.getInstance().getProfile().taximeterFreeOrderCount) {
            case 10:
                seekBarFreeOrderCount.setProgress(0);
                break;
            case 15:
                seekBarFreeOrderCount.setProgress(1);
                break;
            case 20:
                seekBarFreeOrderCount.setProgress(2);
                break;
            case 30:
                seekBarFreeOrderCount.setProgress(3);
                break;
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
            case 0:
                tvNewOrderDistance.setText("1 км.");
                distance = 1;
                break;
            case 1:
                tvNewOrderDistance.setText("2 км.");
                distance = 2;
                break;
            case 2:
                tvNewOrderDistance.setText("3 км.");
                distance = 3;
                break;
            case 3:
                tvNewOrderDistance.setText("5 км.");
                distance = 5;
                break;
            case 4:
                tvNewOrderDistance.setText("10 км.");
                distance = 10;
                break;
            case 5:
                tvNewOrderDistance.setText("15 км.");
                distance = 15;
                break;
            case 6:
                tvNewOrderDistance.setText("Все");
                break;
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
            case 0:
                tvFreeOrderCountValue.setText("10");
                seekBarFreeOrderCountValue = 10;
                break;
            case 1:
                tvFreeOrderCountValue.setText("15");
                seekBarFreeOrderCountValue = 15;
                break;
            case 2:
                tvFreeOrderCountValue.setText("20");
                seekBarFreeOrderCountValue = 20;
                break;
            case 3:
                tvFreeOrderCountValue.setText("30");
                seekBarFreeOrderCountValue = 30;
                break;
        }

        MainApplication.getInstance().getPreferences().setNewOrderAlarm(switchNewOrder.isChecked(), distance, cost);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogService.getInstance().log(this, "onPause");
        MainApplication.getInstance().getProfile().setData(switchPushNotifications.isChecked(), seekBarFreeOrderCountValue);
        if (!switchPushNotifications.isChecked()){
            MainApplication.getInstance().getFirebaseService().getNewPushToken();
        }
    }
}
