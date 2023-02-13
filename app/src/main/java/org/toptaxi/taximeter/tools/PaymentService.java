package org.toptaxi.taximeter.tools;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetBool;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class PaymentService {
    private static volatile PaymentService paymentService;

    private String detailNote;
    private String qiwiNote;
    private Boolean qiwiAvailable;
    private Boolean sbpAvailable;
    private String ckassa = "";
    private String qiwiTerminal = "";
    private String qiwiWallet = "";
    private String ckassaNote = "";
    private String qiwiTerminalNote = "";
    private String qiwiWalletNote = "";


    public static PaymentService getInstance() {
        PaymentService localInstance = paymentService;
        if (localInstance == null) {
            synchronized (PaymentService.class) {
                localInstance = paymentService;
                if (localInstance == null) {
                    paymentService = localInstance = new PaymentService();
                }
            }
        }
        return localInstance;
    }

    public void setPreferences(JSONObject data) {
        detailNote = JSONGetString(data, "detail");
        qiwiNote = JSONGetString(data, "qiwi_note");
        qiwiAvailable = JSONGetBool(data, "qiwi", false);
        sbpAvailable = JSONGetBool(data, "sbp", false);
        ckassa = JSONGetString(data, "ckassa");
        qiwiTerminal = JSONGetString(data, "qiwi_terminal");
        qiwiWallet = JSONGetString(data, "qiwi_wallet");
        ckassaNote = JSONGetString(data, "ckassa_note");
        qiwiTerminalNote = JSONGetString(data, "qiwi_terminal_note");
        qiwiWalletNote = JSONGetString(data, "qiwi_wallet_note");
    }

    public boolean getPaymentsAvailable() {
        return sbpAvailable || qiwiAvailable;
    }

    public boolean getPaymentsAnotherAvailable() {
        return !ckassa.equals("") || !qiwiWallet.equals("") || !qiwiTerminal.equals("");
    }

    private String getPaymentDetailNote() {
        if (detailNote.equals("")) return null;
        return detailNote;
    }


    @SuppressLint("SetTextI18n")
    public void showPaymentDialog(MainAppCompatActivity activity) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.CustomBottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_add_payment,
                        activity.findViewById(R.id.modalBottomAddPayment));

        Button buttonSPBPayment = bottomSheetView.findViewById(R.id.btnSBPPayment);
        Button buttonQiwiPayment = bottomSheetView.findViewById(R.id.btnQiwiPayment);
        EditText ednPaymentAmount = bottomSheetView.findViewById(R.id.ednPaymentAmount);
        TextView tvDetail = bottomSheetView.findViewById(R.id.tvDetail);
        TextView tvAnotherPayments = bottomSheetView.findViewById(R.id.tvAnotherPayments);
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

        if (getPaymentDetailNote() != null) {
            tvDetail.setVisibility(View.VISIBLE);
            tvDetail.setOnClickListener(view -> activity.runOnUiThread(() -> activity.showSimpleDialog(getPaymentDetailNote())));
        } else {
            tvDetail.setVisibility(View.GONE);
        }

        if (getPaymentsAnotherAvailable()) {
            tvAnotherPayments.setVisibility(View.VISIBLE);

            tvAnotherPayments.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                activity.runOnUiThread(() -> showAnotherPaymentDialog(activity));
            });

        } else {
            tvAnotherPayments.setVisibility(View.GONE);
        }

        if (!sbpAvailable || !qiwiAvailable) {
            bottomSheetView.findViewById(R.id.tvChoosePayment).setVisibility(View.GONE);
        } else {
            bottomSheetView.findViewById(R.id.tvChoosePayment).setVisibility(View.VISIBLE);
        }

        if (sbpAvailable) {
            buttonSPBPayment.setVisibility(View.VISIBLE);
        }

        AtomicReference<Boolean> qiwiShowMessage = new AtomicReference<>(false);

        if (qiwiAvailable) {
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
                    JSONObject response = MainApplication.getInstance().getRestService().httpGet(activity,"/payments/order?amount=" + amountInteger + "&source=qiwi");

                    if (JSONGetString(response, "status").equals("OK")) {
                        Boolean showMessage = JSONGetBool(response, "result_message");
                        if (!qiwiShowMessage.get() && showMessage) {
                            activity.runOnUiThread(() -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(qiwiNote);
                                builder.setCancelable(true);
                                builder.setPositiveButton("Понятно", (dialog, which) -> {
                                    qiwiShowMessage.set(true);
                                    dialog.dismiss();
                                    String URL = JSONGetString(response, "result_link");
                                    Uri paymentInstructionLink = Uri.parse(URL);
                                    Intent paymentInstructionLinkIntent = new Intent(Intent.ACTION_VIEW, paymentInstructionLink);
                                    bottomSheetDialog.dismiss();
                                    activity.startActivity(paymentInstructionLinkIntent);

                                });
                                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
                                builder.show();
                            });

                        } else {
                            String URL = JSONGetString(response, "result_link");
                            Uri paymentInstructionLink = Uri.parse(URL);
                            Intent paymentInstructionLinkIntent = new Intent(Intent.ACTION_VIEW, paymentInstructionLink);
                            bottomSheetDialog.dismiss();
                            activity.startActivity(paymentInstructionLinkIntent);
                        }
                    }

                });
            });
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void showAnotherPaymentDialog(MainAppCompatActivity activity){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.CustomBottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_another_payments_available,
                        activity.findViewById(R.id.modalBottomAddPayment));
        if (qiwiTerminal.equals("")){
            bottomSheetView.findViewById(R.id.cvQiwiTerminal).setVisibility(View.GONE);
        }
        else {
            bottomSheetView.findViewById(R.id.cvQiwiTerminal).setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.cvQiwiTerminal).setOnClickListener(view -> activity.goToURL(qiwiTerminal));
            ((TextView)bottomSheetView.findViewById(R.id.tvQiwiTerminalNote)).setText(qiwiTerminalNote);
        }
        if (qiwiWallet.equals("")){
            bottomSheetView.findViewById(R.id.cvQiwiWallet).setVisibility(View.GONE);
        }
        else {
            bottomSheetView.findViewById(R.id.cvQiwiWallet).setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.cvQiwiWallet).setOnClickListener(view -> activity.goToURL(qiwiWallet));
            ((TextView)bottomSheetView.findViewById(R.id.tvQiwiWalletNote)).setText(qiwiWalletNote);
        }

        if (ckassa.equals("")){
            bottomSheetView.findViewById(R.id.cvCKassa).setVisibility(View.GONE);
        }
        else {
            bottomSheetView.findViewById(R.id.cvCKassa).setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.cvCKassa).setOnClickListener(view -> activity.goToURL(ckassa));
            ((TextView)bottomSheetView.findViewById(R.id.tvCKassaNote)).setText(ckassaNote);
        }
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
