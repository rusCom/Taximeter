package org.toptaxi.taximeter.activities.registration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;

import org.toptaxi.taximeter.activities.CheckPhotoActivity;

import java.io.File;

import ernestoyaquello.com.verticalstepperform.Step;

public class PhotoCaptureStep extends Step<String> {
    private final String captureType;
    private String fileName;
    MaterialButton materialButton;
    ImageView imageView;

    ActivityResultLauncher<Intent> mStartForResult;

    public PhotoCaptureStep(String captureType, RegistrationMainActivity appCompatActivity) {
        super(getCaptureTitle(captureType));
        this.captureType = captureType;
        mStartForResult = appCompatActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent != null) {
                            fileName = intent.getStringExtra("filename");
                            if (fileName != null && !fileName.isEmpty()) {
                                markAsCompletedOrUncompleted(true);
                                appCompatActivity.getVerticalStepperForm().goToStep(getPosition() + 1, true);
                            }
                        }
                    } else {
                        markAsCompletedOrUncompleted(true);
                    }
                });
    }

    public static String getCaptureTitle(String captureType) {
        return switch (captureType) {
            case "passport_main" -> "Паспорт. Основной разворот";
            case "passport_registration" -> "Паспорт. Регистрация (прописка)";
            case "drv_license_number_main" -> "Водительское удостоверение";
            case "drv_license_number_reverse" -> "Водительское удостоверение (Обратная сторона)";
            case "сar_registration_main" -> "Свидетельство о  регистрации ТС";
            case "сar_registration_reverse" -> "Свидетельство о  регистрации ТС (Обратная сторона)";
            case "self" -> "Селфи";
            default -> captureType;
        };
    }

    @Override
    protected View createStepContentLayout() {
        CoordinatorLayout coordinatorLayout = new CoordinatorLayout(getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;


        materialButton = new MaterialButton(getContext());
        materialButton.setLayoutParams(params);
        materialButton.setText("Сделать Фото");
        materialButton.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), CheckPhotoActivity.class);
            intent.putExtra("capture_title", getCaptureTitle(captureType));
            intent.putExtra("capture_type", captureType);
            mStartForResult.launch(intent);
        });

        imageView = new ImageView(getContext());
        imageView.setLayoutParams(params);

        coordinatorLayout.addView(materialButton);
        coordinatorLayout.addView(imageView);

        return coordinatorLayout;
    }

    @Override
    public String getStepData() {
        return null;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return null;
    }

    @Override
    protected void restoreStepData(String data) {

    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        return new IsDataValid(isValid());
    }

    public boolean isValid() {
        if (fileName == null) return false;
        if (fileName.isEmpty()) return false;
        return true;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    protected void onStepOpened(boolean animated) {
        if (fileName == null) materialButton.performClick();
        else if (fileName.isEmpty()) materialButton.performClick();
        else {
            File imgFile = new File(getFileName());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        }

    }

    @Override
    protected void onStepClosed(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }
}
