package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import org.json.JSONObject;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

public class TariffPlan implements IMainCardViewData {
    public Integer ID;
    public String Name;
    public Integer Cost;
    public Integer lastID;
    public String note;

    public TariffPlan(JSONObject data) {
        this.ID = JSONGetInteger(data, "id");
        this.Name = JSONGetString(data, "name");
        this.Cost = JSONGetInteger(data, "cost");
        this.lastID = JSONGetInteger(data, "last_id");
        note = JSONGetString(data, "note");
    }

    @Override
    public String getMainText() {
        return Name;
    }

    @Override
    public String getNoteText() {
        return note;
    }
}
