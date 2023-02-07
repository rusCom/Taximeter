package org.toptaxi.taximeter.tools;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class PaymentService {

    public static void showPaymentDialog(AppCompatActivity activity) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.CustomBottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_add_payment,
                        activity.findViewById(R.id.modalBottomAddPayment));

        Button buttonSPBPayment = bottomSheetView.findViewById(R.id.btnSBPPayment);
        Button buttonQiwiPayment = bottomSheetView.findViewById(R.id.btnQiwiPayment);
        TextView ednPaymentAmount = bottomSheetView.findViewById(R.id.ednPaymentAmount);
        buttonSPBPayment.setVisibility(View.GONE);
        buttonQiwiPayment.setVisibility(View.GONE);


        bottomSheetView.findViewById(R.id.btnPayment100).setOnClickListener(v -> ednPaymentAmount.setText("100"));
        bottomSheetView.findViewById(R.id.btnPayment300).setOnClickListener(v -> ednPaymentAmount.setText("300"));
        bottomSheetView.findViewById(R.id.btnPayment500).setOnClickListener(v -> ednPaymentAmount.setText("500"));

        ednPaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ednPaymentAmount.setError(null);
                String amountString = ednPaymentAmount.getText().toString();
                if (amountString.contains(".")) {
                    amountString = amountString.split("\\.")[0];
                    ednPaymentAmount.setText(amountString);
                }
                if (amountString.contains(",")) {
                    amountString = amountString.split(",")[0];
                    ednPaymentAmount.setText(amountString);
                }
            }
        });

        if (!MainApplication.getInstance().getPreferences().getPaymentSBPAvailable() || !MainApplication.getInstance().getPreferences().getPaymentSBPQiwiAvailable()) {
            bottomSheetView.findViewById(R.id.tvChoosePayment).setVisibility(View.GONE);
        } else {
            bottomSheetView.findViewById(R.id.tvChoosePayment).setVisibility(View.VISIBLE);
        }

        if (MainApplication.getInstance().getPreferences().getPaymentSBPAvailable()) {
            buttonSPBPayment.setVisibility(View.VISIBLE);
        }

        AtomicReference<Boolean> qiwiShowMessage = new AtomicReference<>(false);

        if (MainApplication.getInstance().getPreferences().getPaymentSBPQiwiAvailable()) {
            buttonQiwiPayment.setVisibility(View.VISIBLE);
            buttonQiwiPayment.setOnClickListener(view -> {
                String amountString = ednPaymentAmount.getText().toString();
                if (amountString.equals("")) {
                    ednPaymentAmount.requestFocus();
                    ednPaymentAmount.setError("Введите сумму платежа");
                    return;
                }

                int amountInteger = Integer.parseInt(amountString) * 100;

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    if (MainApplication.getInstance().getMainActivity() != null) {
                        MainApplication.getInstance().getMainActivity().runOnUiThread(() -> MainApplication.getInstance().getMainActivity().showProgressDialog());
                    }

                    JSONObject response = MainApplication.getInstance().getRestService().httpGet("/payments/order?amount=" + amountInteger + "&source=qiwi");
                    if (JSONGetString(response, "status").equals("OK")) {
                        Boolean showMessage = JSONGetBool(response, "result_message");
                        if (!qiwiShowMessage.get() && showMessage){
                            if (MainApplication.getInstance().getMainActivity() != null) {
                                MainApplication.getInstance().getMainActivity().runOnUiThread(() -> {
                                    MainApplication.getInstance().getMainActivity().dismissProgressDialog();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setMessage(MainApplication.getInstance().getPreferences().getPaymentQiwiNote());
                                    builder.setCancelable(true);
                                    builder.setNegativeButton("Понятно", (dialog, which) -> {
                                        dialog.dismiss();
                                    });
                                    builder.show();
                                });
                            }

                            qiwiShowMessage.set(true);
                        }
                        else {
                            String URL = JSONGetString(response, "result_link");
                            Uri paymentInstructionLink = Uri.parse(URL);
                            Intent paymentInstructionLinkIntent = new Intent(Intent.ACTION_VIEW, paymentInstructionLink);
                            if (MainApplication.getInstance().getMainActivity() != null) {
                                MainApplication.getInstance().getMainActivity().runOnUiThread(() -> MainApplication.getInstance().getMainActivity().dismissProgressDialog());
                            }
                            bottomSheetDialog.dismiss();
                            activity.startActivity(paymentInstructionLinkIntent);
                        }

                    } else {
                        if (MainApplication.getInstance().getMainActivity() != null) {
                            MainApplication.getInstance().getMainActivity().runOnUiThread(() -> MainApplication.getInstance().getMainActivity().dismissProgressDialog());
                        }
                        MainApplication.getInstance().showToast(JSONGetString(response, "result", "Ошибка обработки платежа. Попробуйте попозже."));
                    }

                });
            });
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

}
