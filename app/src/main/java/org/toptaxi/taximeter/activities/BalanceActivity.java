package org.toptaxi.taximeter.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.ListViewPaymentAdapter;
import org.toptaxi.taximeter.data.Payment;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.dialogs.PaymentsDialogKT;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BalanceActivity extends MainAppCompatActivity implements AbsListView.OnScrollListener {
    ListViewPaymentAdapter adapter;
    ListView listView;
    private View footer;
    Boolean isLoadData = false;
    private String viewType = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            viewType = arguments.getString("type", "main");
        }

        setContentView(R.layout.activity_balance);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("История по балансу");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listView = findViewById(R.id.lvPayments);

        footer = getLayoutInflater().inflate(R.layout.item_messages_footer, null);
        listView.addFooterView(footer);

        adapter = new ListViewPaymentAdapter(this, viewType);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        updateDataAsync();

        if (viewType.equals("main")) {
            findViewById(R.id.btnPaymentInstruction).setVisibility(View.GONE);
            findViewById(R.id.btnPaymentInstructionCorporate).setVisibility(View.GONE);
            if (PaymentsDialogKT.INSTANCE.getPaymentsAvailable()) {
                findViewById(R.id.btnPaymentInstruction).setVisibility(View.VISIBLE);
                findViewById(R.id.btnPaymentInstruction).setOnClickListener(view -> PaymentsDialogKT.INSTANCE.showPaymentDialog(this));
            } else if (MainApplication.getInstance().getPreferences().getPaymentInstructionLink() != null) {
                findViewById(R.id.btnPaymentInstruction).setVisibility(View.VISIBLE);
                findViewById(R.id.btnPaymentInstruction).setOnClickListener(view -> goToURL(MainApplication.getInstance().getPreferences().getPaymentInstructionLink()));
            }
        }

        if (viewType.equals("corporate")) {
            findViewById(R.id.btnPaymentInstruction).setVisibility(View.GONE);
            findViewById(R.id.btnPaymentInstructionCorporate).setVisibility(View.VISIBLE);
            findViewById(R.id.btnPaymentInstructionCorporate).setOnClickListener(view -> showBalanceCorporateDialog());
        }
    }

    public void showBalanceCorporateDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(MainApplication.getInstance().getPreferences().corporateTaxiBalanceButtonDialog);
        alertDialog.setPositiveButton("Позвонить", (dialog, which) -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + MainApplication.getInstance().getPreferences().corporateTaxiContactPhone));
            startActivity(callIntent);

        });
        alertDialog.setNegativeButton("Отмена", null);
        alertDialog.create();
        alertDialog.show();
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
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (((firstVisibleItem + visibleItemCount) >= totalItemCount) && (!isLoadData)) {
            LogService.getInstance().log(this, "onScroll", "loadMore");
            updateDataAsync();
        }
    }


    void updateDataAsync() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            LogService.getInstance().log("BalanceActivity", "start thread");
            isLoadData = true;
            final ArrayList<Payment> result = adapter.LoadMore();
            LogService.getInstance().log("BalanceActivity", "stop load more result = " + result);
            runOnUiThread(() -> {
                LogService.getInstance().log("BalanceActivity", "runOnUiThread result = " + result);
                if (result.isEmpty()) {
                    listView.removeFooterView(footer);
                } else {
                    adapter.AppendNewData(result);
                    adapter.notifyDataSetChanged();
                }
                isLoadData = false;
                LogService.getInstance().log("BalanceActivity", "stop thread");
            });
        });
    }

}
