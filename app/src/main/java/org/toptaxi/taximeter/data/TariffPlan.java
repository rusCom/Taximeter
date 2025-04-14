package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import org.json.JSONObject;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

public class TariffPlan implements IMainCardViewData {
    private final Integer ID;
    public final String name;
    public final Integer cost;
    public final String note;

    public TariffPlan(JSONObject data) {
        this.ID = JSONGetInteger(data, "id");
        this.name = JSONGetString(data, "name");
        this.cost = JSONGetInteger(data, "cost");
        this.note = JSONGetString(data, "note");
    }

    public Integer getID() {
        return ID;
    }

    @Override
    public String getMainText() {
        return name;
    }

    @Override
    public String getNoteText() {
        return note;
    }
}
