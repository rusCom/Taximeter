package org.toptaxi.taximeter.activities.registration;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ernestoyaquello.com.verticalstepperform.Step;

public class RegistrationDialogStep extends Step<String> {
    private final String stepType;
    private EditText mainEditText;
    private final RegistrationMainActivity registrationMainActivity;
    private String curDataID = "";
    private String curDataValue = "";
    private JSONArray arrayData;

    protected RegistrationDialogStep(String stepType, RegistrationMainActivity photoStepsRegistrationActivity) {
        super(getTitle(stepType));
        this.stepType = stepType;
        this.registrationMainActivity = photoStepsRegistrationActivity;


        switch (stepType) {
            case "car_color_id" -> arrayData = photoStepsRegistrationActivity.carColors;
            case "car_brand_id" -> arrayData = photoStepsRegistrationActivity.carModels;
            case "car_model_id" -> {
                String carBrandID = photoStepsRegistrationActivity.getCarBrandStep().getStepData();
                if (!carBrandID.isEmpty()) {
                    arrayData = new JSONArray();
                    for (int itemID = 0; itemID < photoStepsRegistrationActivity.carModels.length(); itemID++) {
                        try {
                            JSONObject brandJSON = photoStepsRegistrationActivity.carModels.getJSONObject(itemID);
                            if (brandJSON.getString("id").equals(carBrandID)) {
                                if (brandJSON.has("models")) {
                                    arrayData = brandJSON.getJSONArray("models");
                                }
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            default -> arrayData = new JSONArray();
        }

    }

    private static String getTitle(String stepType) {
        return switch (stepType) {
            case "car_brand_id" -> "Марка автомобиля";
            case "car_model_id" -> "Модель автомобиля";
            case "car_color_id" -> "Цвет автомобиля";
            default -> stepType;
        };
    }

    @Override
    protected View createStepContentLayout() {
        mainEditText = new EditText(getContext());
        mainEditText.setSingleLine(true);
        mainEditText.setHint(getTitle(stepType));
        mainEditText.setClickable(true);
        mainEditText.setFocusable(false);
        mainEditText.setInputType(InputType.TYPE_NULL);
        mainEditText.setOnClickListener(view -> createDialog());
        if (curDataValue != null && !curDataValue.isEmpty()) {
            mainEditText.setText(curDataValue);
        }
        return mainEditText;

    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(registrationMainActivity);
        builder.setTitle("Выберите " + getTitle(stepType));
        String[] animals;
        try {
            animals = new String[arrayData.length()];
            for (int itemID = 0; itemID < arrayData.length(); itemID++) {
                JSONObject carColor = arrayData.getJSONObject(itemID);
                animals[itemID] = carColor.getString("name");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        builder.setItems(animals, (dialog, itemID) -> {
            try {
                JSONObject itemData = arrayData.getJSONObject(itemID);
                curDataID = itemData.getString("id");
                if (!curDataID.isEmpty()) {
                    curDataValue = itemData.getString("name");
                    mainEditText.setText(curDataValue);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            markAsCompletedOrUncompleted(true);
            if (!curDataID.isEmpty()) {
                registrationMainActivity.getVerticalStepperForm().goToStep(getPosition() + 1, true);
            }

        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public String getStepData() {
        return curDataID;

    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return curDataValue;
    }

    @Override
    protected void restoreStepData(String data) {
        if (!data.isEmpty()) {
            curDataID = data;
            if (stepType.equals("car_model_id")) {
                getBrandModels();
            }
            for (int itemID = 0; itemID < arrayData.length(); itemID++) {
                try {
                    JSONObject itemData = arrayData.getJSONObject(itemID);
                    if (itemData.getString("id").equals(curDataID)) {
                        curDataValue = itemData.getString("name");
                        if (mainEditText != null) {
                            mainEditText.setText(curDataValue);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        if (arrayData == null)return new IsDataValid(true);
        if (arrayData.length() == 0)return new IsDataValid(true);
        if (curDataID == null) return new IsDataValid(false);
        if (curDataID.isEmpty()) return new IsDataValid(false);
        return new IsDataValid(true);
    }


    @Override
    protected void onStepOpened(boolean animated) {
        if (stepType.equals("car_model_id")) {
            getBrandModels();
            if (arrayData.length() == 0){
                registrationMainActivity.getVerticalStepperForm().goToStep(getPosition() + 1, true);
                curDataID = "";
                curDataValue = "";
            }
        }

        if (registrationMainActivity != null) {
            InputMethodManager manager = (InputMethodManager) registrationMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(registrationMainActivity.findViewById(android.R.id.content).getWindowToken(), 0);
            }
        }
        if (arrayData.length() > 0 && curDataID.isEmpty()) {
            mainEditText.callOnClick();
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

    private void getBrandModels() {
        arrayData = new JSONArray();
        String carBrandID = registrationMainActivity.getCarBrandStep().getStepData();
        if (!carBrandID.isEmpty()) {
            for (int itemID = 0; itemID < registrationMainActivity.carModels.length(); itemID++) {
                try {
                    JSONObject brandJSON = registrationMainActivity.carModels.getJSONObject(itemID);
                    if (brandJSON.getString("id").equals(carBrandID)) {
                        if (brandJSON.has("models")) {
                            arrayData = brandJSON.getJSONArray("models");
                        }
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
