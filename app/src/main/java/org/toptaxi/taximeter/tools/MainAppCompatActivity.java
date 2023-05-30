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
import org.toptaxi.taximeter.services.LogService;

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

    protected void httpGetResult(String path){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            MainApplication.getInstance().getRestService().httpGet(this, path);
        });

    }
}
