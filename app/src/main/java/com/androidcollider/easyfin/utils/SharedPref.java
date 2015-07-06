package com.androidcollider.easyfin.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;


public class SharedPref {

    private SharedPreferences sharedPreferences;
    private final static String APP_PREFERENCES = "FinUPref";

    public SharedPref(Context context) {
        this.sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }


    public void disableSnackBarAccount() {
        sharedPreferences.edit().putBoolean("snackAccountDisabled", true).apply();
    }

    public boolean isSnackBarAccountDisable() {
        return sharedPreferences.contains("snackAccountDisabled");
    }



    public void setMainBalanceSettingsConvertCheck(boolean b) {
        sharedPreferences.edit().putBoolean("mainBalanceSettingsConvertChecked", b).apply();
    }

    public boolean getMainBalanceSettingsConvertCheck() {
        return sharedPreferences.getBoolean("mainBalanceSettingsConvertChecked", false);
    }


    public void setMainBalanceSettingsShowCentsCheck(boolean b) {
        sharedPreferences.edit().putBoolean("mainBalanceSettingsShowCentsChecked", b).apply();
    }

    public boolean getMainBalanceSettingsShowCentsCheck() {
        return sharedPreferences.getBoolean("mainBalanceSettingsShowCentsChecked", false);
    }



    public void setRatesInDBExistStatus() {
        sharedPreferences.edit().putBoolean("ratesInDBExistStatus", true).apply();
    }

    public boolean getRatesInDBExistStatus() {
        return sharedPreferences.getBoolean("ratesInDBExistStatus", false);
    }


    public void setRatesUpdateTime() {
        long date = new Date().getTime();
        sharedPreferences.edit().putLong("ratesUpdateTime", date).apply();
    }

    public long getRatesUpdateTime() {
        return sharedPreferences.getLong("ratesUpdateTime", 0);
    }

}
