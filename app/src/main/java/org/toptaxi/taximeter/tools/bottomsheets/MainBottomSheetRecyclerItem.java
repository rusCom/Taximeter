package org.toptaxi.taximeter.tools.bottomsheets;

import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

public class MainBottomSheetRecyclerItem implements IMainCardViewData {
    private final String tag;
    private final String mainText;

    public MainBottomSheetRecyclerItem(String tag, String mainText) {
        this.mainText = mainText;
        this.tag = tag;
    }

    @Override
    public String getMainText() {
        return this.mainText;
    }

    @Override
    public String getTag() {
        return this.tag;
    }
}
