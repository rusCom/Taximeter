package org.toptaxi.taximeter.activities.registration;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.StartApplicationActivity;
import org.toptaxi.taximeter.data.SupportContactItem;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;

public class RegistrationStateActivity extends MainAppCompatActivity {
    TextView tvRegistrationStateSupportPhone;
    TextView tvRegistrationStateStatusName;
    ProgressBar pbRegistrationState;
    ShapeableImageView ibRegistrationStateWhatsapp;
    ShapeableImageView ibRegistrationStateTelegram;
    MaterialButton btnRegistrationStateAgain;
    MaterialButton btnRegistrationStateDelete;
    private String supportPhone = "89273272424";
    private String regState = "send";
    private String rejectReason = "";


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_state);
        this.isCheckProfileAuth = false;

        tvRegistrationStateSupportPhone = findViewById(R.id.tvRegistrationStateSupportPhone);
        tvRegistrationStateStatusName = findViewById(R.id.tvRegistrationStateStatusName);
        pbRegistrationState = findViewById(R.id.pbRegistrationState);
        ibRegistrationStateWhatsapp = findViewById(R.id.ibRegistrationStateWhatsapp);
        ibRegistrationStateTelegram = findViewById(R.id.ibRegistrationStateTelegram);
        btnRegistrationStateAgain = findViewById(R.id.btnRegistrationStateAgain);
        btnRegistrationStateDelete = findViewById(R.id.btnRegistrationStateDelete);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                JSONObject jsonData = new JSONObject(extras.getString("json_data", "{}"));
                supportPhone = JSONGetString(jsonData, "support_phone", "89273272424");
                regState = JSONGetString(jsonData, "reg_state", "send");
                rejectReason = JSONGetString(jsonData, "reject_reason");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        tvRegistrationStateSupportPhone.setText(MainUtils.convertPhoneToCall(supportPhone));
        tvRegistrationStateSupportPhone.setOnClickListener(view -> callIntent(supportPhone));
        ibRegistrationStateWhatsapp.setOnClickListener(view -> goToURL("https://wa.me/" + SupportContactItem.convertPhoneToWhatsApp(supportPhone)));
        ibRegistrationStateTelegram.setOnClickListener(view -> goToURL("https://t.me/" + SupportContactItem.convertPhoneToTelegram(supportPhone)));
        btnRegistrationStateAgain.setOnClickListener(view -> httpGetResult("/profile/registration/new", 1));

        btnRegistrationStateDelete.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegistrationStateActivity.this);
            alertDialog.setTitle("Внимание");
            alertDialog.setMessage("Все Ваши данные и фото документов будут удалены.");
            alertDialog.setPositiveButton("Удалить", (dialogInterface, i) -> httpGetResult("/profile/registration/delete", 2));
            alertDialog.setNegativeButton("Отмена", null);
            alertDialog.create();
            alertDialog.show();

        });


        if (regState.equals("send")){
            tvRegistrationStateStatusName.setText("Ваша анкета на проверке.\nПожалуйста ожидайте рассмотрения.");
            pbRegistrationState.setVisibility(View.VISIBLE);
            btnRegistrationStateAgain.setVisibility(View.GONE);
            btnRegistrationStateDelete.setVisibility(View.GONE);
        }
        else if (regState.equals("reject")){
            tvRegistrationStateStatusName.setText("Ваша анкета отклонена.\n" + rejectReason + ".");
            pbRegistrationState.setVisibility(View.GONE);
            btnRegistrationStateAgain.setVisibility(View.VISIBLE);
            btnRegistrationStateDelete.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onHttpGetResult(JSONObject data, int requestUID) {
        if (requestUID == 1){
            Intent intent = new Intent(RegistrationStateActivity.this, StartApplicationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        if (requestUID == 2){
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString("accountCode", "");
            editor.apply();
            Intent intent = new Intent(RegistrationStateActivity.this, StartApplicationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}