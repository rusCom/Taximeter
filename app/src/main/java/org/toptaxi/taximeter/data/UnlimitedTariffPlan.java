package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import org.json.JSONObject;

public class UnlimitedTariffPlan {
    Integer ID;
    public String Name;
    Integer Cost;
    public Integer lastID;

    public UnlimitedTariffPlan(JSONObject data) {
        this.ID = JSONGetInteger(data, "id");
        this.Name = JSONGetString(data, "name");
        this.Cost = JSONGetInteger(data, "cost");
        this.lastID = JSONGetInteger(data, "last_id");
    }
}
