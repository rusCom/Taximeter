package org.toptaxi.taximeter;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.toptaxi.taximeter.activities.OrdersOnCompleteActivity;
import org.toptaxi.taximeter.activities.mainActivity.MainActivityDrawer;
import org.toptaxi.taximeter.adapters.MainActionAdapter;
import org.toptaxi.taximeter.adapters.OnMainActionClickListener;
import org.toptaxi.taximeter.adapters.RVCurOrdersAdapter;
import org.toptaxi.taximeter.adapters.RecyclerItemClickListener;
import org.toptaxi.taximeter.data.Order;
import org.toptaxi.taximeter.data.Orders;
import org.toptaxi.taximeter.data.SupportContactItem;
import org.toptaxi.taximeter.data.TariffPlan;
import org.toptaxi.taximeter.dialogs.PaymentsDialogKT;
import org.toptaxi.taximeter.services.LocationService;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.FontFitTextView;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;
import org.toptaxi.taximeter.tools.OnMainDataChangeListener;
import org.toptaxi.taximeter.tools.bottomsheets.MainBottomSheetRecycler;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends MainAppCompatActivity implements OnMainDataChangeListener, Orders.OnOrdersChangeListener, OnMapReadyCallback {
    FrameLayout viewCurOrders, viewViewOrder, viewCurOrder, viewGPSError, viewMainData;
    TextView tvGPSStatus, tvNullCurOrderInfo;
    Button btnCurOrderMainAction, btnCurOrderAction, btnCompleteOrders, btnOrderViewBack;
    private boolean isShowGPSData = false;
    protected Toolbar mainToolbar;
    MenuItem miGPSFixed, miGPSNotFixed, miGPSOff, miDriverOffline, miDriverOnOrder, miDriverOnLine;
    RVCurOrdersAdapter curOrdersAdapter;
    public AlertDialog mainActionsDialog;
    GoogleMap googleMap;
    FontFitTextView tvCurOrderClientCost;
    int viewOrderLastState;
    MediaPlayer mpOrderStateChange;
    RecyclerView rvCurOrders;
    FloatingActionButton fabMainActions;
    MainActivityDrawer mainActivityDrawer;
    String lastCurOrderData = "", lastMainActivityCaption = "";
    int lastMainActivityCurView = -1;
    boolean isShowNullBalanceDialog = false;

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogService.getInstance().log(this, "onCreate");
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewCurOrders = findViewById(R.id.curOrdersRecyclerViewLayout);
        viewViewOrder = findViewById(R.id.flViewOrder);
        viewCurOrder = findViewById(R.id.flCurOrder);
        viewGPSError = findViewById(R.id.flGPSError);
        viewMainData = findViewById(R.id.flMainData);

        btnCurOrderMainAction = findViewById(R.id.btnCurOrderMainAction);
        btnCurOrderAction = findViewById(R.id.btnCurOrderAction);
        btnCompleteOrders = findViewById(R.id.btnCurOrderCompleteOrders);

        btnOrderViewBack = findViewById(R.id.btnOrderViewBack);
        btnOrderViewBack.setOnClickListener(v -> {
            if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_VIEW_ORDER) {
                MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
            }
        });


        btnCompleteOrders.setVisibility(View.GONE);
        tvGPSStatus = findViewById(R.id.tvGPSInfo);
        tvNullCurOrderInfo = findViewById(R.id.tvNullCurOrderInfo);
        tvNullCurOrderInfo.setVisibility(View.GONE);

        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);

        fabMainActions = findViewById(R.id.fabMainActions);
        fabMainActions.setOnClickListener(view -> fabMainActionsClick());

        rvCurOrders = findViewById(R.id.rvCurOrders);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvCurOrders.setLayoutManager(llm);
        curOrdersAdapter = new RVCurOrdersAdapter(0);
        rvCurOrders.setAdapter(curOrdersAdapter);
        rvCurOrders.addOnItemTouchListener(
                new RecyclerItemClickListener(this, (view, position) -> {
                    MainApplication.getInstance().setViewOrderID(MainApplication.getInstance().getCurOrders().getOrderID(position));
                    MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_VIEW_ORDER);
                })
        );


        tvCurOrderClientCost = findViewById(R.id.tvCurOrderClientPrice);
        (findViewById(R.id.tvOrderDataClientPhone)).setOnClickListener(view -> callIntent(((TextView) findViewById(R.id.tvOrderDataClientPhone)).getText().toString()));


        mpOrderStateChange = MediaPlayer.create(this, R.raw.order_state_change);
        mpOrderStateChange.setLooping(false);

        // ViewOrderData
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapViewOrder);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnCompleteOrders.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, OrdersOnCompleteActivity.class)));
        mainActivityDrawer = new MainActivityDrawer(this, mainToolbar);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LogService.getInstance().log(this, "onResume");
        MainApplication.getInstance().setMainActivity(this);
        mainActivityDrawer.updateDrawer();
        OnMainCurViewChange();


        // MainApplication.getInstance().getLocationService().setOnLocationDataChange(this);
        MainApplication.getInstance().setOnMainDataChangeListener(this);
        MainApplication.getInstance().getCurOrders().setOnOrdersChangeListener(this);
        onLocationDataChange();

        // Если баланс водителя нулевой, показываем окно пополнения баланса
        // LogService.getInstance().log("MainActivity", "ShowNullBalanceDialog", MainApplication.getInstance().getProfile().getBalance().toString());
        if ((MainApplication.getInstance().getProfile().isShowNullBalanceDialog())
                && (!isShowNullBalanceDialog)) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                LogService.getInstance().log("MainActivity", "ShowNullBalanceDialog", "startSleep");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
                if (!isShowNullBalanceDialog) {
                    isShowNullBalanceDialog = true;
                    runOnUiThread(this::showNullBalanceDialog);
                    LogService.getInstance().log("MainActivity", "ShowNullBalanceDialog", "showDialog");
                }
            });
        }
    }

    public void showNullBalanceDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Для продолжения работы и возможности принятия заказов необходимо пополнить баланс");
        alertDialog.setPositiveButton("Пополнить", (dialogInterface, i) -> {
            if (PaymentsDialogKT.INSTANCE.getPaymentsAvailable()) {
                PaymentsDialogKT.INSTANCE.showPaymentDialog(this);
            } else if (MainApplication.getInstance().getPreferences().getPaymentInstructionLink() != null) {
                goToURL(MainApplication.getInstance().getPreferences().getPaymentInstructionLink());
            } else {
                callIntent(MainApplication.getInstance().getPreferences().getDispatcherPhone());
            }

        });
        alertDialog.setNegativeButton("Отмена", null);
        alertDialog.create();
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogService.getInstance().log(this, "onPause");
        MainApplication.getInstance().setMainActivity(null);
    }

    @Override
    public void onBackPressed() {
        if (mainActivityDrawer.closeDrawer()) {
            return;
        }

        if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_VIEW_ORDER) {
            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
        } else {
            if (MainApplication.getInstance().getMainAccount().getStatus() != null) {
                if (MainApplication.getInstance().getMainAccount().getStatus() == Constants.DRIVER_ON_ORDER)
                    MainApplication.getInstance().showToast(getString(R.string.onCloseDriverOnOrder));
                else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Внимание");
                    alertDialog.setMessage("При закрытие программы Вы будете сняты с линии");
                    alertDialog.setPositiveButton("Сняться", (dialogInterface, i) -> {
                        MainApplication.getInstance().stopMainService();
                        MainApplication.getInstance().isRunning = false;
                        finish();
                    });
                    alertDialog.setNegativeButton("Остаться", null);
                    alertDialog.create();
                    alertDialog.show();
                }
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogService.getInstance().log(this, "onDestroy");
        MainApplication.getInstance().setOnMainDataChangeListener(null);
        MainApplication.getInstance().setMainActivity(null);
    }

    @Override
    public boolean onCreateOptionsMenu(@androidx.annotation.NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        miGPSFixed = menu.findItem(R.id.action_gps_fixed);
        miGPSNotFixed = menu.findItem(R.id.action_gps_not_fixed);
        miGPSOff = menu.findItem(R.id.action_gps_off);

        miDriverOffline = menu.findItem(R.id.action_driver_offline);
        miDriverOnLine = menu.findItem(R.id.action_driver_online);
        miDriverOnOrder = menu.findItem(R.id.action_driver_on_order);


        miGPSFixed.setVisible(false);
        miGPSNotFixed.setVisible(false);
        miGPSOff.setVisible(false);
        switch (MainApplication.getInstance().getLocationService().getGPSStatus(this)) {
            case LocationService.GPS_FIXED:
                miGPSFixed.setVisible(true);
                break;
            case LocationService.GPS_NOT_FIXED:
                miGPSNotFixed.setVisible(true);
                break;
            case LocationService.GPS_OFF:
                miGPSOff.setVisible(true);
                break;
        }
        setDriverStatusIcon(MainApplication.getInstance().getMainAccount().getStatus());

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_gps_fixed) {
            isShowGPSData = !isShowGPSData;
        }
        if (item.getItemId() == R.id.action_gps_not_fixed) {
            isShowGPSData = !isShowGPSData;
        }
        if (item.getItemId() == R.id.action_gps_off) {
            isShowGPSData = !isShowGPSData;
        }
        if (item.getItemId() == R.id.action_driver_offline) {
            driverGoOffLine();
        }
        if (item.getItemId() == R.id.action_driver_online) {
            driverGoOffLine();
        }

        if (isShowGPSData) tvGPSStatus.setVisibility(View.VISIBLE);
        else tvGPSStatus.setVisibility(View.GONE);
        return super.onOptionsItemSelected(item);
    }

    public void driverGoOffLine() {
        if (!MainApplication.getInstance().getMainAccount().getOnLine()) {
            showSimpleDialog(getString(R.string.errorDriverBalanceOnLine));
        } else {
            String alertText = "", action = "";
            switch (MainApplication.getInstance().getMainAccount().getStatus()) {
                case Constants.DRIVER_OFFLINE -> {
                    alertText = "Встать на автораздачу?";
                    action = "/driver/free";
                }
                case Constants.DRIVER_ONLINE -> {
                    alertText = "Сняться с автораздачи?";
                    action = "/driver/busy";
                }
            }
            if (!alertText.equals("")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Внимание");
                alertDialog.setMessage(alertText);
                final String finalAction = action;
                alertDialog.setPositiveButton("Да", (dialogInterface, i) -> httpGetResult(finalAction));
                alertDialog.setNegativeButton("Нет", null);
                alertDialog.create();
                alertDialog.show();
            }
        }
    }

    @Override
    public void OnMainCurViewChange() {
        if (lastMainActivityCurView != MainApplication.getInstance().getMainActivityCurView()) {
            lastMainActivityCurView = MainApplication.getInstance().getMainActivityCurView();
            LogService.getInstance().log(this, "onMainCurViewChange", String.valueOf(MainApplication.getInstance().getMainActivityCurView()));

            viewCurOrders.setVisibility(View.GONE);
            viewViewOrder.setVisibility(View.GONE);
            viewCurOrder.setVisibility(View.GONE);
            fabMainActions.setVisibility(View.GONE);

            // mainToolbar.sh


            switch (MainApplication.getInstance().getMainActivityCurView()) {
                case Constants.CUR_VIEW_CUR_ORDERS -> {
                    viewCurOrders.setVisibility(View.VISIBLE);
                    fabMainActions.setVisibility(View.VISIBLE);
                }
                case Constants.CUR_VIEW_VIEW_ORDER -> {
                    viewViewOrder.setVisibility(View.VISIBLE);
                    viewOrderLastState = MainApplication.getInstance().getViewOrder().getCheck();

                    generateViewOrder();
                }
                case Constants.CUR_VIEW_CUR_ORDER -> {
                    viewCurOrder.setVisibility(View.VISIBLE);
                    //viewOrderLastState = MainApplication.getInstance().getViewOrder().Check;
                    viewOrderLastState = -1;
                    generateCurOrder();
                }
            }
        }
        if (!lastMainActivityCaption.equals(MainApplication.getInstance().getProfile().getMainActivityCaption())) {
            lastMainActivityCaption = MainApplication.getInstance().getProfile().getMainActivityCaption();
            LogService.getInstance().log(this, "setTitle", MainApplication.getInstance().getProfile().getMainActivityCaption());
            this.setTitle(MainApplication.getInstance().getProfile().getMainActivityCaption());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void OnCurOrderDataChange(Order curOrder) {
        generateCurOrder();
    }

    public void fabMainActionsClick() {
        MainActionAdapter mainActionAdapter = new MainActionAdapter(this, R.layout.main_action_item, MainApplication.getInstance().getMainActions());
        ListView listViewItems = new ListView(this);
        listViewItems.setAdapter(mainActionAdapter);
        listViewItems.setOnItemClickListener(new OnMainActionClickListener());

        // put the ListView in the pop up
        mainActionsDialog = new AlertDialog.Builder(MainActivity.this)
                .setView(listViewItems)
                .show();
    }

    public void onTariffPlanClick() {
        if ((MainApplication.getInstance().getMainAccount().getStatus() != Constants.DRIVER_ON_ORDER)
                && (MainApplication.getInstance().getProfile().showActivateTariffPlan())) {
            ArrayList<IMainCardViewData> cards = new ArrayList<>(MainApplication.getInstance().getPreferences().getDriverTariffPlans());
            MainBottomSheetRecycler myBottomSheetFragment = new MainBottomSheetRecycler(
                    cards,
                    mainCardViewData -> {
                        TariffPlan tariffPlan = (TariffPlan) mainCardViewData;
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
                        alertDialog.setTitle("Покупка смены");
                        alertDialog.setMessage("Купить смену \"" + tariffPlan.Name + "\" за " +
                                MainUtils.getSummaString(tariffPlan.Cost) + "?");
                        alertDialog.setPositiveButton("Да", (dialogInterface, i1) -> {
                            httpGetResult("/tariff/activate?tariff_id=" + tariffPlan.ID);
                        });
                        alertDialog.setNegativeButton("Нет", null);
                        alertDialog.create();
                        alertDialog.show();

                    }
            );
            myBottomSheetFragment.show(getSupportFragmentManager(), myBottomSheetFragment.getTag());
        }
    }

    public void onSupportContactClick() {
        List<SupportContactItem> supportContactItemList = new ArrayList<>();
        supportContactItemList.add(new SupportContactItem("phone"));
        supportContactItemList.add(new SupportContactItem("whatsapp"));
        supportContactItemList.add(new SupportContactItem("telegram"));
        ArrayList<IMainCardViewData> cards = new ArrayList<>(supportContactItemList);
        MainBottomSheetRecycler myBottomSheetFragment = new MainBottomSheetRecycler(
                cards,
                mainCardViewData -> {
                    SupportContactItem supportContactItem = (SupportContactItem) mainCardViewData;
                    supportContactItem.onClick(this);
                }
        );
        myBottomSheetFragment.show(getSupportFragmentManager(), myBottomSheetFragment.getTag());
    }

    public void onLocationDataChange() {
        int GPSState = MainApplication.getInstance().getLocationService().getGPSStatus(this);
        if (miGPSFixed != null) {
            miGPSFixed.setVisible(false);
            miGPSNotFixed.setVisible(false);
            miGPSOff.setVisible(false);
            switch (GPSState) {
                case LocationService.GPS_FIXED -> miGPSFixed.setVisible(true);
                case LocationService.GPS_NOT_FIXED -> miGPSNotFixed.setVisible(true);
                case LocationService.GPS_OFF -> miGPSOff.setVisible(true);
            }
        }

        if (GPSState == LocationService.GPS_OFF) {
            tvGPSStatus.setText(getString(R.string.errorGPSOff));
            viewMainData.setVisibility(View.GONE);
            viewGPSError.setVisibility(View.VISIBLE);
            fabMainActions.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.tvGPSErrorInfo)).setText(getString(R.string.errorGPSOff));
        } else if (MainApplication.getInstance().getLocationService().getLocationData().equals("")) {
            tvGPSStatus.setText(getString(R.string.errorGPSNotFixed));
            viewMainData.setVisibility(View.VISIBLE);
            viewGPSError.setVisibility(View.GONE);
            fabMainActions.setVisibility(View.VISIBLE);

        } else {
            viewMainData.setVisibility(View.VISIBLE);
            viewGPSError.setVisibility(View.GONE);

            tvGPSStatus.setText(MainApplication.getInstance().getLocationService().getCurLocationName());

        }

    }

    public void setDriverStatusIcon(int driverStatus) {
        if ((miDriverOffline != null) && (miDriverOnLine != null) && (miDriverOnOrder != null)) {
            miDriverOffline.setVisible(false);
            miDriverOnLine.setVisible(false);
            miDriverOnOrder.setVisible(false);
            switch (driverStatus) {
                case Constants.DRIVER_OFFLINE -> miDriverOffline.setVisible(true);
                case Constants.DRIVER_ONLINE -> miDriverOnLine.setVisible(true);
                case Constants.DRIVER_ON_ORDER -> miDriverOnOrder.setVisible(true);
            }
        }
    }

    @Override
    public void OnOrdersChange() {
        if (MainApplication.getInstance().getCurOrders().getCount() == 0) {
            tvNullCurOrderInfo.setVisibility(View.VISIBLE);
            rvCurOrders.setVisibility(View.GONE);
        } else {
            tvNullCurOrderInfo.setVisibility(View.GONE);
            rvCurOrders.setVisibility(View.VISIBLE);
            rvCurOrders.getRecycledViewPool().clear();
            // curOrdersAdapter.notifyItemRangeInserted(0, MainApplication.getInstance().getCurOrders().getCount());
            // curOrdersAdapter.notifyItemRangeChanged(0, MainApplication.getInstance().getCurOrders().getCount()); // .notifyDataSetChanged();
            try {
                curOrdersAdapter.notifyDataSetChanged();
            } catch (IndexOutOfBoundsException ignored) {
            }

        }

        if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_VIEW_ORDER)
            generateViewOrder();
        if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_CUR_ORDER)
            generateCurOrder();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (MainApplication.getInstance().getMainActivityCurView() == Constants.CUR_VIEW_VIEW_ORDER)
            generateViewOrder();
    }

    public void onCompleteOrdersChange(int orderCount) {
        if (orderCount == 0) {
            runOnUiThread(() -> {
                btnCompleteOrders.setVisibility(View.GONE);
                fabMainActions.setVisibility(View.VISIBLE);
            });
        } else {
            runOnUiThread(() -> {
                btnCompleteOrders.setVisibility(View.VISIBLE);
                fabMainActions.setVisibility(View.GONE);
            });
        }
    }

    private void generateCurOrder() {
        Order viewOrder = MainApplication.getInstance().getCurOrder();
        if (viewOrder.getState() == null) return;
        if (lastCurOrderData.equals(viewOrder.JSONDataToCheckNew)) {
            ((TextView) findViewById(R.id.tvCurOrderTimer)).setText(viewOrder.getTimer());
        } else {
            lastCurOrderData = viewOrder.JSONDataToCheckNew;
            viewOrder.fillCurOrderViewData(this, viewCurOrder);
            btnCompleteOrders.setVisibility(View.GONE);
            findViewById(R.id.btnOrderAction).setVisibility(View.GONE);


            if (viewOrderLastState != viewOrder.getState()) {
                mpOrderStateChange.start();
                viewOrderLastState = viewOrder.getState();
            }

            findViewById(R.id.llCurOrderTitleEx).setVisibility(View.VISIBLE);

            ((TextView) findViewById(R.id.tvCurOrderStateName)).setText(viewOrder.getStateName());
            ((TextView) findViewById(R.id.tvCurOrderTimer)).setText(viewOrder.getTimer());

            ((FontFitTextView) findViewById(R.id.tvCurOrderClientPrice)).setText(viewOrder.getCostString());

            LogService.getInstance().log(this, "generateCurOrder", "mainAction = " + viewOrder.getMainAction());

            switch (viewOrder.getMainAction()) {
                case "apply_deny" -> {
                    fabMainActions.setVisibility(View.GONE);
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Принять");
                    btnCurOrderMainAction.setOnClickListener(view -> httpGetResult("/last/orders/apply"));
                    btnCurOrderAction.setVisibility(View.VISIBLE);
                    btnCurOrderAction.setText("Отказаться");
                    btnCurOrderAction.setOnClickListener(view -> httpGetResult("/last/orders/deny"));
                }
                case "set_driver_at_client" -> {
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Подъехал");
                    btnCurOrderAction.setVisibility(View.GONE);
                    btnCurOrderMainAction.setOnClickListener(view -> httpGetResult("/last/orders/waiting"));
                    fabMainActions.setVisibility(View.VISIBLE);
                }
                case "set_client_in_car" -> {
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Клиент в машине");
                    btnCurOrderAction.setVisibility(View.GONE);
                    btnCurOrderMainAction.setOnClickListener(view -> httpGetResult("/last/orders/executed"));
                    fabMainActions.setVisibility(View.VISIBLE);
                }
                case "set_order_done" -> {
                    if (MainApplication.getInstance().getCompleteOrders().getCount() > 0)
                        btnCompleteOrders.setVisibility(View.VISIBLE);
                    else btnCompleteOrders.setVisibility(View.GONE);
                    btnCurOrderMainAction.setVisibility(View.VISIBLE);
                    btnCurOrderMainAction.setText("Выполнил");
                    btnCurOrderAction.setVisibility(View.GONE);
                    btnCurOrderMainAction.setOnClickListener(view -> httpGetResult("/last/orders/done"));
                    if (MainApplication.getInstance().getCompleteOrders().getCount() == 0)
                        fabMainActions.setVisibility(View.GONE);
                    else
                        fabMainActions.setVisibility(View.VISIBLE);

                }
            }
        }
    }

    private void generateViewOrder() {
        LogService.getInstance().log(this, "generateViewOrder");
        final Order viewOrder = MainApplication.getInstance().getViewOrder();
        if (viewOrder == null) {
            mpOrderStateChange.start();
            showToast("Заказ забрал другой водитель");

            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);

        } else {
            LatLng coord = viewOrder.getPoint();
            if (coord != null) {
                if (googleMap != null) {
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(coord));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 15));
                }
            }

            viewOrder.fillCurOrderViewData(this, viewViewOrder);


            findViewById(R.id.btnOrderAction).setVisibility(View.GONE);
            findViewById(R.id.tvOrderTimer).setVisibility(View.GONE);

            Button btnOrderMainAction = findViewById(R.id.btnOrderMainAction);

            switch (viewOrder.getCheck()) {
                case 0 -> {
                    btnOrderMainAction.setText("Взять заказ");
                    btnOrderMainAction.setEnabled(true);
                    btnOrderMainAction.setOnClickListener(view -> {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Внимание");
                        if (viewOrder.corporateTaxi && MainApplication.getInstance().getPreferences().isShowCorporateTaxiCheckOrderDialog()) {
                            alertDialog.setMessage(MainApplication.getInstance().getPreferences().corporateTaxiCheckOrderDialog);
                        } else if (MainApplication.getInstance().getPreferences().isLongDistanceMessage() && viewOrder.getPickUpDistance() > 5000) {
                            alertDialog.setMessage("До заказа более 5 километров. Время подачи до клиента не более 10 минут. Вы уверены, что хотите принять данный заказ?");
                        } else {
                            alertDialog.setMessage("Принять данный заказ?");
                        }

                        alertDialog.setPositiveButton("Да", (dialogInterface, i) -> {
                            MainApplication.getInstance().setMainActivityCurView(Constants.CUR_VIEW_CUR_ORDERS);
                            httpGetResult("/last/orders/check?order_id=" + viewOrder.getID());
                            MainApplication.getInstance().getPreferences().setLastShowCorporateTaxiCheckOrderDialog();
                        });
                        alertDialog.setNegativeButton("Нет", null);
                        alertDialog.create();
                        alertDialog.show();

                    });
                }
                case 1 -> {
                    btnOrderMainAction.setText("Заказ на автораздаче");
                    btnOrderMainAction.setEnabled(false);
                }
                case 2 -> {
                    btnOrderMainAction.setText("Заказ Вам не доступен");
                    btnOrderMainAction.setEnabled(false);
                }
            }
            if (viewOrderLastState != viewOrder.getCheck()) {
                mpOrderStateChange.start();
                viewOrderLastState = viewOrder.getCheck();
            }
        }
    }
}
