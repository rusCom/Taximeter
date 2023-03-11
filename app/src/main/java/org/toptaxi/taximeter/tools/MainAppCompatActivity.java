package org.toptaxi.taximeter.tools;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainAppCompatActivity extends AppCompatActivity {
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Если по какой-либо причине данные по водителю не загружены, то загружаем их. Бывает такое, когда из спящего режима востанавливается приложение
        if (!MainApplication.getInstance().getMainAccount().isParsedData) {
            httpGetResult("/profile/auth");
            MainApplication.getInstance().startMainService();
        }

    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Получение данных ...");
        }
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void goToURL(String url) {
        if (url != null) {
            Uri uriLink = Uri.parse(url);
            Intent paymentInstructionLinkIntent = new Intent(Intent.ACTION_VIEW, uriLink);
            startActivity(paymentInstructionLinkIntent);
        }
    }

    public void showSimpleDialog(String dialogText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogText);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    protected void httpGetResult(String path){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            MainApplication.getInstance().getRestService().httpGet(this, path);
            /*
            String errorText = "";
            runOnUiThread(this::showProgressDialog);
            JSONObject response = MainApplication.getInstance().getRestService().httpGet(path);
            if (JSONGetString(response, "status").equals("OK")){
                if (response.has("result")){
                    try {
                        MainApplication.getInstance().parseData(response.getJSONObject("result"));
                    } catch (JSONException ignored) {
                    }
                }
            } // if (JSONGetString(response, "status").equals("OK")){
            else {
                errorText = JSONGetString(response, "result");
                if (errorText.equals("")){
                    errorText = response.toString();
                }
            }
            runOnUiThread(this::dismissProgressDialog);
            if (!errorText.equals("")){
                String finalErrorText = errorText;
                runOnUiThread(()->showToast(finalErrorText));
            }

             */
        });

    }
}
