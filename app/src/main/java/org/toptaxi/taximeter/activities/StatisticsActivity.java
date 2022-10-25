package org.toptaxi.taximeter.activities;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

public class StatisticsActivity extends AppCompatActivity {
    TextView curTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        TabHost tabHost = findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("/last/statistics/orders");
        tabSpec.setIndicator("Заказы");
        tabSpec.setContent(R.id.tvStatisticsOrders);
        tabHost.addTab(tabSpec);
        findViewById(R.id.tvStatisticsOrders).setVisibility(View.VISIBLE);
        curTextView = findViewById(R.id.tvStatisticsOrders);

        if (MainApplication.getInstance().getPreferences().useRating()) {
            tabSpec = tabHost.newTabSpec("/last/statistics/rating");
            tabSpec.setIndicator("Рейтинг");
            tabSpec.setContent(R.id.tvStatisticsRating);
            tabHost.addTab(tabSpec);
            findViewById(R.id.tvStatisticsRating).setVisibility(View.VISIBLE);
        } else findViewById(R.id.tvStatisticsRating).setVisibility(View.GONE);

        if (MainApplication.getInstance().getPreferences().isDriverInvite()) {
            tabSpec = tabHost.newTabSpec("/last/statistics/share");
            tabSpec.setIndicator("Друзья");
            tabSpec.setContent(R.id.tvStatisticsShare);
            tabHost.addTab(tabSpec);
            findViewById(R.id.tvStatisticsShare).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tvStatisticsShare).setVisibility(View.VISIBLE);
        }

        // обработчик переключения вкладок
        tabHost.setOnTabChangedListener(tabId -> {
            switch (tabId) {
                case "/last/statistics/rating":
                    curTextView = findViewById(R.id.tvStatisticsRating);
                    break;
                case "/last/statistics/orders":
                    curTextView = findViewById(R.id.tvStatisticsOrders);
                    break;
                case "/last/statistics/share":
                    curTextView = findViewById(R.id.tvStatisticsShare);
                    break;
            }
            new GetStatInfoTask(StatisticsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tabId);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetStatInfoTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "/last/statistics/orders");
    }

    private class GetStatInfoTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progressDialog;
        Context mContext;

        GetStatInfoTask(Context context) {
            mContext = context;
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Получение данных ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... data) {
            return MainApplication.getInstance().getRestService().httpGet(data[0]);
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            progressDialog.dismiss();
            if (curTextView != null) {
                curTextView.setText(MainApplication.fromHtml(JSONGetString(response, "result")));
            }
        }
    }
}
