package org.toptaxi.taximeter.activities;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetFloat;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;
import org.toptaxi.taximeter.tools.bottomsheets.MainBottomSheetRecycler;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InviteActivity extends MainAppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    Button btnPlusDay, btnMinusDay;
    TextView textViewDate, textViewRulesLinkInvite, tvAllBonuses;
    TextView tvInviteDriverCount, tvInviteClientCount, tvInviteDriverOrderCount, tvInviteClientOrderCount, tvInviteDriverBonus, tvInviteClientBonus;
    LocalDate curDate;
    SwipeRefreshLayout swipeRefreshLayout;
    PieChart pieChart;
    int pieChartDriver, pieChartClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Пригласить друга");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        textViewDate = findViewById(R.id.textViewDateInvite);

        tvInviteDriverCount = findViewById(R.id.tvInviteDriverCount);
        tvInviteClientCount = findViewById(R.id.tvInviteClientCount);
        tvInviteDriverOrderCount = findViewById(R.id.tvInviteDriverOrderCount);
        tvInviteClientOrderCount = findViewById(R.id.tvInviteClientOrderCount);
        tvInviteDriverBonus = findViewById(R.id.tvInviteDriverBonus);
        tvInviteClientBonus = findViewById(R.id.tvInviteClientBonus);

        curDate = LocalDate.now().withDayOfMonth(1);

        btnPlusDay = findViewById(R.id.btnPlusDayInvite);
        btnMinusDay = findViewById(R.id.btnMinusDayInvite);
        btnPlusDay.setOnClickListener(view -> dataPlusDate());
        btnMinusDay.setOnClickListener(view -> dataMinusDate());
        btnPlusDay.setText(">>>");
        btnMinusDay.setText("<<<");

        textViewRulesLinkInvite = findViewById(R.id.textViewRulesLinkInvite);
        String urlLink = JSONGetString(MainApplication.getInstance().getPreferences().getGuaranteedIncome(), "rules");
        if (!MainUtils.isEmptyString(urlLink)){
            textViewRulesLinkInvite.setVisibility(View.VISIBLE);
            textViewRulesLinkInvite.setMovementMethod(LinkMovementMethod.getInstance());
            textViewRulesLinkInvite.setText(Html.fromHtml("<string name=\"link\"><a href=\"" + urlLink + "\">Условия начисления</a></string>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
        else {
            textViewRulesLinkInvite.setVisibility(View.GONE);
        }


        tvAllBonuses = findViewById(R.id.tvAllBonuses);


        swipeRefreshLayout = findViewById(R.id.activityInviteRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        pieChartDriver = ContextCompat.getColor(this, R.color.primaryGreen);
        pieChartClient = ContextCompat.getColor(this, R.color.primaryYellow);

        pieChart = findViewById(R.id.activityInvitePieChart);
        pieChart.addPieSlice(new PieModel(0.3f, pieChartDriver));
        pieChart.addPieSlice(new PieModel(0.7f, pieChartClient));


        loadData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dataPlusDate() {
        if (!curDate.equals(LocalDate.now().withDayOfMonth(1))) {
            curDate = curDate.plusMonths(1).withDayOfMonth(1);
            loadData();
        }
    }

    private void dataMinusDate() {
        curDate = curDate.minusMonths(1).withDayOfMonth(1);
        loadData();
    }

    public void loadData() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        LogService.getInstance().log(this, curDate.toString());
        if (curDate.getYear() == LocalDate.now().getYear())
            textViewDate.setText(MainUtils.getMonthName(curDate));
        else
            textViewDate.setText(String.format(Locale.getDefault(), "%s %d г.", MainUtils.getMonthName(curDate), curDate.getYear()));

        new Thread(() -> {
            Float allBonusSumma = 0f, clientBonusSumma = 0f, driverBonusSumma = 0f;
            JSONObject driverResult = null;
            JSONObject clientResult = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            JSONObject data = MainApplication.getInstance().getRestService().httpGet("/statistics/invite?date=" + formatter.format(curDate) + " 00:00:00");
            LogService.getInstance().log("InviteActivity", data.toString());
            if (JSONGetString(data, "status").equals("OK")) {
                try {
                    JSONObject result = data.getJSONObject("result");
                    if (result.has("driver")) {
                        driverResult = result.getJSONObject("driver");
                        driverBonusSumma = JSONGetFloat(driverResult, "bonus");
                        allBonusSumma += driverBonusSumma;
                    }
                    if (result.has("client")) {
                        clientResult = result.getJSONObject("client");
                        clientBonusSumma = JSONGetFloat(clientResult, "bonus");
                        allBonusSumma += clientBonusSumma;
                    }

                } catch (Exception ignored) {
                }
            }

            Float finalAllBonusSumma = allBonusSumma;
            Float finalDriverBonusSumma = driverBonusSumma;
            Float finalClientBonusSumma = clientBonusSumma;
            JSONObject finalDriverResult = driverResult;
            JSONObject finalClientResult = clientResult;
            runOnUiThread(() -> {
                tvAllBonuses.setText(MessageFormat.format("Заработано\n{0}", MainUtils.getSummaString(finalAllBonusSumma)));
                if (finalAllBonusSumma > 0) {
                    pieChart.clearChart();
                    pieChart.addPieSlice(new PieModel(finalDriverBonusSumma / finalAllBonusSumma, pieChartDriver));
                    pieChart.addPieSlice(new PieModel(finalClientBonusSumma / finalAllBonusSumma, pieChartClient));
                }
                if (finalDriverResult != null) {
                    tvInviteDriverCount.setText(JSONGetString(finalDriverResult, "invite_count"));
                    tvInviteDriverOrderCount.setText(JSONGetString(finalDriverResult, "order_count"));
                    tvInviteDriverBonus.setText(MainUtils.getSummaString(JSONGetFloat(finalDriverResult, "bonus")));
                } else {
                    tvInviteDriverCount.setText("0");
                    tvInviteDriverOrderCount.setText("0");
                    tvInviteDriverBonus.setText(MainUtils.getSummaString(0));
                }

                if (finalClientResult != null) {
                    tvInviteClientCount.setText(JSONGetString(finalClientResult, "invite_count"));
                    tvInviteClientOrderCount.setText(JSONGetString(finalClientResult, "order_count"));
                    tvInviteClientBonus.setText(MainUtils.getSummaString(JSONGetFloat(finalClientResult, "bonus")));
                } else {
                    tvInviteClientCount.setText("0");
                    tvInviteClientOrderCount.setText("0");
                    tvInviteClientBonus.setText(MainUtils.getSummaString(0));
                }


                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(curDate.withDayOfMonth(1).equals(LocalDate.now().withDayOfMonth(1)));
            });
        }).start();

    }

    public void btnInviteClick(View view) {
        MainBottomSheetRecycler myBottomSheetFragment = new MainBottomSheetRecycler(
                mainCardViewData ->
                        sendSharingIntent(JSONGetString(MainApplication.getInstance().getPreferences().getInviteData(), mainCardViewData.getTag()))
        );
        myBottomSheetFragment.addItem("driver", "Отправить приглашение водителю");
        myBottomSheetFragment.addItem("client", "Отправить приглашение клиенту");
        myBottomSheetFragment.show(getSupportFragmentManager(), myBottomSheetFragment.getTag());
    }

    private void sendSharingIntent(String inviteText) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Отправить приглашение");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, inviteText);
        startActivity(Intent.createChooser(sharingIntent, "Выберите способ отправки приглашения"));
    }

    @Override
    public void onRefresh() {
        loadData();
    }
}
