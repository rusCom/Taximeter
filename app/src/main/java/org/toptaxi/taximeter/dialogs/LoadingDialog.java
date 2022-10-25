package org.toptaxi.taximeter.dialogs;

import android.app.AlertDialog;
import android.content.Context;

import org.toptaxi.taximeter.R;

public class LoadingDialog {
    private AlertDialog dialog;

    public LoadingDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_loading);
        dialog = builder.create();
    }

    public void show(){
        dialog.show();
    }

    public void dismiss(){
        dialog.dismiss();
    }
}
