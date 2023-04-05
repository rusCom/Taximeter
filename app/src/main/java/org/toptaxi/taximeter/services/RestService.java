package org.toptaxi.taximeter.services;

import android.content.Context;
import android.os.Build;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestService {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient;
    private JSONObject header;
    private final ArrayList<String> restHost;
    private int restIndex;


    public RestService(Context context) {
        httpClient = new OkHttpClient();
        header = new JSONObject();
        reloadHeader();
        restHost = new ArrayList<>();
        restHost.add(MainApplication.getInstance().getResources().getString(R.string.mainRestHost));
        restHost.add(MainApplication.getInstance().getResources().getString(R.string.reserveRestHost));
        restIndex = 0;
    }

    public String getRestHost(){
        return restHost.get(0);
    }

    public void setRestHost(JSONArray hosts) {
        LogService.getInstance().log(this, "setRestHost", hosts.toString());
        restHost.clear();
        for (int itemID = 0; itemID < hosts.length(); itemID++) {
            try {
                restHost.add(hosts.getString(itemID));
            } catch (JSONException ignored) {
            }
        }

    }

    public void reloadHeader() {
        header = new JSONObject();
        try {
            header.put("token", MainApplication.getInstance().getMainAccount().getToken());
            header.put("version", MainApplication.getInstance().getAppVersion());
            header.put("os_name", "android");
            header.put("os_release", Build.VERSION.RELEASE);
            header.put("os_sdk", Build.VERSION.SDK_INT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getHeader() {

        try {
            header.put("location", MainApplication.getInstance().getLocationService().toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(header.toString().getBytes());
        } else {
            return android.util.Base64.encodeToString(header.toString().getBytes(), android.util.Base64.NO_WRAP);
        }

    }

    public void httpGetResult(final String path) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            if (MainApplication.getInstance().getMainActivity() != null) {
                MainApplication.getInstance().getMainActivity().runOnUiThread(() -> MainApplication.getInstance().getMainActivity().showProgressDialog());
            }

            JSONObject request = httpGet(path);

            try {
                if (request.getString("status").equals("OK")) {
                    if (request.has("result")) {
                        MainApplication.getInstance().parseData(request.getJSONObject("result"));
                    }
                } else {
                    if (request.has("result")) {
                        MainApplication.getInstance().showToast(request.getString("result"));
                    } else {
                        MainApplication.getInstance().showToast(request.toString());
                    }

                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }

            if (MainApplication.getInstance().getMainActivity() != null) {
                MainApplication.getInstance().getMainActivity().runOnUiThread(() -> MainApplication.getInstance().getMainActivity().dismissProgressDialog());
            }
        });
    }

    public void serverError(String method, String data){
        httpGetThread("/server_error?method=" + method + "&data=" + data);
    }


    public void httpGetThread(final String path) {
        new Thread(() -> httpGetHost(path)).start();
    }

    public void httpPostThread(final String path, JSONObject data) {
        new Thread(() -> httpPost(path, data)).start();
    }

    public JSONObject httpGet(MainAppCompatActivity mainAppCompatActivity, String path) {
        if (mainAppCompatActivity != null){
            mainAppCompatActivity.runOnUiThread(mainAppCompatActivity::showProgressDialog);
        }
        JSONObject response = httpGetHost(path);
        if (response == null) {
            response = new JSONObject();
            try {
                response.put("status_code", "500");
                response.put("status", "Internal Server Error");
                response.put("result", "Ошибка связи с сервером. Попробуйте попозже. (response is null)");
            } catch (JSONException ignored) {
            }
        }
        LogService.getInstance().log(this, "httpGet", "path = '" + path + "'; response = '" + response + "'");
        if (mainAppCompatActivity != null){
            mainAppCompatActivity.runOnUiThread(mainAppCompatActivity::dismissProgressDialog);
        }

        try {
            if (response.getString("status").equals("OK")){
                if (response.has("result")) {
                    MainApplication.getInstance().parseData(response.getJSONObject("result"));
                }
            }
            else {
                if (mainAppCompatActivity != null){
                    if (response.has("result")){
                        String errorText = response.getString("result");
                        mainAppCompatActivity.runOnUiThread(()->mainAppCompatActivity.showToast(errorText));
                    }
                    else {
                        JSONObject finalResponse = response;
                        mainAppCompatActivity.runOnUiThread(()->mainAppCompatActivity.showToast(finalResponse.toString()));
                    }
                }
            }
        } catch (JSONException e) {
            if (mainAppCompatActivity != null){
                mainAppCompatActivity.runOnUiThread(()->mainAppCompatActivity.showToast(e.getMessage()));
            }
        }

        return response;
    }

    public JSONObject httpGet(String path) {
        JSONObject response = httpGetHost(path);
        if (response == null) {
            response = new JSONObject();
            try {
                response.put("status_code", "500");
                response.put("result", "Ошибка связи с сервером. Попробуйте попозже. (response is null)");
            } catch (JSONException ignored) {
            }
        }
        LogService.getInstance().log(this, "httpGet", "path = '" + path + "'; response = '" + response + "'");
        return response;
    }

    public JSONObject httpPost(String path, JSONObject data) {
        JSONObject response = httpPostHost(path, data);
        if (response == null) {
            response = new JSONObject();
            try {
                response.put("status_code", "500");
                response.put("result", "Ошибка связи с сервером. Попробуйте попозже. (response is null)");
            } catch (JSONException ignored) {
            }
        }
        LogService.getInstance().log(this, "httpPost", "path = '" + path + "'; response = '" + response + "'");
        return response;
    }


    private JSONObject httpGetHost(String path) {
        String url = restHost.get(restIndex) + path;
        Response response = restCallGet(url);
        if (response == null) {
            for (int item = 0; item < restHost.size(); item++) {
                if ((item != restIndex) && (response == null)) {
                    url = restHost.get(item) + path;
                    response = restCallGet(url);
                    if (response != null) {
                        restIndex = item;
                    }
                }
            }
        }

        if (response == null) {
            return null;
        }
        if (response.code() != 200) {
            return null;
        }
        try {
            return new JSONObject(Objects.requireNonNull(response.body()).string());
        } catch (JSONException | IOException ignored) {
        }
        return null;
    }

    private JSONObject httpPostHost(String path, JSONObject data) {
        String url = restHost.get(restIndex) + path;
        Response response = restCallPost(url, data);

        if (response == null) {
            for (int item = 0; item < restHost.size(); item++) {
                if ((item != restIndex) && (response == null)) {
                    url = restHost.get(item) + path;
                    response = restCallPost(url, data);
                    if (response != null) {
                        restIndex = item;
                    }
                }
            }
        }
        if (response == null) {
            return null;
        }
        if (response.code() != 200) {
            return null;
        }
        try {
            return new JSONObject(Objects.requireNonNull(response.body()).string());
        } catch (JSONException | IOException ignored) {
        }
        return null;
    }

    private Response restCallGet(String url) {
        LogService.getInstance().log(this, "restCallGet", url);
        Response response = null;
        try {

            Request request = new Request.Builder()
                    .url(url)
                    .header("authorization", "Bearer " + getHeader())
                    .build();
            response = httpClient.newCall(request).execute();
        } catch (Exception exception) {
            MainApplication.getInstance().getRestService().serverError("restCallGet", ExceptionUtils.getStackTrace(exception));
        }
        return response;
    }

    private Response restCallPost(String url, JSONObject data) {
        Response response = null;
        try {
            RequestBody body = RequestBody.create(data.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .header("authorization", "Bearer " + getHeader())
                    .post(body)
                    .build();
            response = httpClient.newCall(request).execute();
        } catch (IOException ignored) {
        }
        return response;
    }


}
