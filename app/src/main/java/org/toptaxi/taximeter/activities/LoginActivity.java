package org.toptaxi.taximeter.activities;


import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.LockOrientation;
import org.toptaxi.taximeter.tools.MainUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText edActivityLoginPhone, edActivityLoginCode;
    private TextInputLayout ilActivityLoginPhone, ilActivityLoginPassword;
    private LinearLayout llActivityLoginProgress;
    private TextView tvActivityLoginTimer;
    private Button btnActivityLoginProfileLogin;
    private SharedPreferences sharedPreferences;
    private TextView tvActivityLoginDocuments;
    ProgressDialog progressDialog;
    InstallReferrerClient referrerClient;
    String referrerUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_login);
        edActivityLoginPhone = findViewById(R.id.edActivityLoginPhone);
        edActivityLoginCode = findViewById(R.id.edActivityLoginCode);
        ilActivityLoginPhone = findViewById(R.id.ilActivityLoginPhone);
        ilActivityLoginPassword = findViewById(R.id.ilActivityLoginPassword);
        llActivityLoginProgress = findViewById(R.id.llActivityLoginProgress);
        tvActivityLoginTimer = findViewById(R.id.tvActivityLoginTimer);
        tvActivityLoginDocuments = findViewById(R.id.tvActivityLoginDocuments);
        btnActivityLoginProfileLogin = findViewById(R.id.btnActivityLoginProfileLogin);

        edActivityLoginPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());


        Button btnActivityLoginProfileRegistration = findViewById(R.id.btnActivityLoginProfileRegistration);

        btnActivityLoginProfileLogin.setOnClickListener(view -> profileLoginClick(false));
        btnActivityLoginProfileRegistration.setOnClickListener(this::profileRegistrationClick);

        llActivityLoginProgress.setVisibility(View.GONE);

        setDocumentsText();

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    try {
                        ReferrerDetails response = referrerClient.getInstallReferrer();
                        referrerUrl = response.getInstallReferrer();
                    } catch (RemoteException ignored) {
                    }
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {

            }
        });


        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);


    }

    OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    public void setDocumentsText() {
        if (!MainApplication.getInstance().getPreferences().getLicenseAgreementLink().equals("")) {
            String text = "<html>Нажимая кнопку \"Получить код\" и “Вход”,  Вы соглашаетесь с " +
                    "\"<a href=\"" + MainApplication.getInstance().getPreferences().getLicenseAgreementLink() + "\">Лицензионным соглашением</a>\"";
            if (!MainApplication.getInstance().getPreferences().getPrivacyPolicyLink().equals("")) {
                text += ", а также с обработкой персональной информации на условиях " +
                        "\"<a href=\"" + MainApplication.getInstance().getPreferences().getPrivacyPolicyLink() + "\">Политики конфиденциальности</a>\"";
            }
            text += "</html>";
            tvActivityLoginDocuments.setText(Html.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
            tvActivityLoginDocuments.setVisibility(View.VISIBLE);
            tvActivityLoginDocuments.setMovementMethod(LinkMovementMethod.getInstance());
            tvActivityLoginDocuments.setLinkTextColor(Color.BLUE);
        } else {
            tvActivityLoginDocuments.setVisibility(View.GONE);
        }

    }

    public void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Получение данных ...");
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        edActivityLoginPhone.setText(sharedPreferences.getString("accountPhone", ""));
        edActivityLoginCode.setText(sharedPreferences.getString("accountCode", ""));
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
        editor.apply();
    }

    private boolean validatePassword() {
        if (edActivityLoginCode.getText().toString().trim().equals("")) {
            ilActivityLoginPassword.setError(getString(R.string.errorPasswordNotEntered));
            edActivityLoginCode.requestFocus();
            return false;
        } else if (edActivityLoginCode.getText().toString().trim().length() != 4) {
            ilActivityLoginPassword.setError(getString(R.string.errorPasswordNotEntered));
            edActivityLoginCode.requestFocus();
            return false;
        } else {
            ilActivityLoginPassword.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePhone() {
        String phone = edActivityLoginPhone.getText().toString().trim();
        if (phone.equals("")) {
            ilActivityLoginPhone.setError(getString(R.string.errorPhoneNotEntered));
            edActivityLoginPhone.requestFocus();
            return false;
        } else if (MainUtils.convertPhone(phone).equals("")) {
            ilActivityLoginPhone.setError("Введен не корректный номер телефона");
            edActivityLoginPhone.requestFocus();
            return false;
        } else {
            ilActivityLoginPhone.setErrorEnabled(false);
        }
        return true;
    }

    public void profileLoginClick(boolean force) {
        if (validatePhone()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
            editor.apply();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                LogService.getInstance().log("LoginActivity", "profileLoginClick");
                runOnUiThread(this::showLoadingDialog);
                String url = "/profile/login?phone=" + edActivityLoginPhone.getText().toString().trim();
                if (force) {
                    url += "&force=true&";
                    if (referrerUrl != null) {
                        url += referrerUrl;
                    }
                }
                JSONObject data = MainApplication.getInstance().getRestService().httpGet(url);
                runOnUiThread(this::dismissLoadingDialog);
                LogService.getInstance().log("LoginActivity", data.toString());
                if (JSONGetString(data, "status").equals("OK")) {
                    /* Вернуть, если придеться делать, что у каджой организации своя ссылка на licenseAgreementLink
                    boolean changeDocumentsLink = false;
                    if (!JSONGetString(data, "result_privacy_policy_link").equals("")){
                        MainApplication.getInstance().getPreferences().privacyPolicyLink = JSONGetString(data, "result_privacy_policy_link");
                        changeDocumentsLink = true;
                    }
                    if (!JSONGetString(data, "result_public_offer_link").equals("")){
                        MainApplication.getInstance().getPreferences().publicOfferLink = JSONGetString(data, "result_public_offer_link");
                        changeDocumentsLink = true;
                    }
                    if (changeDocumentsLink){
                        runOnUiThread(this::setDocumentsText);
                    }

                     */

                    executorService.execute(() -> {
                        String waitType = "Повторный запрос доступен через ";
                        int Timer = JSONGetInteger(data, "result_timeout", 90);
                        if (JSONGetString(data, "result_type").equals("call")) {
                            waitType = "Ожидайте звонка от робота. Надо ответить на звонок и прослушать код для входа. " + waitType;
                        }
                        runOnUiThread(() -> {
                            llActivityLoginProgress.setVisibility(View.VISIBLE);
                            btnActivityLoginProfileLogin.setVisibility(View.GONE);
                        });
                        while (Timer > 0) {
                            String message = waitType + " " + Timer + " секунд.";
                            runOnUiThread(() -> tvActivityLoginTimer.setText(message));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {
                            }
                            Timer--;
                        }

                        runOnUiThread(() -> {
                            llActivityLoginProgress.setVisibility(View.GONE);
                            btnActivityLoginProfileLogin.setVisibility(View.VISIBLE);
                        });
                    });
                } else if (JSONGetString(data, "status").equals("Unauthorized")) {
                    runOnUiThread(() -> showRegisterDialog(JSONGetString(data, "result_support_phone"), JSONGetString(data, "result_registration_app")));
                } else {
                    runOnUiThread(() -> showToast(JSONGetString(data, "result")));
                }
            }); // executorService.execute(() -> {
        } // if (validatePhone()){
    }

    public void profileRegistrationClick(View view) {
        if (!validatePhone()) return;
        if (!validatePassword()) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String accountPhone = edActivityLoginPhone.getText().toString().trim();
        String accountCode = edActivityLoginCode.getText().toString().trim();
        editor.putString("accountPhone", accountPhone);
        editor.putString("accountCode", accountCode);
        editor.apply();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            LogService.getInstance().log("LoginActivity", "profileRegistrationClick");
            runOnUiThread(this::showLoadingDialog);
            JSONObject data = MainApplication.getInstance().getRestService().httpGet("/profile/registration?phone=" + accountPhone + "&code=" + accountCode);
            runOnUiThread(this::dismissLoadingDialog);
            LogService.getInstance().log("LoginActivity", data.toString());
            if (JSONGetString(data, "status").equals("OK")) {
                MainApplication.getInstance().getMainAccount().setToken(JSONGetString(data, "result"));
                MainApplication.getInstance().getRestService().reloadHeader();
                runOnUiThread(() -> {
                    setResult(RESULT_OK);
                    finish();
                });
            } else if (JSONGetString(data, "status").equals("Unauthorized")) {
                runOnUiThread(() -> showRegisterDialog(JSONGetString(data, "result_support_phone"), JSONGetString(data, "result_registration_app")));
            } else {
                runOnUiThread(() -> showToast(JSONGetString(data, "result")));
            }
        });
    }

    private void showRegisterDialog(final String supportPhone, final String registrationApp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Водитель с данным номером телефона не зарегистрирован в аТакси. Для регистрации необходимо позвонить по номеру: \n"
                + supportPhone + " или пройти регистрацию онлайн.");
        builder.setCancelable(true);
        builder.setPositiveButton("Позвонить", (dialog, which) -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + supportPhone));
            startActivity(callIntent);
            dialog.dismiss();
            setResult(RESULT_CANCELED);
            finish();

        });

        builder.setNeutralButton("Регистрация", (dialog, which) -> {
            AlertDialog.Builder builderReg = new AlertDialog.Builder(this);
            String message = "Для регистрации онлайн необходимо будет предоставить фотографии следующих документов:\n" +
                    " - паспорт гражданина РФ;\n" +
                    " - водительское удостоверние РФ;\n" +
                    " - свидетельство о регистрации автомобиля.\n" +
                    "Так же обращаем Ваше внимание, что необходим стаж вождения не менее 3 лет.";

            builderReg.setMessage(message);
            builderReg.setCancelable(true);
            builderReg.setPositiveButton("Зарегестрироваться", (dialog2, which2) -> {
                profileLoginClick(true);
            });
            builderReg.show();




            /*

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(registrationApp);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + registrationApp)));
                } catch (android.content.ActivityNotFoundException activityNotFoundException) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + registrationApp)));
                }
            }

             */


        });
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.show();
    }

}
