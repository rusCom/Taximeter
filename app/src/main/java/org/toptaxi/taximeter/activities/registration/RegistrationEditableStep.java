package org.toptaxi.taximeter.activities.registration;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import ernestoyaquello.com.verticalstepperform.Step;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;

public class RegistrationEditableStep extends Step<String> {
    private EditText mainEditText;
    private final String stepType;
    private String curData;
    private final VerticalStepperFormView stepperFormView;

    public RegistrationEditableStep(String stepType, VerticalStepperFormView stepperFormView) {
        super(getTitle(stepType));
        this.stepType = stepType;
        this.stepperFormView = stepperFormView;
    }

    private static String getTitle(String stepType) {
        return switch (stepType) {
            case "surname" -> "Фамилия";
            case "name" -> "Имя";
            case "patronymic" -> "Отчество (при наличии)";
            case "passport_number" -> "Номер паспорта РФ";
            case "drv_license_number" -> "Номер водительского удостоверения";
            case "car_gov_number" -> "ГосНомер автомобиля";
            case "car_year" -> "Год выпуска";
            default -> stepType;
        };
    }

    public void setData(String data) {
        this.curData = data;
        if (mainEditText != null) {
            mainEditText.setText(curData);
        }
    }

    @Override
    protected View createStepContentLayout() {

        // Here we generate the view that will be used by the library as the content of the step.
        // In this case we do it programmatically, but we could also do it by inflating an XML layout.
        mainEditText = new EditText(getContext());
        mainEditText.setSingleLine(true);
        mainEditText.setHint(getTitle(stepType));
        mainEditText.requestFocus();
        if (curData != null) {
            mainEditText.setText(curData);
        }
        if (stepType.equals("passport_number")) {
            mainEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (stepType.equals("car_year")) {
            mainEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (stepType.equals("drv_license_number")) {
            mainEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            mainEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }

        mainEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            curData = mainEditText.getText().toString();
            if (!curData.isEmpty()) {
                curData = curData.substring(0, 1).toUpperCase() + curData.substring(1).toLowerCase();
                mainEditText.setText(curData);
            }

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                markAsCompletedOrUncompleted(true);
                if (isValid()) {
                    stepperFormView.goToStep(getPosition() + 1, true);
                }
                return true;
            }
            return false;
        });

        mainEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Whenever the user updates the user name text, we update the state of the step.
                // The step will be marked as completed only if its data is valid, which will be
                // checked automatically by the form with a call to isStepDataValid().
                markAsCompletedOrUncompleted(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return mainEditText;
    }

    private boolean isValid() {
        switch (stepType) {
            case "name", "surname" -> {
                return getStepData().length() >= 1;
            }
            case "passport_number", "drv_license_number" -> {
                return getStepData().length() == 10;
            }
            case "car_gov_number" -> {
                String regex = "^[АВЕКМНОРСТУХABEKMHOPCTYX]{1}\\d{3}[АВЕКМНОРСТУХABEKMHOPCTYX]{2}\\d{2,3}$";
                String carNumber = "А123ВС45";
                return getStepData().toUpperCase().matches(regex);
            }
            case "car_year" -> {
                if (getStepData().length() != 4) return false;
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        if (isValid()) return new IsDataValid(true);
        String errorMessage = "Ошибка заполнения данных";
        if (getStepData().isEmpty()){errorMessage = "";}
        switch (stepType) {
            case "name", "surname" -> {
                errorMessage = "Не может быть пустым";
            }
            case "passport_number" -> {
                errorMessage = "Номер паспорта должен состоят из 10 цифр";
            }
            case "drv_license_number" ->{
                errorMessage = "Номер водительського удостоверния должен состоять из 10 цфир";
            }
            case "car_gov_number" -> {
                errorMessage = "Проверьте правильность ввода данных";
            }

        }

        return new IsDataValid(false, errorMessage);
    }

    @Override
    public String getStepData() {
        // We get the step's data from the value that the user has typed in the EditText view.
        String data = "";

        if (mainEditText != null && mainEditText.getText() != null)
            data = mainEditText.getText().toString();
        if (data.isEmpty() && curData != null && !curData.isEmpty()) {
            data = curData;
        }
        data = data.trim();
        return data;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        // Because the step's data is already a human-readable string, we don't need to convert it.
        // However, we return "(Empty)" if the text is empty to avoid not having any text to display.
        // This string will be displayed in the subtitle of the step whenever the step gets closed.
        if (curData != null && !curData.isEmpty()) return curData;
        String userName = getStepData();
        return !userName.isEmpty() ? userName : "Не заполнено";
    }

    @Override
    protected void onStepOpened(boolean animated) {
        // This will be called automatically whenever the step gets opened.
        mainEditText.requestFocus();
    }

    @Override
    protected void onStepClosed(boolean animated) {
        // This will be called automatically whenever the step gets closed.
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {
        // This will be called automatically whenever the step is marked as completed.
    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {
        // This will be called automatically whenever the step is marked as uncompleted.
    }

    @Override
    protected void restoreStepData(String stepData) {
        // To restore the step after a configuration change, we restore the text of its EditText view.
        mainEditText.setText(stepData);
    }
}
