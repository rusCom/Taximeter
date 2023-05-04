package org.toptaxi.taximeter.tools.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.cardview.MainCardDataAdapter;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewClickListener;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

import java.util.List;

public class MainBottomSheetRecycler extends BottomSheetDialogFragment {
    private final List<IMainCardViewData> listItems;
    private final IMainCardViewClickListener clickListener;

    public MainBottomSheetRecycler(List<IMainCardViewData> listItems, IMainCardViewClickListener clickListener) {
        this.listItems = listItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_dialog_recycler_view, null);
        bottomSheetDialog.setContentView(view);

        RecyclerView recyclerView = view.findViewById(R.id.bottom_dialog_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        MainCardDataAdapter tariffPlansAdapter = new MainCardDataAdapter(listItems,
                mainCardViewData -> {
                    bottomSheetDialog.dismiss();
                    if (clickListener != null) {
                        clickListener.clickItem(mainCardViewData);
                    }
                });

        recyclerView.setAdapter(tariffPlansAdapter);

        if (getContext() != null){
            RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(itemDecoration);
        }

        return bottomSheetDialog;
    }
}