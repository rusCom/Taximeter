package org.toptaxi.taximeter.activities.mainActivity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;


import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.toptaxi.taximeter.MainActivity;
import org.toptaxi.taximeter.MainApplication;
import org.toptaxi.taximeter.R;
import org.toptaxi.taximeter.activities.BalanceActivity;
import org.toptaxi.taximeter.activities.HisOrdersActivity;
import org.toptaxi.taximeter.activities.InviteDriverActivity;
import org.toptaxi.taximeter.activities.MessagesActivity;
import org.toptaxi.taximeter.activities.SettingsActivity;
import org.toptaxi.taximeter.activities.StatisticsActivity;
import org.toptaxi.taximeter.services.LogService;
import org.toptaxi.taximeter.tools.Constants;

public class MainActivityDrawer implements Drawer.OnDrawerItemClickListener {
    MainActivity mainActivity;
    Toolbar toolbar;
    protected AccountHeader accountHeader;

    PrimaryDrawerItem balanceItem, themeItem, messagesItem, unlimInfo;
    PrimaryDrawerItem balanceCorporateTaxiItem;
    ProfileDrawerItem profile;
    Drawer drawer;

    String fullName = "", carName = "";

    public MainActivityDrawer(MainActivity mainActivity, Toolbar toolbar) {
        this.mainActivity = mainActivity;
        this.toolbar = toolbar;


    }

    private void generateDrawer() {
        LogService.getInstance().log(this, "generateNewDrawer");
        fullName = MainApplication.getInstance().getMainAccount().getName();
        carName = MainApplication.getInstance().getMainAccount().getSerName();

        profile = new ProfileDrawerItem()
                .withName(fullName)
                .withEmail(carName)
                .withIcon(AppCompatResources.getDrawable(mainActivity, R.mipmap.ic_launcher));

        accountHeader = new AccountHeaderBuilder()
                .withActivity(mainActivity)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(profile)
                .withCompactStyle(true)
                .withTextColor(ContextCompat.getColor(mainActivity, R.color.account_header_text))
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderListener((view, profile, currentProfile) -> {
                    Intent intent = new Intent(mainActivity, SettingsActivity.class);
                    mainActivity.startActivity(intent);
                    return false;
                })
                .build();

        drawer = new DrawerBuilder()
                .withActivity(mainActivity)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withOnDrawerItemClickListener(this)
                .build();

        balanceItem = new PrimaryDrawerItem().withName("Баланс").withIcon(FontAwesome.Icon.faw_rub).withSelectable(false).withBadge(MainApplication.getInstance().getMainAccount().getBalanceString()).withIdentifier(Constants.MENU_BALANCE);
        drawer.addItem(balanceItem);

        if (MainApplication.getInstance().getPreferences().corporateTaxi){
            balanceCorporateTaxiItem = new PrimaryDrawerItem().withName("Баланс Корпоративное такси").withIcon(FontAwesome.Icon.faw_rub).withSelectable(false).withBadge(MainApplication.getInstance().getMainAccount().getBalanceCorporateTaxiString()).withIdentifier(Constants.MENU_BALANCE_CORPORATE);
            drawer.addItem(balanceCorporateTaxiItem);
        }


        if (MainApplication.getInstance().getPreferences().useUnlimitedTariffPlans()) {
            unlimInfo = new PrimaryDrawerItem().withName(MainApplication.getInstance().getMainAccount().getUnlimitedTariffInfo()).withIcon(FontAwesome.Icon.faw_fire).withSelectable(false).withIdentifier(Constants.MENU_ACITVATE_UNLIM);
            drawer.addItem(unlimInfo);
        }


        drawer.addItem(new DividerDrawerItem());


        // Если у нас есть телефон диспетчера, то занчит ему можно и писать
        if (MainApplication.getInstance().getPreferences().dispatcherMessages) {
            messagesItem = new PrimaryDrawerItem().withName("Чат с диспетчером").withIcon(FontAwesome.Icon.faw_commenting_o).withSelectable(false).withBadge(String.valueOf(MainApplication.getInstance().getMainAccount().getNotReadMessageCount())).withIdentifier(Constants.MENU_MESSAGES);
            drawer.addItem(messagesItem);
        }


        drawer.addItem(new PrimaryDrawerItem().withName("Статистика|Рейтинг").withIcon(FontAwesome.Icon.faw_cube).withSelectable(false).withIdentifier(Constants.MENU_STATISTICS));
        if (MainApplication.getInstance().getPreferences().isDriverInvite()) {
            drawer.addItem(new PrimaryDrawerItem().withName("Пригласить друга").withIcon(FontAwesome.Icon.faw_share_alt).withSelectable(false).withIdentifier(Constants.MENU_DRIVER_INVITE));
        }

        drawer.addItem(new PrimaryDrawerItem().withName("История заказов").withIcon(FontAwesome.Icon.faw_history).withSelectable(false).withIdentifier(Constants.MENU_HIS_ORDERS));


        drawer.addItem(new DividerDrawerItem());
        if (!MainApplication.getInstance().getPreferences().getPaymentInstructionLink().equals("")) {
            drawer.addItem(new PrimaryDrawerItem().withName("Как пополнить баланс").withIcon(FontAwesome.Icon.faw_credit_card).withSelectable(false).withIdentifier(Constants.MENU_PAYMENT_INSTRUCTION));
        }
        if (!MainApplication.getInstance().getPreferences().instructionLink.equals("")) {
            drawer.addItem(new PrimaryDrawerItem().withName("Инструкция по работе").withIcon(FontAwesome.Icon.faw_question).withSelectable(false).withIdentifier(Constants.MENU_INSTRUCTION));
        }
        if (!MainApplication.getInstance().getPreferences().vkGroupLink.equals("")) {
            drawer.addItem(new PrimaryDrawerItem().withName("Группа VK").withIcon(FontAwesome.Icon.faw_vk).withSelectable(false).withIdentifier(Constants.MENU_VK_GROUP));
        }

        if (!MainApplication.getInstance().getPreferences().getDispatcherPhone().equals("")) {
            drawer.addItem(new PrimaryDrawerItem().withName("Позвонить диспетчеру").withIcon(FontAwesome.Icon.faw_phone).withSelectable(false).withIdentifier(Constants.MENU_DISPATCHING_CALL));
        }
        if (!MainApplication.getInstance().getPreferences().getSupportPhone().equals("")) {
            drawer.addItem(new PrimaryDrawerItem().withName("Позвонить в администрацию").withIcon(FontAwesome.Icon.faw_phone).withSelectable(false).withIdentifier(Constants.MENU_SUPPORT_CALL));
        }
        drawer.addItem(new PrimaryDrawerItem().withName("Настройки").withIcon(FontAwesome.Icon.faw_cog).withSelectable(false).withIdentifier(Constants.MENU_SETTINGS));
        themeItem = new PrimaryDrawerItem().withName(MainApplication.getInstance().getPreferences().getThemeName()).withSelectable(false).withIcon(FontAwesome.Icon.faw_exchange).withIdentifier(Constants.MENU_THEME);
        drawer.addItem(themeItem);
        drawer.addItem(new DividerDrawerItem());
        drawer.addItem(new PrimaryDrawerItem().withName("ver. " + MainApplication.getInstance().getAppVersion()).withIcon(FontAwesome.Icon.faw_creative_commons).withEnabled(false).withSelectable(false));

    }

    public void updateDrawer() {
        if (drawer != null) {
            if (!fullName.equals(MainApplication.getInstance().getMainAccount().getName())) {
                generateDrawer();
            } else if (!carName.equals(MainApplication.getInstance().getMainAccount().getSerName())) {
                generateDrawer();
            } else {
                LogService.getInstance().log(this, "updateDrawer");

                balanceItem.withBadge(MainApplication.getInstance().getMainAccount().getBalanceString());
                drawer.updateItem(balanceItem);

                if (MainApplication.getInstance().getPreferences().corporateTaxi){
                    balanceCorporateTaxiItem.withBadge(MainApplication.getInstance().getMainAccount().getBalanceCorporateTaxiString());
                    drawer.updateItem(balanceCorporateTaxiItem);
                }


                if (messagesItem != null) {
                    messagesItem.withBadge(MainApplication.getInstance().getMainAccount().getNotReadMessageCount());
                    drawer.updateItem(messagesItem);
                }

                if (unlimInfo != null) {
                    unlimInfo.withName(MainApplication.getInstance().getMainAccount().getUnlimitedTariffInfo());
                    drawer.updateItem(unlimInfo);
                }
            }
        } else {
            generateDrawer();
        }

    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switch ((int) drawerItem.getIdentifier()) {
            case Constants.MENU_THEME:
                MainApplication.getInstance().getPreferences().changeTheme();
                mainActivity.getDelegate().setLocalNightMode(MainApplication.getInstance().getPreferences().getTheme());
                mainActivity.recreate();
                mainActivity.getApplication().setTheme(R.style.AppTheme);
                themeItem.withName(MainApplication.getInstance().getPreferences().getThemeName());
                drawer.updateItem(themeItem);
                break;
            case Constants.MENU_ACITVATE_UNLIM:
                mainActivity.onUnlimitedTariffPlanClick();
                break;
            case Constants.MENU_MESSAGES:
                mainActivity.startActivity(new Intent(mainActivity, MessagesActivity.class));
                break;
            case Constants.MENU_BALANCE:
                mainActivity.startActivity(new Intent(mainActivity, BalanceActivity.class));
                break;
            case Constants.MENU_BALANCE_CORPORATE:
                Intent balanceCorporateIntent = new Intent(mainActivity, BalanceActivity.class);
                balanceCorporateIntent.putExtra("type", "corporate");
                mainActivity.startActivity(balanceCorporateIntent);
                break;
            case Constants.MENU_STATISTICS:
                mainActivity.startActivity(new Intent(mainActivity, StatisticsActivity.class));
                break;
            case Constants.MENU_DRIVER_INVITE:
                Intent shareDriverIntent = new Intent(mainActivity, InviteDriverActivity.class);
                mainActivity.startActivity(shareDriverIntent);
                break;
            case Constants.MENU_SETTINGS:
                Intent settingsIntent = new Intent(mainActivity, SettingsActivity.class);
                mainActivity.startActivity(settingsIntent);
                break;
            case Constants.MENU_SUPPORT_CALL:
                mainActivity.callIntent(MainApplication.getInstance().getPreferences().getSupportPhone());
                break;
            case Constants.MENU_DISPATCHING_CALL:
                mainActivity.callIntent(MainApplication.getInstance().getPreferences().getDispatcherPhone());
                break;
            case Constants.MENU_HIS_ORDERS:
                mainActivity.startActivity(new Intent(mainActivity, HisOrdersActivity.class));
                break;
            case Constants.MENU_PAYMENT_INSTRUCTION:
                Uri address = Uri.parse(MainApplication.getInstance().getPreferences().getPaymentInstructionLink());
                Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, address);
                mainActivity.startActivity(openLinkIntent);
                break;
            case Constants.MENU_INSTRUCTION:
                Uri instructionLink = Uri.parse(MainApplication.getInstance().getPreferences().instructionLink);
                Intent instructionLinkIntent = new Intent(Intent.ACTION_VIEW, instructionLink);
                mainActivity.startActivity(instructionLinkIntent);
                break;
            case Constants.MENU_VK_GROUP:
                Uri vkGroupLink = Uri.parse(MainApplication.getInstance().getPreferences().vkGroupLink);
                Intent vkGroupLinkIntent = new Intent(Intent.ACTION_VIEW, vkGroupLink);
                mainActivity.startActivity(vkGroupLinkIntent);
                break;
        }
        return false;
    }

    public boolean closeDrawer() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return true;
        }
        return false;
    }
}