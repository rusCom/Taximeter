package org.toptaxi.taximeter.tools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainAppCompatActivity extends AppCompatActivity {
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
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

    public void showSimpleDialog(String dialogText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogText);
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
