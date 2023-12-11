package org.toptaxi.taximeter.activities;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;
import static org.toptaxi.taximeter.tools.MainUtils.getOrderCountName;
import static org.toptaxi.taximeter.tools.MainUtils.getSummaString;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.MainUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import params.com.stepprogressview.StepProgressView;

public class GuaranteedIncomeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    SwipeRefreshLayout swipeRefreshLayout;
    TextView textViewCurDate, textViewOrderCount, textViewAdditionalSumma, textViewRulesLink;
    LocalDate curDate;
    Button btnPlusDay, btnMinusDay;
    ProgressBar progressBarOrderCount;
    StepProgressView stepProgressView;
    String urlLink;
    LocalDate startReportDate = LocalDate.of(2023, 12, 10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guaranteed_income);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Гарантированный доход");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        curDate = LocalDate.now();

        swipeRefreshLayout = findViewById(R.id.guaranteedIncomeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        textViewAdditionalSumma = findViewById(R.id.textViewAdditionalSumma);
        textViewCurDate = findViewById(R.id.textViewCurDate);
        textViewOrderCount = findViewById(R.id.textViewOrderCount);
        textViewRulesLink = findViewById(R.id.textViewRulesLink);

        progressBarOrderCount = findViewById(R.id.progressBarOrderCount);

        stepProgressView = findViewById(R.id.stepProgressView);

        btnPlusDay = findViewById(R.id.btnPlusDay);
        btnMinusDay = findViewById(R.id.btnMinusDay);

        btnPlusDay.setOnClickListener(view -> dataPlusDate());
        btnMinusDay.setOnClickListener(view -> dataMinusDate());

        btnPlusDay.setText(">>>");
        btnMinusDay.setText("<<<");

        loadData();

        textViewCurDate.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(GuaranteedIncomeActivity.this,
                    (view1, year, monthOfYear, dayOfMonth) -> {
                        LocalDate newDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                        if (newDate.isBefore(startReportDate)) {
                            newDate = startReportDate;
                        }

                        if (newDate.isAfter(LocalDate.now())) {
                            newDate = LocalDate.now();
                        }
                        if (!newDate.equals(curDate)) {
                            curDate = newDate;
                            loadData();
                        }

                    }, curDate.getYear(), curDate.getMonthValue() - 1, curDate.getDayOfMonth());
            datePickerDialog.show();
        });

        urlLink = JSONGetString(MainApplication.getInstance().getPreferences().getGuaranteedIncome(), "rules");

        textViewRulesLink.setMovementMethod(LinkMovementMethod.getInstance());
        textViewRulesLink.setText(Html.fromHtml("<string name=\"link\"><a href=\"" + urlLink + "\">Условия начисления</a></string>", HtmlCompat.FROM_HTML_MODE_LEGACY));
    }


    private void dataPlusDate() {
        if (!curDate.equals(LocalDate.now())) {
            curDate = curDate.plusDays(1);
            loadData();
        }
    }

    private void dataMinusDate() {
        if (curDate.isAfter(startReportDate)){
            curDate = curDate.minusDays(1);
            loadData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRefresh() {
        loadData();
    }

    public void loadData() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        textViewCurDate.setText(String.format(Locale.getDefault(), "%02d.%02d.%04d", curDate.getDayOfMonth(), curDate.getMonthValue(), curDate.getYear()));

        new Thread(() -> {
            int orderCount = 0;
            int progressPercent = 0;
            int orderSumma = 0;
            int curStepOrderCount = 0;
            int additionalSumma = 0;
            String newURLLink = "";
            List<Integer> stepsList = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            JSONObject data = MainApplication.getInstance().getRestService().httpGet("/statistics/guaranteed_income?date=" + formatter.format(curDate) + " 00:00:00");
            if (JSONGetString(data, "status").equals("OK")) {
                try {
                    JSONObject result = data.getJSONObject("result");
                    orderCount = JSONGetInteger(result, "order_count", 0);
                    orderSumma = JSONGetInteger(result, "order_summa", 0);
                    additionalSumma = JSONGetInteger(result, "additional_summa", 0);
                    newURLLink = result.getJSONObject("rules").getString("rules");

                    JSONArray stepsArray = result.getJSONObject("rules").getJSONArray("steps");
                    curStepOrderCount = stepsArray.getInt(0);

                    for (int i = 0; i < stepsArray.length(); i++) {
                        int stepOrderCount = stepsArray.getInt(i);
                        stepsList.add(stepOrderCount);
                        if (orderCount > stepOrderCount) {
                            curStepOrderCount = stepOrderCount;
                        }
                    }
                    stepsList.sort(Comparator.naturalOrder());


                    progressPercent = (int) ((float) orderCount / Float.valueOf(Collections.max(stepsList)) * 100f);

                    LogService.getInstance().log("sys", "curStepOrderCount = " + String.valueOf(curStepOrderCount));
                    LogService.getInstance().log("sys", "progressPercent = " + String.valueOf(progressPercent));
                    // LogService.getInstance().log("sys", "additionalSumma = " + String.valueOf(progressPercent));

                } catch (JSONException ignored) {
                }
            }

            int finalOrderSumma = orderSumma;
            int finalOrderCount = orderCount;
            int finalProgressPercent = progressPercent;
            int finalAdditionalSumma = additionalSumma;

            String finalNewURLLink = newURLLink;
            runOnUiThread(() -> {
                String textData = finalOrderCount + " " + getOrderCountName(finalOrderCount);
                if (finalOrderSumma != 0) textData += "\nна " + getSummaString(finalOrderSumma);
                textViewOrderCount.setText(textData);
                progressBarOrderCount.setProgress(finalProgressPercent, true);

                stepProgressView.setMarkers(stepsList);
                stepProgressView.setTotalProgress(Collections.max(stepsList) + 1);
                stepProgressView.setCurrentProgress(finalOrderCount);

                String additionalSummaText = "";
                if (curDate.equals(LocalDate.now())) {
                    if (finalOrderCount < Collections.min(stepsList)) {
                        int needOrder = Collections.min(stepsList) - finalOrderCount;
                        additionalSummaText = "Для начисления гарантированного дохода выполните ещё " + needOrder + " " + getOrderCountName(needOrder) + ".";
                    } else if (finalOrderCount < Collections.max(stepsList)) {
                        int needOrder = Collections.max(stepsList) - finalOrderCount;
                        additionalSummaText = "Для начисления повышенного гарантированного дохода выполните ещё " + needOrder + " " + getOrderCountName(needOrder) + ".";
                    }
                } else if (finalAdditionalSumma != 0) {
                    additionalSummaText = "Ваша доплата до гарантированного дохода составила:\n" + MainUtils.getSummaString(finalAdditionalSumma);
                } else if (finalOrderCount < Collections.min(stepsList)) {
                    additionalSummaText = "К сожалению, Вы не выполнили условия для начисления гарантированного дохода";
                }

                MainUtils.TextViewSetTextOrGone(textViewAdditionalSumma, additionalSummaText);

                if (!finalNewURLLink.equals(urlLink)) {
                    urlLink = finalNewURLLink;
                    textViewRulesLink.setText(Html.fromHtml("<string name=\"link\"><a href=\"" + urlLink + "\">Условия начисления</a></string>", HtmlCompat.FROM_HTML_MODE_LEGACY));
                }
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(curDate.equals(LocalDate.now()));
            });
        }).start();

    }
}