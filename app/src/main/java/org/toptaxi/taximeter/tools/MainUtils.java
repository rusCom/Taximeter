package org.toptaxi.taximeter.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.taximeter.data.SupportContactItem;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.bottomsheets.MainBottomSheetRecycler;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainUtils {

    public static void onSupportContactClick(MainAppCompatActivity appCompatActivity) {
        List<SupportContactItem> supportContactItemList = new ArrayList<>();
        supportContactItemList.add(new SupportContactItem("phone"));
        supportContactItemList.add(new SupportContactItem("whatsapp"));
        supportContactItemList.add(new SupportContactItem("telegram"));
        ArrayList<IMainCardViewData> cards = new ArrayList<>(supportContactItemList);
        MainBottomSheetRecycler myBottomSheetFragment = new MainBottomSheetRecycler(
                cards,
                mainCardViewData -> {
                    SupportContactItem supportContactItem = (SupportContactItem) mainCardViewData;
                    supportContactItem.onClick(appCompatActivity);
                }
        );
        myBottomSheetFragment.show(appCompatActivity.getSupportFragmentManager(), myBottomSheetFragment.getTag());
    }

    public static void scaleButtonDrawables(Button btn, double fitFactor) {
        Drawable[] drawables = btn.getCompoundDrawables();

        for (int i = 0; i < drawables.length; i++) {
            if (drawables[i] != null) {
                int imgWidth = drawables[i].getIntrinsicWidth();
                int imgHeight = drawables[i].getIntrinsicHeight();
                if ((imgHeight > 0) && (imgWidth > 0)) {    //might be -1
                    float scale;
                    if ((i == 0) || (i == 2)) { //left or right -> scale height
                        scale = (float) (btn.getHeight() * fitFactor) / imgHeight;
                    } else { //top or bottom -> scale width
                        scale = (float) (btn.getWidth() * fitFactor) / imgWidth;
                    }
                    if (scale < 1.0) {
                        Rect rect = drawables[i].getBounds();
                        int newWidth = (int)(imgWidth * scale);
                        int newHeight = (int)(imgHeight * scale);
                        rect.left = rect.left + (int)(0.5 * (imgWidth - newWidth));
                        rect.top = rect.top + (int)(0.5 * (imgHeight - newHeight));
                        rect.right = rect.left + newWidth;
                        rect.bottom = rect.top + newHeight;
                        drawables[i].setBounds(rect);
                    }
                }
            }
        }
    }

    public static boolean isEmptyString(String string) {
        return string == null || string.isEmpty();
    }
    public static Double round(Double d, int precise) {
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.setScale(precise, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    public static String getDistance(Double Distance) {
        String result = "0";
        if (Distance > 0) {
            if (Distance < 1000) {
                String pattern = "##0";
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                String format = decimalFormat.format(Distance);
                result = "~" + format + " м.";
            } else {
                String pattern = "##0.00";
                DecimalFormat decimalFormat = new DecimalFormat(pattern);
                String format = decimalFormat.format(Distance / 1000);
                result = "~" + format + " км.";
            }
        }

        return result;
    }

    public static String getRubSymbol() {
        String result;
        result = String.valueOf(Html.fromHtml("&#x20bd", Html.FROM_HTML_MODE_LEGACY)); // for 24 api and more
        if (result.trim().isEmpty()) result = "руб.";
        return result;
    }

    public static String getSummaString(int summa) {
        return new DecimalFormat("###,##0").format(summa) + " " + getRubSymbol();
    }

    public static String getSummaString(Float summa) {
        LogService.getInstance().log("InviteActivity", summa.toString());
        return new DecimalFormat("##0.00").format(summa) + " " + getRubSymbol();
    }

    public static String getOrderCountName(int count) {
        return switch (count) {
            case 1, 21, 31, 41 -> "заказ";
            case 2, 3, 4, 22, 23, 24, 32, 33, 34, 42, 43, 44 -> "заказа";
            default -> "заказов";
        };
    }

    public static String getMonthName(LocalDate date){
        switch (date.getMonthValue()) {
            case 1 -> {
                return "Январь";
            }
            case 2 -> {
                return "Февраль";
            }
            case 3 -> {
                return "Март";
            }
            case 4 -> {
                return "Апрель";
            }
            case 5 -> {
                return "Май";
            }
            case 6 -> {
                return "Июнь";
            }
            case 7 -> {
                return "Июль";
            }
            case 8 -> {
                return "Август";
            }
            case 9 -> {
                return "Сентябрь";
            }
            case 10 -> {
                return "Октябрь";
            }
            case 11 -> {
                return "Ноябрь";
            }
            case 12 -> {
                return "Декабрь";
            }


            default -> throw new IllegalStateException("Unexpected value: " + date.getMonthValue());
        }

    }

    public static Boolean JSONGetBool(JSONObject data, String field, Boolean def) {
        Boolean result = JSONGetBoolean(data, field);
        if (result == null) return def;
        return result;
    }

    public static Boolean JSONGetBool(JSONObject data, String field) {
        Boolean result = JSONGetBoolean(data, field);
        if (result == null) return false;
        return result;
    }

    private static Boolean JSONGetBoolean(JSONObject data, String field) {
        Boolean result = null;
        if (data.has(field)) {
            try {
                result = data.getBoolean(field);
            } catch (JSONException ignored) {
            }
        }

        if (result == null) {
            String fieldValue = JSONGetString(data, field);
            if (fieldValue.equals("true")) {
                result = true;
            }
            if (fieldValue.equals("1")) {
                result = true;
            }
        }
        return result;
    }

    public static String JSONGetString(JSONObject data, String field, String def) {
        String result = JSONGetString(data, field);
        if (result.equals("")) {
            result = def;
        }
        return result;
    }

    public static String JSONGetString(JSONObject data, String field) {
        String result = "";
        if (field.startsWith("result_")) {
            if (data.has("result")) {
                try {
                    JSONObject resultObject = data.getJSONObject("result");
                    field = field.replace("result_", "");
                    result = resultObject.getString(field);
                } catch (JSONException ignored) {
                }
            }
        } else if (data.has(field)) {
            try {
                result = data.getString(field);
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    public static Integer JSONGetInteger(JSONObject data, String field, Integer def) {
        Integer result = JSONGetInteger(data, field);
        if (result == null) {
            result = def;
        }
        return result;
    }

    public static Integer JSONGetInteger(JSONObject data, String field) {
        String strResult = JSONGetString(data, field);
        if (strResult.equals("")) return null;
        return Integer.parseInt(strResult);
    }

    public static Calendar JSONGetCalendar(JSONObject data, String field) {
        String stringData = JSONGetString(data, field);
        if (stringData.equals("")) return null;
        stringData = stringData.replace("T", " ");

        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(Timestamp.valueOf(stringData).getTime());
        return result;
    }
    public static Float JSONGetFloat(JSONObject data, String field) {
        Float result = null;
        if (data.has(field)) {
            try {
                result = Float.parseFloat(data.getString(field));
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    public static Double JSONGetDouble(JSONObject data, String field) {
        Double result = null;
        if (data.has(field)) {
            try {
                result = data.getDouble(field);
            } catch (JSONException ignored) {
            }
        }
        return result;
    }

    public static Double JSONGetDouble(JSONObject data, String field, Double def) {
        Double result = JSONGetDouble(data, field);
        if (result == null) return def;
        return result;
    }

    public static void TextViewSetTextOrGone(TextView textView, String text) {
        if (text == null) {
            textView.setVisibility(View.GONE);
        } else if (text.trim().equals("")) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text.trim());
        }
    }

    public static void TextViewSetTextOrGone(TextView textView, View divider, String text) {
        if (text.equals("")) {
            textView.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }

    public static boolean isJSONArrayHaveValue(JSONArray jsonArray, String value) {
        for (int itemID = 0; itemID < jsonArray.length(); itemID++) {
            try {
                if (jsonArray.getString(itemID).equals(value)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String convertPhoneToCall(String phone){
        return "+7 (" + phone.substring(1,4) + ") " + phone.substring(4,7) + " " + phone.substring(7,9) + "-" + phone.substring(9,11);
    }

    public static String convertPhone(String phone) {
        if (phone == null) {
            return "";
        }
        if (phone.isEmpty()) {
            return "";
        }

        String result = phone.replaceAll("[^\\d]", "");
        if (result.length() == 10) {
            result = "8" + result;
        } else {
            result = "8" + result.substring(1);
        }

        if (!result.startsWith("89")) {
            return "";
        }

        if (result.length() != 11) {
            return "";
        }

        return result;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void openPlayMarketIntent(Context context, String appName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appName)));
        }
    }

    public static Integer passedTimeSek(long timeout) {
        if (timeout == 0) {
            return 2147483647;
        }
        long absMillis = Math.abs(System.currentTimeMillis() - timeout);
        int absSek = Math.toIntExact(absMillis / 1000);
        return absSek;
    }

    public static Integer passedTimeHour(long timeout) {
        if (timeout == 0) {
            return 2147483647;
        }
        long absMillis = Math.abs(System.currentTimeMillis() - timeout);
        int absSek = Math.toIntExact(absMillis / 1000 / 60 / 60);
        return absSek;
    }
}
