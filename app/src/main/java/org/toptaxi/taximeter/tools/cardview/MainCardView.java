package org.toptaxi.taximeter.tools.cardview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import org.toptaxi.taximeter.R;

public class MainCardView extends MaterialCardView {

    private TextView mainTextView;
    private TextView noteTextView;
    private ImageView imageViewBegin;
    private String mainText;
    private String noteText;

    private IMainCardViewData mainCardViewData;
    private IMainCardViewClickListener mainCardViewClickListener;

    public MainCardView(@NonNull Context context) {
        super(context);
        initControl(context);
    }

    public MainCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initControl(context);
    }

    public MainCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context);
    }

    private void initControl(Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_view_main, this);
        mainTextView = findViewById(R.id.mainCardViewTextViewMain);
        noteTextView = findViewById(R.id.mainCardViewTextViewNote);
        imageViewBegin = findViewById(R.id.mainCardViewImageViewBegin);

        mainTextView.setVisibility(GONE);
        noteTextView.setVisibility(GONE);
        imageViewBegin.setVisibility(GONE);
    }

    private void initData(){
        mainTextView.setText(mainCardViewData.getMainText());
        mainTextView.setVisibility(VISIBLE);
        noteTextView.setText(mainCardViewData.getNoteText());
        noteTextView.setVisibility(VISIBLE);

        if (mainCardViewClickListener != null){
            findViewById(R.id.mainCardViewCard).setOnClickListener(v -> mainCardViewClickListener.clickItem(mainCardViewData));
        }
    }

    public void setData(IMainCardViewData mainCardViewData, IMainCardViewClickListener mainCardViewClickListener){
        this.mainCardViewData = mainCardViewData;
        this.mainCardViewClickListener = mainCardViewClickListener;
        initData();
    }

    public void setData(IMainCardViewData mainCardViewData) {
        this.mainCardViewData = mainCardViewData;
        initData();
    }

    public void setImage(int resourceID){
        imageViewBegin.setImageResource(resourceID);
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
        this.mainTextView.setText(mainText);
        mainTextView.setVisibility(VISIBLE);
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
        this.noteTextView.setText(noteText);
        noteTextView.setVisibility(VISIBLE);
    }

}
