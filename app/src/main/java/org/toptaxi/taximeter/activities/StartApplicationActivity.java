package org.toptaxi.taximeter.activities;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;
import org.toptaxi.taximeter.tools.MainUtils;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartApplicationActivity extends AppCompatActivity {
    TextView tvAction;

    boolean isFinished = false;
    boolean isShowNewVersionDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogService.getInstance().log(this, "onCreate");

        new LockOrientation(this).lock();
        setContentView(R.layout.activity_splash);

        tvAction = findViewById(R.id.tvSplashAction);
        ((TextView) findViewById(R.id.tvSplashVersion)).setText(MainApplication.getInstance().getAppVersion());
        ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).cancelAll();
        checkPermissions();
    }


    @SuppressLint("SetTextI18n")
    private void checkPermissions() {
        Boolean startApplication = true;
        tvAction.setText("Проверка подключения к Сети Интернет");
        if (!isNetworkAvailable()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Для работы программы необходимо подключение к интернет. Проверьте подключение и попробуйте еще раз.");
            alertDialog.setPositiveButton("Ok", (dialogInterface, i) -> onBackPressed());
            alertDialog.create();
            alertDialog.show();
            startApplication = false;
        }
        tvAction.setText("Проверка доступа к GPS");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_LOCATION);
                return;
            }
        }
        tvAction.setText("Проверка подключения к GPS");
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton("Открыть настройки", (paramDialogInterface, paramInt) -> {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
                onBackPressed();
            });
            dialog.show();
            startApplication = false;
        }


        /*

        if (startApplication) {
            Intent intent = new Intent();
            String packageName = this.getPackageName();
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    LogService.getInstance().log(this, "isIgnoringBatteryOptimizations");
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    this.startActivity(intent);
                    startApplication = false;
                }
            }

        }

         */


        LogService.getInstance().log(this, "checkPermissions", "startInit");

        if (startApplication) {
            if (MainApplication.getInstance().isMainServiceStart()) { // Если сервис уже запущен, значит само приложение так же уже работает, пожтому просто запускаем
                if (!MainApplication.getInstance().getMainAccount().isParsedData) {
                    try {
                        JSONObject result = MainApplication.getInstance().getRestService().httpGet("/profile/auth");
                        JSONObject authData = result.getJSONObject("result");
                        MainApplication.getInstance().parseData(authData);
                    } catch (JSONException ignored) {
                    }
                }
                startApplication();
            } else {
                auth();
            }

        }
    }

    private void auth() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            LogService.getInstance().log("StartApplicationActivity", "start auth");

            runOnUiThread(() -> tvAction.setText("Запуск приложения ..."));
            JSONObject result = MainApplication.getInstance().getRestService().httpGet("/profile/auth");
            if (isFinished) return;

            try {
                JSONObject data = result.getJSONObject("result");
                if (result.getString("status_code").equals("200")) {
                    runOnUiThread(() -> tvAction.setText("Обработка данных ..."));
                    MainApplication.getInstance().parseData(data);


                    if (data.has("application")) {
                        JSONObject application = data.getJSONObject("application");
                        Integer necessaryVersion = JSONGetInteger(application, "android_necessary_version", 0);
                        Integer desirableVersion = JSONGetInteger(application, "android_desirable_version", 0);
                        if (MainApplication.getInstance().getAppVersionCode() < necessaryVersion) {
                            runOnUiThread(this::showNewNecessaryVersionDialog);
                            isFinished = true;
                        } else if (MainApplication.getInstance().getAppVersionCode() < desirableVersion && !isShowNewVersionDialog) {
                            runOnUiThread(this::showNewDesirableVersionDialog);
                            isFinished = true;
                        }
                    }

                    if (!isFinished) {
                        // TODO когда на сервере переведем получение данных в запрос auth убрать данный пункт
                        JSONObject data2 = MainApplication.getInstance().getRestService().httpGet("/last/data").getJSONObject("result");
                        MainApplication.getInstance().parseData(data2);
                        runOnUiThread(() -> tvAction.setText("Запуск приложения ..."));

                        MainApplication.getInstance().startMainService();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        LogService.getInstance().log("sys", "startMainActivity");
                        startActivity(intent);
                    }

                } else if (result.getString("status_code").equals("401")) {
                    MainApplication.getInstance().getPreferences().parseData(data);
                    MainApplication.getInstance().getFirebaseService().clearData();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginActivityResultLauncher.launch(loginIntent);
                } else if (result.getString("status_code").equals("403")) {
                    runOnUiThread(() -> showBlockedDialog(data));
                } else if (result.getString("status_code").equals("451")) {
                    runOnUiThread(() -> showDocumentDialog(data));
                } else {
                    String res = result.toString();
                    runOnUiThread(() -> Toast.makeText(StartApplicationActivity.this, res, Toast.LENGTH_LONG).show());
                }

            } catch (JSONException e) {
                LogService.getInstance().log(this, "initException", e.toString());
                LogService.getInstance().log(this, "initException", result.toString());

                runOnUiThread(() -> Toast.makeText(StartApplicationActivity.this, e.toString(), Toast.LENGTH_LONG).show());
            }
        });

    }


    private void startApplication() {
        LogService.getInstance().log(this, "startApplication");
        // Запускаем сервис

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainApplication.getInstance().stopMainService();
        isFinished = true;
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACTIVITY_LOGIN) {
            if (resultCode == RESULT_CANCELED) {
                onBackPressed();
            } else checkPermissions();
        }

    }


    ActivityResultLauncher<Intent> loginActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    MainApplication.getInstance().getFirebaseService().getNewPushToken();
                    auth();
                } else {
                    onBackPressed();
                }
            });


    private void showBlockedDialog(JSONObject data) {
        String phone = MainUtils.JSONGetString(data, "support_phone", "+7 (347) 294-73-99");
        String callSign = MainUtils.JSONGetString(data, "callsign");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Вы заблокированны. Для разблокировки необходимо позвонить по номеру: \n" + phone + ".\nВаш позывной: " + callSign);
        builder.setCancelable(false);
        builder.setPositiveButton("Позвонить", (dialog, which) -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            startActivity(callIntent);
            onBackPressed();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.dismiss();
            onBackPressed();
        });
        builder.show();
    }


    private void acceptDocumentThread(String doc_type) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            LogService.getInstance().log("StartApplicationActivity", "start acceptDocumentThread");

            runOnUiThread(() -> tvAction.setText("Подтверждение принятия ..."));
            MainApplication.getInstance().getRestService().httpGet("/profile/document/accept?doc_type=" + doc_type);
            if (isFinished) return;
            auth();

        });

    }

    private void showDocumentDialog(JSONObject data) {
        String doc_name = MainUtils.JSONGetString(data, "doc_name");
        String link = MainUtils.JSONGetString(data, "doc_link");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String Text = "Для продолжения работы необходимо ознакомиться и подтвердить принятие следующего документа: \n\"" + doc_name + "\".\n" +
                "С полным текстом документа можно ознакомиться по адресу в сети интернет: \n" + link;

        builder.setTitle(doc_name);

        final TextView message = new TextView(builder.getContext());
        message.setText(Text);
        message.setAutoLinkMask(Linkify.WEB_URLS);
        message.setLinksClickable(true);
        message.setPadding(50, 50, 50, 50);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setView(message);


        builder.setPositiveButton("Принимаю", (dialog, which) -> {
            dialog.dismiss();
            acceptDocumentThread(MainUtils.JSONGetString(data, "doc_type"));
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.dismiss();
            onBackPressed();
        });
        builder.show();
    }

    private void showNewDesirableVersionDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);

        alertDialog.setMessage("Для корректной работы приложения рекомендуется обновить приложение");
        alertDialog.setNegativeButton("Позже", (dialogInterface, i) -> {
            isShowNewVersionDialog = true;
            isFinished = false;
            auth();

        });

        alertDialog.setPositiveButton("Обновить", (dialogInterface, i) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=org.toptaxi.taximeter"));
            startActivity(intent);
            onBackPressed();
        });
        alertDialog.create();
        alertDialog.show();
    }

    private void showNewNecessaryVersionDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);

        alertDialog.setMessage("Для корректной работы приложения необходимо обновить приложение");
        alertDialog.setNegativeButton("Отмена", (dialogInterface, i) -> onBackPressed());

        alertDialog.setPositiveButton("Обновить", (dialogInterface, i) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=org.toptaxi.taximeter"));
            startActivity(intent);
            onBackPressed();
        });
        alertDialog.create();
        alertDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Для продолжения работы необходимо дать разрешение на доступ к GPS данным", Toast.LENGTH_LONG).show();
            }
            checkPermissions();
        }

    }

    private boolean isNetworkAvailable() {
        int[] networkTypes = {ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI};
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null &&
                        activeNetworkInfo.getType() == networkType)
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


}
