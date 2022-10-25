package org.toptaxi.taximeter.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.adapters.ListViewMessageAdapter;
import org.toptaxi.taximeter.data.Messages;
import org.toptaxi.taximeter.tools.Constants;
import org.toptaxi.taximeter.tools.LockOrientation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MessagesActivity extends AppCompatActivity implements AbsListView.OnScrollListener, Messages.OnMessagesListener {
    private static String TAG = "#########" + MessagesActivity.class.getName();
    ListViewMessageAdapter adapter;
    ListView listView;
    EditText edMessage;
    private View footer;
    LoadMoreAsyncTask loadMoreAsyncTask = new LoadMoreAsyncTask();
    boolean isFirst = true;
    RelativeLayout rlSendForm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Утановка текущего поворота экрана
        new LockOrientation(this).lock();
        setContentView(R.layout.activity_messages);
        listView = (ListView) findViewById(R.id.lvMessages);

        footer = getLayoutInflater().inflate(R.layout.item_messages_footer, null);
        listView.addHeaderView(footer);

        adapter = new ListViewMessageAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);

        loadMoreAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        edMessage = (EditText) findViewById(R.id.etMessagesMessage);
        edMessage.setSingleLine(true);

        edMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "onSendButtonClick");
                    btnSendMessageClick(null);
                }

                return false;
            }
        });

        rlSendForm = (RelativeLayout) findViewById(R.id.rlActivityMessagesSendForm);


    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount());
        MainApplication.getInstance().getMainMessages().setOnMessagesListener(this);

        // Если телефон диспетчера указан, то значит можно и сообщения отправлять, т.к. диспетчер есть
        if (!MainApplication.getInstance().getPreferences().getSupportPhone().equals(""))
            rlSendForm.setVisibility(View.VISIBLE);
        else {
            rlSendForm.setVisibility(View.GONE);
        }
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
        if ((MainApplication.getInstance().getMainMessages().getCount() < 10)) {
            listView.removeHeaderView(footer);
        } else {
            if ((firstVisibleItem == 0) && (loadMoreAsyncTask.getStatus() == AsyncTask.Status.FINISHED)) {
                //Log.d(TAG, "onScroll stra = " + firstVisibleItem);
                loadMoreAsyncTask = new LoadMoreAsyncTask();
                loadMoreAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void OnNewMessage() {
        adapter.notifyDataSetChanged();
        listView.setSelection(listView.getCount());
    }

    private class LoadMoreAsyncTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            if (isFirst) {
                isFirst = false;
                return -1;

            } else {
                Log.d(TAG, "doInBackground");
                return MainApplication.getInstance().getMainMessages().LoadMore();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute result = " + result);
            if (result == 0) {
                //footer.setVisibility(View.GONE);
                listView.removeHeaderView(footer);
            } else if (result > 0) {
                adapter.notifyDataSetChanged();
                listView.setSelection(result);
            }

        }
    }

    public void btnSendMessageClick(View view) {
        String Text = ((EditText) findViewById(R.id.etMessagesMessage)).getText().toString();
        if (!Text.trim().equals("")) {
            new SendMessageTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Text);
        }
    }

    private class SendMessageTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog progressDialog;

        SendMessageTask(Context mContext) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Передача данных ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            findViewById(R.id.btnMessagesSend).setEnabled(false);
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject result = null;
            try {
                result = MainApplication.getInstance().getRestService().httpGet("/last/messages/send?text=" + URLEncoder.encode(strings[0], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            MainApplication.getInstance().getMainMessages().LoadNew();
            return result;

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            adapter.notifyDataSetChanged();
            listView.setSelection(listView.getCount());

            findViewById(R.id.btnMessagesSend).setEnabled(true);
            edMessage.getText().clear();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow((findViewById(R.id.etMessagesMessage)).getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);


        }
    }

}
