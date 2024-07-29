package org.toptaxi.taximeter.services;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
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


    public RestService() {
        httpClient = new OkHttpClient();

        header = new JSONObject();
        reloadHeader();
        restHost = new ArrayList<>();
        restHost.add(MainApplication.getInstance().getResources().getString(R.string.mainRestHost));
        restHost.add(MainApplication.getInstance().getResources().getString(R.string.mainRestHost));
        restHost.add(MainApplication.getInstance().getResources().getString(R.string.reserveRestHost));
        restIndex = 0;
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
        } catch (JSONException ignored) {
        }
    }

    private String getHeader() {
        try {
            header.put("location", MainApplication.getInstance().getLocationService().toJSON());
        } catch (JSONException ignored) {
        }
        return Base64.getEncoder().encodeToString(header.toString().getBytes());
    }

    public void httpGetThread(final String path) {
        new Thread(() -> httpGetHost(path)).start();
    }

    public void httpPostThread(final String path, JSONObject data) {
        new Thread(() -> httpPost(path, data)).start();
    }

    public JSONObject httpGet(MainAppCompatActivity mainAppCompatActivity, String path) {
        if (mainAppCompatActivity != null) {
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
        if (mainAppCompatActivity != null) {
            mainAppCompatActivity.runOnUiThread(mainAppCompatActivity::dismissProgressDialog);
        }

        try {
            if (response.getString("status").equals("OK")) {
                if (response.has("result")) {
                    MainApplication.getInstance().parseData(response.getJSONObject("result"));
                }
            } else {
                if (mainAppCompatActivity != null) {
                    if (response.has("result")) {
                        String errorText = response.getString("result");
                        mainAppCompatActivity.showToast(errorText);
                    } else {
                        mainAppCompatActivity.showToast(response.toString());
                    }
                }
            }
        } catch (JSONException e) {
            if (mainAppCompatActivity != null) {
                mainAppCompatActivity.showToast(e.getMessage());
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
        // LogService.getInstance().log("sys", "httpGet", "path = '" + path + "'; response = '" + response + "'");
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
        } catch (Exception ignored) {
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

    public void sendFile(String path, String memberId, String sourceImageFile) {
        try {
            String url = restHost.get(restIndex) + path;
            File sourceFile = new File(sourceImageFile);
            final MediaType MEDIA_TYPE = sourceImageFile.endsWith("png") ?
                    MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            //LogService.getInstance().log("sys", url);
            //LogService.getInstance().log("sys", MEDIA_TYPE.toString());

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(memberId, sourceImageFile, RequestBody.create(sourceFile, MEDIA_TYPE))
                    // .addFormDataPart("result", "my_image")
                    .build();
            Request request = new Request.Builder()
                    .header("authorization", "Bearer " + getHeader())
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = httpClient.newCall(request).execute();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }


    }


}
