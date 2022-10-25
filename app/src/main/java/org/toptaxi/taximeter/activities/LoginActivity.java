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

import androidx.appcompat.app.AppCompatActivity;

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
        btnActivityLoginProfileRegistration.setTextSize((float) (btnActivityLoginProfileRegistration.getTextSize() * 1.5));



        btnActivityLoginProfileLogin.setOnClickListener(this::profileLoginClick);
        btnActivityLoginProfileRegistration.setOnClickListener(this::profileRegistrationClick);

        llActivityLoginProgress.setVisibility(View.GONE);

        setDocumentsText();


    }

    public void setDocumentsText(){
        if (!MainApplication.getInstance().getPreferences().privacyPolicyLink.equals("")
            && !MainApplication.getInstance().getPreferences().publicOfferLink.equals(""))
        {
            String text = "<html>Нажимая кнопку \"Получить код\" и “Вход”,  Вы соглашаетесь с " +
                    "\"<a href=\"" + MainApplication.getInstance().getPreferences().publicOfferLink + "\">Лицензионным соглашением</a>\"" +
                    ", а также с обработкой персональной информации на условиях " +
                    "\"<a href=\"" + MainApplication.getInstance().getPreferences().privacyPolicyLink + "\">Политики конфиденциальности</a>\".</html>";
            tvActivityLoginDocuments.setText(Html.fromHtml(text));
            tvActivityLoginDocuments.setVisibility(View.VISIBLE);
            tvActivityLoginDocuments.setMovementMethod(LinkMovementMethod.getInstance());
            tvActivityLoginDocuments.setLinkTextColor(Color.BLUE);
        }
        else {
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
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
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
        }
        else if(MainUtils.convertPhone(phone).equals("")){
            ilActivityLoginPhone.setError("Введен не корректный номер телефона");
            edActivityLoginPhone.requestFocus();
            return false;
        }
        else {
            ilActivityLoginPhone.setErrorEnabled(false);
        }
        return true;
    }

    public void profileLoginClick(View view) {
        if (validatePhone()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
            editor.apply();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                LogService.getInstance().log("LoginActivity", "profileLoginClick");
                runOnUiThread(this::showLoadingDialog);
                JSONObject data = MainApplication.getInstance().getRestService().httpGet("/profile/login?phone=" + edActivityLoginPhone.getText().toString().trim());
                runOnUiThread(this::dismissLoadingDialog);
                LogService.getInstance().log("LoginActivity", data.toString());
                if (JSONGetString(data, "status").equals("OK")) {
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

                    executorService.execute(()->{
                        String waitType = "Повторный запрос доступен через ";
                        int Timer = JSONGetInteger(data, "result_timeout", 90);
                        if (JSONGetString(data, "result_type").equals("call")){
                            waitType = "Ожидайте звонка от робота. Надо ответить на звонок и прослушать код для входа. " + waitType;
                        }
                        runOnUiThread(()->{
                            llActivityLoginProgress.setVisibility(View.VISIBLE);
                            btnActivityLoginProfileLogin.setVisibility(View.GONE);
                        });
                        while (Timer > 0){
                            String message = waitType + " " + Timer + " секунд.";
                            runOnUiThread(()-> tvActivityLoginTimer.setText(message));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {
                            }
                            Timer --;
                        }

                        runOnUiThread(()->{
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
                runOnUiThread(()->{
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

        builder.setMessage("Водитель с данным номером телефона не зарегестрирован. Для регистрации необходимо позвонить по номеру: "
                + supportPhone + " или пройти самостоятельную регистрацию онлайню.");
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
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(registrationApp);
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }
            else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + registrationApp)));
                } catch (android.content.ActivityNotFoundException activityNotFoundException) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + registrationApp)));
                }
            }

        });
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.show();
    }

}
