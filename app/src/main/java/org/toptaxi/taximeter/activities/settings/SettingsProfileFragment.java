package org.toptaxi.taximeter.activities.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.StartApplicationActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsProfileFragment extends Fragment {
    ProgressDialog progressDialog;

    public SettingsProfileFragment() {
        // Required empty public constructor
    }

    public static SettingsProfileFragment mewInstance() {
        SettingsProfileFragment fragment = new SettingsProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_profile, container, false);
        view.findViewById(R.id.btnChangeProfileButton).setOnClickListener(view1 -> onChangeProfileClick());
        return view;
    }

    public void onChangeProfileClick() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getContext());
        alertDialog.setCancelable(true);
        alertDialog.setMessage("После подтверждения выхода все данные не сохранятся и необходимо будет пройти повторную авторизацию");
        alertDialog.setNegativeButton("Отмена", (dialogInterface, i) -> {
        });

        alertDialog.setPositiveButton("Подтвердить выход", (dialogInterface, i) -> {
            /*


            */

            closeProfile();


        });
        alertDialog.create();
        alertDialog.show();
    }

    public void closeProfile(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            this.getActivity().runOnUiThread(this::showLoadingDialog);
            MainApplication.getInstance().stopMainService();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {

            }
            MainApplication.getInstance().getFirebaseService().clearData();
            MainApplication.getInstance().getMainAccount().setToken("");
            MainApplication.getInstance().getRestService().reloadHeader();

            this.getActivity().runOnUiThread(this::dismissLoadingDialog);
            Intent intent = new Intent(this.getContext(), StartApplicationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    public void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this.getContext());
            progressDialog.setMessage("Обработка данных ...");
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
}
