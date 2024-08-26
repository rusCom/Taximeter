package org.toptaxi.taximeter.data;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.MessagesActivity;
import org.toptaxi.taximeter.activities.NewOrderActivity;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.DBHelper;
import org.toptaxi.taximeter.tools.MainUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

public class Messages {
    protected static String TAG = "#########" + Messages.class.getName();
    private final SQLiteDatabase dataBase;
    private final TreeSet<Message> messages;
    private OnMessagesListener onMessagesListener;
    MediaPlayer mediaPlayerIncomingMessage;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private final Map<Long, String> sentTemplateMessages = new HashMap<>();

    public interface OnMessagesListener {
        void OnNewMessage();
    }

    public Messages() {
        DBHelper dbHelper = new DBHelper(MainApplication.getInstance());
        dataBase = dbHelper.getWritableDatabase();
        messages = new TreeSet<>(new MessageComp());
        mediaPlayerIncomingMessage = MediaPlayer.create(MainApplication.getInstance(), R.raw.incomming_message);
        mediaPlayerIncomingMessage.setLooping(false);
    }

    public void OnNewMessages(JSONArray jsonMessages) throws JSONException {
        for (int itemID = 0; itemID < jsonMessages.length(); itemID++) {
            JSONObject jsonMessage = jsonMessages.getJSONObject(itemID);
            final Message message = new Message(jsonMessage);
            LogService.getInstance().log(this, message.Text);
            if ((message.Type.equals("disp")) || (message.Type.equals("info"))) {
                messages.add(message);
            }
            MainApplication.getInstance().getRestService().httpGetThread("/messages/delivered?message_id=" + message.ID);
            if (isNewMessage(message)) {
                if (message.Type.equals("new_order")) {
                    JSONArray dataArray = new JSONArray(message.Text);
                    Order newOrder = new Order(dataArray.getJSONObject(0));
                    MainApplication.getInstance().setNewOrder(newOrder);
                    Intent dialogIntent = new Intent(MainApplication.getInstance(), NewOrderActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApplication.getInstance().startActivity(dialogIntent);
                } // if (message.Type.equals("new_order")){
                else if (onMessagesListener != null) { // открыто окно с сообщениями
                    onMessagesListener.OnNewMessage();
                } else if (MainApplication.getInstance().getMainActivity() != null) { // открыто основное окно программы
                    MainApplication.getInstance().getMainActivity().runOnUiThread(() -> {
                        mediaPlayerIncomingMessage.start();
                        AlertDialog.Builder adb = new AlertDialog.Builder(MainApplication.getInstance().getMainActivity());
                        adb.setMessage(message.Text);
                        adb.setIcon(android.R.drawable.ic_dialog_info);
                        adb.setPositiveButton("Ok", (dialogInterface, i) -> setRead(message.ID));
                        if (message.Type.equals("disp")) {
                            adb.setTitle("Сообщение от диспетчера");
                            adb.setNegativeButton("Ответить", (dialogInterface, i) -> {
                                setRead(message.ID);
                                Intent messagesIntent = new Intent(MainApplication.getInstance().getMainActivity(), MessagesActivity.class);
                                MainApplication.getInstance().getMainActivity().startActivity(messagesIntent);
                            });
                        }
                        adb.create();
                        adb.show();
                    });
                } else {
                    mediaPlayerIncomingMessage.start();
                }
            }
        }
    }

    public void setOnMessagesListener(OnMessagesListener onMessagesListener) {
        this.onMessagesListener = onMessagesListener;
    }

    private boolean isNewMessage(Message message) {
        boolean result = false;
        Cursor cursor = dataBase.rawQuery("select * from messages where id = ? ", new String[]{String.valueOf(message.ID)});
        if (cursor.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put("id", message.ID);
            dataBase.insert("messages", null, cv);
            result = true;
        }
        return result;
    }

    public void setFromJSON(JSONArray data) throws JSONException {
        LogService.getInstance().log("Messages", data.toString());
        for (int itemID = 0; itemID < data.length(); itemID++) {
            Message message = new Message(data.getJSONObject(itemID));
            messages.add(message);
        }
    }

    public Message getItem(int position) {
        return (Message) (messages.toArray())[position];
    }

    public int getCount() {
        return messages.size();
    }

    public void setRead(int MessageID) {
        MainApplication.getInstance().getRestService().httpGetThread("/messages/read?message_id=" + MessageID);
    }

    public int getLastID() {
        int result = 0;
        Iterator<Message> iterator = messages.iterator();
        if (iterator.hasNext()) {
            result = iterator.next().ID;
            while (iterator.hasNext()) {
                int messageID = iterator.next().ID;
                if (messageID < result)
                    result = messageID;
            }
        }
        return result;
    }

    public int LoadMore() {
        int resultCount = 0;
        try {
            JSONObject response = MainApplication.getInstance().getRestService().httpGet("/messages?last_id=" + getLastID());
            if (response.getString("status").equals("OK")) {
                JSONArray result = response.getJSONArray("result");
                setFromJSON(result);
                resultCount = result.length();
            }
        } catch (JSONException ignored) {
        }
        return resultCount;
    }

    public void LoadNew() {
        try {
            JSONObject response = MainApplication.getInstance().getRestService().httpGet("/messages");
            if (response.getString("status").equals("OK")) {
                setFromJSON(response.getJSONArray("result"));
            }
        } catch (JSONException ignored) {
        }
    }

    public Integer checkSendingTemplateMessage(String message){
        int result = 0;
        for (Map.Entry<Long, String> pair: sentTemplateMessages.entrySet()) {
            if (pair.getValue().equals(message)){
                if (MainUtils.passedTimeSek(pair.getKey()) < MainApplication.getInstance().getPreferences().systemTemplateMessagesTimeout){
                    result = MainUtils.passedTimeSek(pair.getKey());
                }
            }
        }
        if (result == 0){
            sentTemplateMessages.put(System.currentTimeMillis(), message);
        }
        return result;
    }

}
