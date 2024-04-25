package org.toptaxi.taximeter.activities;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.ListViewMessageAdapter;
import org.toptaxi.taximeter.data.Messages;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessagesActivity extends MainAppCompatActivity implements AbsListView.OnScrollListener, Messages.OnMessagesListener {
    ListViewMessageAdapter adapter;
    ListView listView;
    EditText edMessage;
    private View footer;
    RelativeLayout rlSendForm;
    MediaPlayer mediaPlayerNewMessage;
    Boolean isLoadData = false;
    Boolean isCreateActivity = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Чат с диспетчером");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listView = findViewById(R.id.lvMessages);

        footer = getLayoutInflater().inflate(R.layout.item_messages_footer, null);
        listView.addHeaderView(footer);

        adapter = new ListViewMessageAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);


        LogService.getInstance().log("Messages", "onCreate");
        updateDataAsync();

        mediaPlayerNewMessage = MediaPlayer.create(this, R.raw.incomming_message_frg);
        mediaPlayerNewMessage.setLooping(false);

        edMessage = findViewById(R.id.etMessagesMessage);
        edMessage.setSingleLine(true);

        edMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                LogService.getInstance().log("Messages", "onSendButtonClick");
                btnSendMessageClick(null);
            }

            return false;
        });

        rlSendForm = findViewById(R.id.rlActivityMessagesSendForm);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLoadData = true;


        MainApplication.getInstance().getMainMessages().setOnMessagesListener(this);

        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount());

        // listView.post(() -> listView.setSelection(listView.getCount() - 1));

        // Если телефон диспетчера указан, то значит можно и сообщения отправлять, т.к. диспетчер есть
        if (!MainApplication.getInstance().getPreferences().getSupportPhone().isEmpty())
            rlSendForm.setVisibility(View.VISIBLE);
        else {
            rlSendForm.setVisibility(View.GONE);
        }
        isLoadData = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.getInstance().getMainMessages().setOnMessagesListener(null);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if ((firstVisibleItem == 0) && (!isLoadData)) {
            LogService.getInstance().log("Messages", "onScroll");
            updateDataAsync();
        }
    }

    @Override
    public void OnNewMessage() {
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            listView.setSelection(listView.getCount());
            mediaPlayerNewMessage.start();
        });
    }

    void updateDataAsync() {
        if (isLoadData) return;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            if (isLoadData) return;
            isLoadData = true;
            LogService.getInstance().log("Messages", "updateDataAsync start thread");

            // final ArrayList<Payment> result = adapter.LoadMore();
            int result = MainApplication.getInstance().getMainMessages().LoadMore();
            LogService.getInstance().log("Messages", "updateDataAsync stop load more result = " + result);
            runOnUiThread(() -> {
                LogService.getInstance().log("Messages", "updateDataAsync runOnUiThread start result = " + result);
                if (result == 0) {
                    listView.removeHeaderView(footer);
                } else if (result > 0) {
                    adapter.notifyDataSetChanged();
                    LogService.getInstance().log("Messages", "updateDataAsync runOnUiThread isCreateActivity = " + isCreateActivity);
                    if (isCreateActivity) {
                        listView.setSelection(listView.getCount());
                        isCreateActivity = false;
                    } else {
                        listView.setSelection(result);
                    }
                }
                LogService.getInstance().log("Messages", "updateDataAsync runOnUiThread stop");
                isLoadData = false;
            });
        });
    }


    public void btnSendMessageClick(View view) {
        String Text = ((EditText) findViewById(R.id.etMessagesMessage)).getText().toString();
        if (!MainUtils.isEmptyString(Text)) {
            sendMessageAsync(Text);
        }
    }

    void sendMessageAsync(String message) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            LogService.getInstance().log("Messages", "sendMessageAsync start thread");
            runOnUiThread(this::showProgressDialog);
            isLoadData = true;
            try {
                MainApplication.getInstance().getRestService().httpGet("/messages/send?message=" + URLEncoder.encode(message, "UTF-8"));
            } catch (UnsupportedEncodingException ignored) {
            }

            MainApplication.getInstance().getMainMessages().LoadNew();
            LogService.getInstance().log("Messages", "sendMessageAsync stop load more result");
            runOnUiThread(() -> {
                LogService.getInstance().log("Messages", "sendMessageAsync runOnUiThread start");
                adapter.notifyDataSetChanged();
                listView.setSelection(listView.getCount());

                findViewById(R.id.btnMessagesSend).setEnabled(true);
                edMessage.getText().clear();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow((findViewById(R.id.etMessagesMessage)).getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                LogService.getInstance().log("Messages", "sendMessageAsync runOnUiThread start");
                isLoadData = false;
                dismissProgressDialog();
            });
        });
    }
}
