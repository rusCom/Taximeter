package org.toptaxi.taximeter.data;

import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.tools.MainAppCompatActivity;
import org.toptaxi.taximeter.tools.MainUtils;
import org.toptaxi.taximeter.tools.cardview.IMainCardViewData;

public class SupportContactItem implements IMainCardViewData {
    private final String type;

    public SupportContactItem(String type) {
        this.type = type;
    }

    public void onClick(MainAppCompatActivity mainAppCompatActivity){
        String phone = MainApplication.getInstance().getPreferences().getSupportPhone();
        switch (type){
            case "phone" -> mainAppCompatActivity.callIntent(phone);
            case "whatsapp" -> mainAppCompatActivity.goToURL("https://wa.me/" + convertPhoneToWhatsApp(phone));
            case "telegram" -> mainAppCompatActivity.goToURL("https://t.me/" + convertPhoneToTelegram(phone));
        }
    }

    public void onClick(MainAppCompatActivity mainAppCompatActivity, String phone){
        switch (type){
            case "phone" -> mainAppCompatActivity.callIntent(phone);
            case "whatsapp" -> mainAppCompatActivity.goToURL("https://wa.me/" + convertPhoneToWhatsApp(phone));
            case "telegram" -> mainAppCompatActivity.goToURL("https://t.me/" + convertPhoneToTelegram(phone));
        }
    }


    public static String convertPhoneToWhatsApp(String phone){
        phone = MainUtils.convertPhone(phone);
        phone = "7" + phone.substring(1);
        return phone;
    }

    public static String convertPhoneToTelegram(String phone){
        phone = MainUtils.convertPhone(phone);
        phone = "+7" + phone.substring(1);
        return phone;
    }

    @Override
    public String getMainText() {
        switch (type) {
            case "phone" -> {
                return "Позвонить в техподдержку";
            }
            case "whatsapp" -> {
                return "Написать в WhatsApp";
            }
            case "telegram" -> {
                return "Написать в Telegram";
            }
        }
        return "";
    }

    @Override
    public Integer getImageResourceID() {
        switch (type) {
            case "phone" -> {
                return R.drawable.ic_phone;
            }
            case "whatsapp" -> {
                return R.drawable.ic_whatsapp;
            }
            case "telegram" -> {
                return R.drawable.ic_telegram;
            }
        }
        return null;
    }
}
