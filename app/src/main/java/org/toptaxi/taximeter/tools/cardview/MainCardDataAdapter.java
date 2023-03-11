package org.toptaxi.taximeter.tools.cardview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.toptaxi.taximeter.R;

import java.util.List;

public class MainCardDataAdapter extends RecyclerView.Adapter<MainCardDataAdapter.MainCardDataViewHolder>{

    private final List<IMainCardViewData> listItems;
    private final IMainCardViewClickListener clickListener;

    public MainCardDataAdapter(List<IMainCardViewData> listItems, IMainCardViewClickListener clickListener) {
        this.listItems = listItems;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MainCardDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_dialog_recycler_view_element, parent, false);
        return new MainCardDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainCardDataViewHolder holder, int position) {
        IMainCardViewData mainCardViewData = listItems.get(position);
        if (mainCardViewData == null){
            return;
        }
        holder.mainCardView.setData(mainCardViewData, clickListener);
    }

    @Override
    public int getItemCount() {
        if (listItems != null){
            return listItems.size();
        }
        return 0;
    }

    public static class MainCardDataViewHolder extends RecyclerView.ViewHolder{
        private final MainCardView mainCardView;

        public MainCardDataViewHolder(@NonNull View itemView) {
            super(itemView);
            mainCardView = itemView.findViewById(R.id.sys_bottom_dialog_recycler_item);
        }
    }
}
