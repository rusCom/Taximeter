package org.toptaxi.taximeter.activities.registration;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.StartApplicationActivity;
import org.toptaxi.taximeter.data.SupportContactItem;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.bottomsheets.MainBottomSheetRecycler;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ernestoyaquello.com.verticalstepperform.Step;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;


public class RegistrationMainActivity extends MainAppCompatActivity implements StepperFormListener {
    private VerticalStepperFormView verticalStepperForm;
    JSONObject jsonData;
    JSONArray carColors;
    JSONArray carModels;
    JSONObject profileData;
    private RegistrationEditableStep surnameStep;
    private RegistrationEditableStep nameStep;
    private RegistrationEditableStep patronymicStep;
    private RegistrationEditableStep passportNumberStep;
    private RegistrationEditableStep drvLicenseNumberStep;
    private RegistrationDialogStep carColorStep;
    private RegistrationDialogStep carBrandStep;
    private RegistrationDialogStep carModelStep;
    private RegistrationEditableStep carGovNumberStep;
    private RegistrationEditableStep carYearStep;
    private PhotoCaptureStep passportMainStep;
    private PhotoCaptureStep passportRegistrationStep;
    private PhotoCaptureStep drvLicenseNumberMainStep;
    private PhotoCaptureStep drvLicenseNumberReverseStep;
    private PhotoCaptureStep carRegistrationMainStep;
    private PhotoCaptureStep carRegistrationReverseStep;
    private PhotoCaptureStep selfStep;
    private String supportPhone = "89273272424";


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_registration);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.isCheckProfileAuth = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                jsonData = new JSONObject(extras.getString("json_data", "{}"));
                profileData = jsonData.getJSONObject("profile");
                carColors = jsonData.getJSONArray("car_colors");
                carModels = jsonData.getJSONArray("car_models");
                supportPhone = JSONGetString(jsonData, "support_phone", "89273272424");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        if (jsonData == null) {
            jsonData = new JSONObject();
            profileData = new JSONObject();
            carColors = new JSONArray();
            carModels = new JSONArray();
        }


        // Finding the view
        verticalStepperForm = findViewById(R.id.vertical_stepper_form);

        surnameStep = new RegistrationEditableStep("surname", verticalStepperForm);
        nameStep = new RegistrationEditableStep("name", verticalStepperForm);
        patronymicStep = new RegistrationEditableStep("patronymic", verticalStepperForm);
        passportNumberStep = new RegistrationEditableStep("passport_number", verticalStepperForm);
        drvLicenseNumberStep = new RegistrationEditableStep("drv_license_number", verticalStepperForm);
        carGovNumberStep = new RegistrationEditableStep("car_gov_number", verticalStepperForm);
        carYearStep = new RegistrationEditableStep("car_year", verticalStepperForm);

        carColorStep = new RegistrationDialogStep("car_color_id", this);
        carBrandStep = new RegistrationDialogStep("car_brand_id", this);
        carModelStep = new RegistrationDialogStep("car_model_id", this);

        surnameStep.setData(JSONGetString(profileData, "surname"));
        nameStep.setData(JSONGetString(profileData, "name"));
        patronymicStep.setData(JSONGetString(profileData, "patronymic"));
        passportNumberStep.setData(JSONGetString(profileData, "passport_number"));
        drvLicenseNumberStep.setData(JSONGetString(profileData, "drv_license_number"));
        carGovNumberStep.setData(JSONGetString(profileData, "car_gov_number"));
        carYearStep.setData(JSONGetString(profileData, "car_year"));

        carColorStep.restoreStepData(JSONGetString(profileData, "car_color_id"));
        carBrandStep.restoreStepData(JSONGetString(profileData, "car_brand_id"));
        carModelStep.restoreStepData(JSONGetString(profileData, "car_model_id"));

        passportMainStep = new PhotoCaptureStep("passport_main", this);
        passportRegistrationStep = new PhotoCaptureStep("passport_registration", this);
        drvLicenseNumberMainStep = new PhotoCaptureStep("drv_license_number_main", this);
        drvLicenseNumberReverseStep = new PhotoCaptureStep("drv_license_number_reverse", this);
        carRegistrationMainStep = new PhotoCaptureStep("сar_registration_main", this);
        carRegistrationReverseStep = new PhotoCaptureStep("сar_registration_reverse", this);
        selfStep = new PhotoCaptureStep("self", this);

        verticalStepperForm
                .setup(this,
                        surnameStep,
                        nameStep,
                        patronymicStep,
                        passportNumberStep,
                        drvLicenseNumberStep,
                        carColorStep,
                        carBrandStep,
                        carModelStep,
                        carGovNumberStep,
                        carYearStep,
                        passportMainStep,
                        passportRegistrationStep,
                        drvLicenseNumberMainStep,
                        drvLicenseNumberReverseStep,
                        carRegistrationMainStep,
                        carRegistrationReverseStep,
                        selfStep)
                .stepNextButtonText("Далее...")
                .confirmationStepTitle("Отправить анкету")
                .lastStepNextButtonText("Отправить анкету на проверку")
                .displayStepButtons(false)
                .init();
    }

    public VerticalStepperFormView getVerticalStepperForm() {
        return verticalStepperForm;
    }

    public RegistrationDialogStep getCarBrandStep() {
        return carBrandStep;
    }

    @Override
    protected void onDestroy() {

        sendData(false);
        super.onDestroy();
    }

    private void sendData(boolean isCheckResult) {
        try {
            profileData.put("surname", surnameStep.getStepData());
            profileData.put("name", nameStep.getStepData());
            profileData.put("patronymic", patronymicStep.getStepData());
            profileData.put("passport_number", passportNumberStep.getStepData());
            profileData.put("drv_license_number", drvLicenseNumberStep.getStepData());
            profileData.put("car_color_id", carColorStep.getStepData());
            profileData.put("car_brand_id", carBrandStep.getStepData());
            profileData.put("car_model_id", carModelStep.getStepData());
            profileData.put("car_gov_number", carGovNumberStep.getStepData());
            profileData.put("car_year", carYearStep.getStepData());
            if (!isCheckResult) {
                MainApplication.getInstance().getRestService().httpPostThread("/profile/set", profileData);
            } else {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    runOnUiThread(this::showProgressDialog);
                    MainApplication.getInstance().getRestService().httpPost("/profile/set", profileData);

                    if (passportMainStep.isValid()) {
                        MainApplication.getInstance().getRestService().sendFile("/profile/document/send", "passport_main", passportMainStep.getFileName());
                    }
                    if (passportRegistrationStep.isValid()) {
                        MainApplication.getInstance().getRestService().sendFile("/profile/document/send", "passport_registration", passportRegistrationStep.getFileName());
                    }
                    if (drvLicenseNumberMainStep.isValid()) {
                        MainApplication.getInstance().getRestService().sendFile("/profile/document/send", "drv_license_number_main", drvLicenseNumberMainStep.getFileName());
                    }
                    if (drvLicenseNumberReverseStep.isValid()) {
                        MainApplication.getInstance().getRestService().sendFile("/profile/document/send", "drv_license_number_reverse", drvLicenseNumberReverseStep.getFileName());
                    }
                    if (carRegistrationMainStep.isValid()) {
                        MainApplication.getInstance().getRestService().sendFile("/profile/document/send", "car_registration_main", carRegistrationMainStep.getFileName());
                    }
                    if (carRegistrationReverseStep.isValid()) {
                        MainApplication.getInstance().getRestService().sendFile("/profile/document/send", "car_registration_reverse", carRegistrationReverseStep.getFileName());
                    }
                    if (selfStep.isValid()) {
                        MainApplication.getInstance().getRestService().sendFile("/profile/document/send", "self", selfStep.getFileName());
                    }

                    JSONObject result = MainApplication.getInstance().getRestService().httpGet("/profile/registration/send");
                    runOnUiThread(this::dismissProgressDialog);

                    if (JSONGetString(result, "status").equals("OK")) {
                        Intent intent = new Intent(RegistrationMainActivity.this, StartApplicationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                });
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCompletedForm() {
        sendData(true);
    }

    @Override
    public void onCancelledForm() {

    }

    @Override
    public void onStepAdded(int index, Step<?> addedStep) {

    }

    @Override
    public void onStepRemoved(int index) {

    }

    public void onMenuSupportClick(MenuItem item) {
        LogService.getInstance().log("sys", "onMenuClick");
        List<SupportContactItem> supportContactItemList = new ArrayList<>();
        supportContactItemList.add(new SupportContactItem("phone"));
        supportContactItemList.add(new SupportContactItem("whatsapp"));
        supportContactItemList.add(new SupportContactItem("telegram"));
        ArrayList<IMainCardViewData> cards = new ArrayList<>(supportContactItemList);
        MainBottomSheetRecycler myBottomSheetFragment = new MainBottomSheetRecycler(
                cards,
                mainCardViewData -> {
                    SupportContactItem supportContactItem = (SupportContactItem) mainCardViewData;
                    supportContactItem.onClick(this, supportPhone);
                }
        );
        myBottomSheetFragment.show(getSupportFragmentManager(), myBottomSheetFragment.getTag());
    }
}