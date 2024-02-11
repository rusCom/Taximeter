package org.toptaxi.taximeter.tools;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.services.LogService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainAppCompatActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    protected boolean isCheckProfileAuth = true;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCheckProfileAuth){
            // Если по какой-либо причине данные по водителю не загружены, то загружаем их. Бывает такое, когда из спящего режима востанавливается приложение
            if (!MainApplication.getInstance().getMainAccount().isParsedData) {
                httpGetResult("/profile/auth");
                MainApplication.getInstance().startMainService();
            }
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
                try {
                    progressDialog.dismiss();
                } catch (Exception ignored) {
                }
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

    public void callIntent(String phone) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialIntent);
    }

    public void showSimpleDialog(String dialogText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogText);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    protected void httpGetResult(String path) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            runOnUiThread(this::showProgressDialog);
            JSONObject result = MainApplication.getInstance().getRestService().httpGet(path);
            runOnUiThread(this::dismissProgressDialog);
        });
    }

    protected void httpGetResult(String path, int requestUID) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            runOnUiThread(this::showProgressDialog);
            JSONObject result = MainApplication.getInstance().getRestService().httpGet(path);
            runOnUiThread(this::dismissProgressDialog);
            onHttpGetResult(result, requestUID);
            runOnUiThread(() -> onHttpGetResult(result, requestUID));
        });
    }

    protected void httpPostResult(String path, JSONObject data) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            runOnUiThread(this::showProgressDialog);
            JSONObject result = MainApplication.getInstance().getRestService().httpPost(path, data);
            runOnUiThread(this::dismissProgressDialog);
        });
    }

    protected void onHttpGetResult(JSONObject data, int requestUID){

    }
}
