package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetDouble;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.services.LogService;

import java.text.DecimalFormat;

public class Payment {
    public String ID, Name, Note, Date;
    public Double Summa, Balance;

    public Payment(JSONObject data) throws JSONException {
        ID = JSONGetString(data, "id");
        Summa = JSONGetDouble(data, "summa");
        Date = JSONGetString(data, "date");
        Name = JSONGetString(data, "name");
        Note = JSONGetString(data, "note");
        Balance = JSONGetDouble(data, "balance");
        // LogService.getInstance().log(this, data.toString());
    }

    public String getSummaString() {
        return new DecimalFormat("###,#00.00").format(Summa);
    }

    public String getBalanceString() {
        return new DecimalFormat("###,#00.00").format(Balance);
    }


}
