package org.toptaxi.taximeter.data;

import static org.toptaxi.taximeter.tools.MainUtils.JSONGetInteger;
import static org.toptaxi.taximeter.tools.MainUtils.JSONGetString;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class Message {
    public Integer ID;
    public Integer Status;
    public String Text;
    public String RegDate;
    public String Type;
    public Integer Route;


    public Message(JSONObject data) throws JSONException {
        ID = JSONGetInteger(data, "id");
        Status = JSONGetInteger(data, "status");
        Text = JSONGetString(data, "text");
        RegDate = JSONGetString(data, "vregdate");
        Type = JSONGetString(data, "type");
        Route = JSONGetInteger(data,"route");
    }
}
