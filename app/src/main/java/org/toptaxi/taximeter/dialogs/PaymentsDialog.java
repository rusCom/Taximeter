package org.toptaxi.taximeter.dialogs;

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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import ru.tinkoff.acquiring.sdk.TinkoffAcquiring;
import ru.tinkoff.acquiring.sdk.localization.AsdkSource;
import ru.tinkoff.acquiring.sdk.localization.Language;
import ru.tinkoff.acquiring.sdk.models.enums.CheckType;
import ru.tinkoff.acquiring.sdk.models.options.CustomerOptions;
import ru.tinkoff.acquiring.sdk.models.options.FeaturesOptions;
import ru.tinkoff.acquiring.sdk.models.options.OrderOptions;
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions;
import ru.tinkoff.acquiring.sdk.utils.Money;

public class PaymentsDialog {
    private static volatile PaymentsDialog paymentService;
    private String detailNote;
    private String qiwiNote;
    private String tinkoffNote;
    private Boolean qiwiAvailable = false;
    private Boolean sbpAvailable = false;
    private Boolean tinkoffAvailable = false;
    private String sbpNote;
    private String ckassa = "";
    private String qiwiTerminal = "";
    private String qiwiWallet = "";
    private String ckassaNote = "";
    private String qiwiTerminalNote = "";
    private String qiwiWalletNote = "";


    public static PaymentsDialog getInstance() {
        PaymentsDialog localInstance = paymentService;
        if (localInstance == null) {
            synchronized (PaymentsDialog.class) {
                localInstance = paymentService;
                if (localInstance == null) {
                    paymentService = localInstance = new PaymentsDialog();
                }
            }
        }
        return localInstance;
    }

    public void setPreferences(JSONObject data) {
        detailNote = JSONGetString(data, "detail");
        qiwiNote = JSONGetString(data, "qiwi_note");
        qiwiAvailable = JSONGetBool(data, "qiwi", false);
        sbpNote = JSONGetString(data, "sbp_note");
        sbpAvailable = JSONGetBool(data, "sbp", false);
        tinkoffNote = JSONGetString(data, "tinkoff_note");
        tinkoffAvailable = JSONGetBool(data, "tinkoff", false);
        ckassa = JSONGetString(data, "ckassa");
        qiwiTerminal = JSONGetString(data, "qiwi_terminal");
        qiwiWallet = JSONGetString(data, "qiwi_wallet");
        ckassaNote = JSONGetString(data, "ckassa_note");
        qiwiTerminalNote = JSONGetString(data, "qiwi_terminal_note");
        qiwiWalletNote = JSONGetString(data, "qiwi_wallet_note");
    }

    public boolean getPaymentsAvailable() {
        return sbpAvailable || qiwiAvailable || tinkoffAvailable;
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
        Button buttonTinkoffPayment = bottomSheetView.findViewById(R.id.btnTinkoffPayment);

        EditText ednPaymentAmount = bottomSheetView.findViewById(R.id.ednPaymentAmount);
        TextView tvDetail = bottomSheetView.findViewById(R.id.tvDetail);
        TextView tvAnotherPayments = bottomSheetView.findViewById(R.id.tvAnotherPayments);
        buttonSPBPayment.setVisibility(View.GONE);
        buttonQiwiPayment.setVisibility(View.GONE);
        buttonTinkoffPayment.setVisibility(View.GONE);


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

        AtomicReference<Boolean> sbpShowMessage = new AtomicReference<>(false);

        if (sbpAvailable) {
            buttonSPBPayment.setVisibility(View.VISIBLE);
            buttonSPBPayment.setOnClickListener(view -> {
                String amountString = ednPaymentAmount.getText().toString();
                if (amountString.equals("")) {
                    ednPaymentAmount.requestFocus();
                    ednPaymentAmount.setError("Введите сумму платежа");
                    return;
                }

                int amountInteger = Integer.parseInt(amountString) * 100;
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    JSONObject response = MainApplication.getInstance().getRestService().httpGet(activity, "/payments/order?amount=" + amountInteger + "&source=sbp");
                    if (JSONGetString(response, "status").equals("OK")) {
                        Boolean showMessage = JSONGetBool(response, "result_message");
                        if (!sbpShowMessage.get() && showMessage) {
                            activity.runOnUiThread(() -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(sbpNote);
                                builder.setCancelable(true);
                                builder.setPositiveButton("Понятно", (dialog, which) -> {
                                    sbpShowMessage.set(true);
                                    dialog.dismiss();
                                    bottomSheetDialog.dismiss();

                                    tinkoffPay(activity,
                                            "sbp",
                                            JSONGetString(response, "result_terminal_key"),
                                            JSONGetString(response, "result_public_key"),
                                            JSONGetString(response, "result_number"), amountInteger);


                                });
                                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
                                builder.show();
                            });

                        } else {
                            bottomSheetDialog.dismiss();

                            tinkoffPay(activity,
                                    "sbp",
                                    JSONGetString(response, "result_terminal_key"),
                                    JSONGetString(response, "result_public_key"),
                                    JSONGetString(response, "result_number"), amountInteger);


                        }
                    }

                });

            });
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
                    JSONObject response = MainApplication.getInstance().getRestService().httpGet(activity, "/payments/order?amount=" + amountInteger + "&source=qiwi");

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

        if (tinkoffAvailable) {
            buttonTinkoffPayment.setVisibility(View.VISIBLE);
            AtomicReference<Boolean> tinkoffShowMessage = new AtomicReference<>(false);
            buttonTinkoffPayment.setOnClickListener(view -> {
                String amountString = ednPaymentAmount.getText().toString();
                if (amountString.equals("")) {
                    ednPaymentAmount.requestFocus();
                    ednPaymentAmount.setError("Введите сумму платежа");
                    return;
                }

                int amountInteger = Integer.parseInt(amountString) * 100;
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    JSONObject response = MainApplication.getInstance().getRestService().httpGet(activity, "/payments/order?amount=" + amountInteger + "&source=tinkoff");
                    if (JSONGetString(response, "status").equals("OK")) {
                        Boolean showMessage = JSONGetBool(response, "result_message");
                        if (!tinkoffShowMessage.get() && showMessage) {
                            activity.runOnUiThread(() -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(tinkoffNote);
                                builder.setCancelable(true);
                                builder.setPositiveButton("Понятно", (dialog, which) -> {
                                    sbpShowMessage.set(true);
                                    dialog.dismiss();
                                    bottomSheetDialog.dismiss();

                                    tinkoffPay(activity,
                                            "tinkoff",
                                            JSONGetString(response, "result_terminal_key"),
                                            JSONGetString(response, "result_public_key"),
                                            JSONGetString(response, "result_number"), amountInteger);


                                });
                                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
                                builder.show();
                            });

                        } else {
                            bottomSheetDialog.dismiss();

                            tinkoffPay(activity,
                                    "tinkoff",
                                    JSONGetString(response, "result_terminal_key"),
                                    JSONGetString(response, "result_public_key"),
                                    JSONGetString(response, "result_number"), amountInteger);


                        }
                    }

                });

            });
        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void tinkoffPay(MainAppCompatActivity activity, String payType, String terminalKey, String publicKey, String orderID, long summa) {


        OrderOptions orderOptions = new OrderOptions();
        orderOptions.setOrderId(orderID);
        orderOptions.setAmount(Money.ofCoins(summa));
        orderOptions.setTitle("Пополнение баланса");
        orderOptions.setRecurrentPayment(false);

        FeaturesOptions featuresOptions = new FeaturesOptions();
        featuresOptions.setTinkoffPayEnabled(false);
        featuresOptions.setFpsEnabled(true);
        featuresOptions.setEmailRequired(false);
        featuresOptions.setLocalizationSource(new AsdkSource(Language.RU));
        featuresOptions.setUseSecureKeyboard(true);

        CustomerOptions customerOptions = new CustomerOptions();
        customerOptions.setCustomerKey(MainApplication.getInstance().getMainAccount().getToken());
        customerOptions.setCheckType(CheckType.NO.toString());

        PaymentOptions paymentOptions = new PaymentOptions();
        paymentOptions.setOrder(orderOptions);
        paymentOptions.setFeatures(featuresOptions);
        paymentOptions.setCustomer(customerOptions);


        try {

            var tinkoffAcquiring = new TinkoffAcquiring(MainApplication.getInstance(), terminalKey, publicKey);
            if (payType.equals("sbp")) {
                tinkoffAcquiring.payWithSbp(activity, paymentOptions, 254);
            } else if (payType.equals("tinkoff")) {
                featuresOptions.setFpsEnabled(false);
                tinkoffAcquiring.openPaymentScreen(activity, paymentOptions, 154);
            }

        } catch (RuntimeException exception) {
            activity.showToast("Ошибка платежной системы. Попробуйте попозже.\n" + exception.getLocalizedMessage());
        }


    }

    public void showAnotherPaymentDialog(MainAppCompatActivity activity) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.CustomBottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_another_payments_available,
                        activity.findViewById(R.id.modalBottomAddPayment));


        if (qiwiTerminal.equals("")) {
            bottomSheetView.findViewById(R.id.cvQiwiTerminal).setVisibility(View.GONE);
        } else {
            bottomSheetView.findViewById(R.id.cvQiwiTerminal).setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.cvQiwiTerminal).setOnClickListener(view -> activity.goToURL(qiwiTerminal));
            ((TextView) bottomSheetView.findViewById(R.id.tvQiwiTerminalNote)).setText(qiwiTerminalNote);
        }
        if (qiwiWallet.equals("")) {
            bottomSheetView.findViewById(R.id.cvQiwiWallet).setVisibility(View.GONE);
        } else {
            bottomSheetView.findViewById(R.id.cvQiwiWallet).setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.cvQiwiWallet).setOnClickListener(view -> activity.goToURL(qiwiWallet));
            ((TextView) bottomSheetView.findViewById(R.id.tvQiwiWalletNote)).setText(qiwiWalletNote);
        }

        if (ckassa.equals("")) {
            bottomSheetView.findViewById(R.id.cvCKassa).setVisibility(View.GONE);
        } else {
            bottomSheetView.findViewById(R.id.cvCKassa).setVisibility(View.VISIBLE);
            bottomSheetView.findViewById(R.id.cvCKassa).setOnClickListener(view -> activity.goToURL(ckassa));
            ((TextView) bottomSheetView.findViewById(R.id.tvCKassaNote)).setText(ckassaNote);
        }
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
