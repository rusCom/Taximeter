package org.toptaxi.taximeter.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.data.UnlimitedTariffPlan;

public class OnMainActionUnlimClickListener implements AdapterView.OnItemClickListener {
    protected static String TAG = "#########" + OnMainActionUnlimClickListener.class.getName();
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Context context = view.getContext();
        TextView textViewItem = ((TextView) view.findViewById(R.id.tvMainActionUnlimTitle));
        final UnlimitedTariffPlan unlimTariff = (UnlimitedTariffPlan)textViewItem.getTag();
        Log.d(TAG,  "onItemClick " + unlimTariff.Name);


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
        alertDialog.setTitle("Внимание");
        alertDialog.setMessage("Активировать безлимит: " + unlimTariff.Name);
        alertDialog.setPositiveButton("Да", (dialogInterface, i1) -> {
            MainApplication.getInstance().getRestService().httpGetResult("/last/unlimited_tariff/activate?tariff_id=" + unlimTariff.lastID);
            MainApplication.getInstance().getMainActivity().mainActionsUnlimDialog.cancel();
        });
        alertDialog.setNegativeButton("Нет" , null);
        alertDialog.create();
        alertDialog.show();
    }
}
