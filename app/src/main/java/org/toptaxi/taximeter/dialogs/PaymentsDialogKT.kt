package org.toptaxi.taximeter.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONObject
import org.toptaxi.taximeter.MainApplication
import org.toptaxi.taximeter.R
import org.toptaxi.taximeter.tools.MainAppCompatActivity
import org.toptaxi.taximeter.tools.MainUtils
import ru.tinkoff.acquiring.sdk.TinkoffAcquiring
import ru.tinkoff.acquiring.sdk.models.enums.CheckType
import ru.tinkoff.acquiring.sdk.models.options.screen.PaymentOptions
import ru.tinkoff.acquiring.sdk.redesign.mainform.MainFormLauncher
import ru.tinkoff.acquiring.sdk.redesign.sbp.SbpPayLauncher
import ru.tinkoff.acquiring.sdk.utils.Money
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

object PaymentsDialogKT {

    private var tbank = false;
    private var tbank_commission = 0;

    fun setPreferences(data: JSONObject) {
        tbank_commission = MainUtils.JSONGetInteger(data, "tbank_commission", 0)
        tbank = MainUtils.JSONGetBool(data, "tbank", false)
    }

    fun getPaymentsAvailable(): Boolean {
        return tbank
    }


    @SuppressLint("SetTextI18n")
    fun showPaymentDialog(activity: MainAppCompatActivity) {
        val bottomSheetDialog = BottomSheetDialog(activity, R.style.CustomBottomSheetDialog)
        val bottomSheetView: View = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_add_payment, activity.findViewById(R.id.modalBottomAddPayment))

        val buttonSPBPayment = bottomSheetView.findViewById<Button>(R.id.btnSBPPayment)
        val buttonTinkoffPayment = bottomSheetView.findViewById<Button>(R.id.btnTinkoffPayment)

        val ednPaymentAmount = bottomSheetView.findViewById<EditText>(R.id.ednPaymentAmount)


        bottomSheetView.findViewById<View>(R.id.btnPayment100).setOnClickListener { ednPaymentAmount.setText("100") }
        bottomSheetView.findViewById<View>(R.id.btnPayment300).setOnClickListener { ednPaymentAmount.setText("300") }
        bottomSheetView.findViewById<View>(R.id.btnPayment500).setOnClickListener { ednPaymentAmount.setText("500") }

        ednPaymentAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                ednPaymentAmount.error = null
                var amountString = ednPaymentAmount.text.toString()
                if (amountString.contains(".")) {
                    amountString = amountString.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    ednPaymentAmount.setText(amountString)
                }
                if (amountString.contains(",")) {
                    amountString = amountString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    ednPaymentAmount.setText(amountString)
                }
            }
        })


        val sbpShowMessage = AtomicReference(false)


        buttonSPBPayment.setOnClickListener {
            val amountString = ednPaymentAmount.text.toString()
            if (amountString == "") {
                ednPaymentAmount.requestFocus()
                ednPaymentAmount.error = "Введите сумму платежа"
                return@setOnClickListener
            }

            val amountInteger = amountString.toInt() * 100
            val executorService = Executors.newSingleThreadExecutor()
            executorService.execute {
                val response =
                    MainApplication.getInstance().restService.httpGet(activity, "/payments/order?amount=$amountInteger&source=sbp")
                if (MainUtils.JSONGetString(response, "status") == "OK") {
                    val showMessage = MainUtils.JSONGetBool(response, "result_message")
                    if (!sbpShowMessage.get() && showMessage) {
                        activity.runOnUiThread {
                            val builder = AlertDialog.Builder(activity)
                            builder.setMessage("Комиссия при оплате через СПБ или картой любого банка $tbank_commission%.")
                            builder.setCancelable(true)
                            builder.setPositiveButton("Понятно") { dialog: DialogInterface, _: Int ->
                                sbpShowMessage.set(true)
                                dialog.dismiss()
                                bottomSheetDialog.dismiss()
                                sbpPay(
                                    activity,
                                    MainUtils.JSONGetString(response, "result_terminal_key"),
                                    MainUtils.JSONGetString(response, "result_public_key"),
                                    MainUtils.JSONGetString(response, "result_number"), amountInteger.toLong()
                                )
                            }
                            builder.setNegativeButton("Отмена") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                            builder.show()
                        }
                    } else {
                        bottomSheetDialog.dismiss()
                        sbpPay(
                            activity,
                            MainUtils.JSONGetString(response, "result_terminal_key"),
                            MainUtils.JSONGetString(response, "result_public_key"),
                            MainUtils.JSONGetString(response, "result_number"), amountInteger.toLong()
                        )
                    }
                }
            }
        }


        val tinkoffShowMessage = AtomicReference(false)
        buttonTinkoffPayment.setOnClickListener {
            val amountString = ednPaymentAmount.text.toString()
            if (amountString == "") {
                ednPaymentAmount.requestFocus()
                ednPaymentAmount.error = "Введите сумму платежа"
                return@setOnClickListener
            }

            val amountInteger = amountString.toInt() * 100
            val executorService = Executors.newSingleThreadExecutor()
            executorService.execute {
                val response =
                    MainApplication.getInstance().restService
                        .httpGet(activity, "/payments/order?amount=$amountInteger&source=tinkoff")
                if (MainUtils.JSONGetString(response, "status") == "OK") {
                    val showMessage = MainUtils.JSONGetBool(response, "result_message")
                    if (!tinkoffShowMessage.get() && showMessage) {
                        activity.runOnUiThread {
                            val builder = AlertDialog.Builder(activity)
                            builder.setMessage("Оплата картой любого банка. Комиссия $tbank_commission%.")
                            builder.setCancelable(true)
                            builder.setPositiveButton("Понятно") { dialog: DialogInterface, _: Int ->
                                sbpShowMessage.set(true)
                                dialog.dismiss()
                                bottomSheetDialog.dismiss()
                                tinkoffPay(
                                    activity,
                                    MainUtils.JSONGetString(response, "result_terminal_key"),
                                    MainUtils.JSONGetString(response, "result_public_key"),
                                    MainUtils.JSONGetString(response, "result_number"), amountInteger.toLong()
                                )
                            }
                            builder.setNegativeButton(
                                "Отмена"
                            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                            builder.show()
                        }
                    } else {
                        bottomSheetDialog.dismiss()

                        tinkoffPay(
                            activity,
                            MainUtils.JSONGetString(response, "result_terminal_key"),
                            MainUtils.JSONGetString(response, "result_public_key"),
                            MainUtils.JSONGetString(response, "result_number"), amountInteger.toLong()
                        )
                    }
                }
            }
        }




        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun sbpPay(
        activity: MainAppCompatActivity,
        terminalKey: String,
        publicKey: String,
        orderID: String,
        summa: Long
    ) {
        val tinkoffAcquiring = TinkoffAcquiring(
            context = activity,
            terminalKey = terminalKey,
            publicKey = publicKey
        )
        tinkoffAcquiring.initSbpPaymentSession()
        val options = PaymentOptions().setOptions {
            setTerminalParams(
                terminalKey = terminalKey,
                publicKey = publicKey
            )
            orderOptions {
                orderId = orderID
                amount = Money.ofCoins(summa)
                title = "Пополнение баланса aTaxi.Водитель"
                recurrentPayment = false
            }
        }
        activity.mainFormSBPPaymentLauncher.launch(
            SbpPayLauncher.StartData(
                paymentOptions = options
            )
        )


    }


    private fun tinkoffPay(
        activity: MainAppCompatActivity,
        terminalKey: String,
        publicKey: String,
        orderID: String,
        summa: Long
    ) {


        val paymentOptions = PaymentOptions().setOptions {
            setTerminalParams(terminalKey, publicKey)
            orderOptions {
                orderId = orderID
                amount = Money.ofCoins(summa)
                title = "Пополнение баланса aTaxi.Водитель"
                recurrentPayment = false
            }
            customerOptions {
                customerKey = MainApplication.getInstance().mainAccount.token
                checkType = CheckType.NO.toString()
            }
            featuresOptions {
                useSecureKeyboard = true
            }
        }
        activity.mainFormPaymentLauncher.launch(MainFormLauncher.StartData(paymentOptions))
    }


}