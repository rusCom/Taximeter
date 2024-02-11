package org.toptaxi.taximeter.adapters;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.MessagesActivity;
import org.toptaxi.taximeter.activities.OrdersOnCompleteActivity;
import org.toptaxi.taximeter.activities.PriorOrderActivity;
import org.toptaxi.taximeter.data.MainActionItem;
import org.toptaxi.taximeter.tools.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnMainActionClickListener implements AdapterView.OnItemClickListener {
    protected static String TAG = "#########" + OnMainActionClickListener.class.getName();

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Context context = view.getContext();
        TextView textViewItem = view.findViewById(R.id.tvMainActionTitle);
        MainActionItem mainActionItem = (MainActionItem) textViewItem.getTag();
        //Log.d(TAG,  "onItemClick " + mainActionItem.getActionName());

        if (MainApplication.getInstance().getMainActivity() != null) {
            ((MainActivity) context).mainActionsDialog.cancel();

            switch (mainActionItem.getAction()) {
                case Constants.MAIN_ACTION_GO_ONLINE, Constants.MAIN_ACTION_GO_OFFLINE ->
                        MainApplication.getInstance().getMainActivity().driverGoOffLine();
                case Constants.MAIN_ACTION_TARIFF_PLAN ->
                        MainApplication.getInstance().getMainActivity().onTariffPlanClick();
                case Constants.MAIN_ACTION_PRIOR_ORDER -> {
                    if (MainApplication.getInstance().getMainAccount().getCheckPriorOrder()) {
                        Intent priorOrders = new Intent(MainApplication.getInstance().getMainActivity(), PriorOrderActivity.class);
                        MainApplication.getInstance().getMainActivity().startActivity(priorOrders);
                    } else {
                        AlertDialog.Builder adb = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
                        adb.setMessage(MainApplication.getInstance().getPreferences().getCheckPriorErrorText());
                        adb.setIcon(android.R.drawable.ic_dialog_info);
                        adb.setPositiveButton("Ok", null);
                        adb.create();
                        adb.show();
                    }
                }
                case Constants.MAIN_ACTION_SEND_MESSAGE -> {
                    Intent messagesIntent = new Intent(MainApplication.getInstance().getMainActivity(), MessagesActivity.class);
                    MainApplication.getInstance().getMainActivity().startActivity(messagesIntent);
                }
                case Constants.MENU_TEMPLATE_MESSAGE -> {
                    String message = textViewItem.getText().toString();
                    Integer res = MainApplication.getInstance().getMainMessages().checkSendingTemplateMessage(message);
                    if (res == 0) {
                        try {
                            message = URLEncoder.encode(message, "UTF-8");
                        } catch (UnsupportedEncodingException ignored) {
                        }
                        sendMessageThread(message);

                        // MainApplication.getInstance().getRestService().httpGetResult("/last/messages/send?text=" + message);
                    } else {
                        MainApplication.getInstance().showToast("Повторная отправка сообщения доступна через " + (60 - res) + " секунд. Ожидайте ответа.");
                    }
                }
                case Constants.MAIN_ACTION_ORDERS_COMPLETE ->
                        MainApplication.getInstance().getMainActivity().startActivity(new Intent(MainApplication.getInstance().getMainActivity(), OrdersOnCompleteActivity.class));
                case Constants.MENU_CLOSE_APPLICATION ->
                        MainApplication.getInstance().getMainActivity().onBackPressed();
            }
        }
    }

    void sendMessageThread(String message){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            MainApplication.getInstance().showProgressDialog();
            JSONObject response = MainApplication.getInstance().getRestService().httpGet("/messages/send?message=" + message);
            MainApplication.getInstance().dismissProgressDialog();
            if (JSONGetString(response, "status_code").equals("200")){
                try {
                    MainApplication.getInstance().parseData(response.getJSONObject("result"));
                } catch (JSONException ignored) {
                }
                MainApplication.getInstance().showToast("Сообщение доставлено. Ожидайте ответ.");
            }
            else if (JSONGetString(response, "status_code").equals("400")){
                MainApplication.getInstance().showToast(JSONGetString(response, "result"));
            }
            else {
                MainApplication.getInstance().showToast(response.toString());
            }
        });

    }

}
