package com.track.client.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreference {
    private static final String DB_NAME = "track_client";
    private static AppSharedPreference appSharedPreference;
    private static SharedPreferences preferences;

    private AppSharedPreference() {
    }

    public static AppSharedPreference getInstance(Context context) {
        if (preferences == null) {
            appSharedPreference = new AppSharedPreference();
            preferences = context.getSharedPreferences(DB_NAME, Context.MODE_PRIVATE);
        }
        return appSharedPreference;
    }

    public String getDeviceId() {
        return preferences.getString("device_id", "");
    }

    public void setDeviceId(String deviceId) {
        preferences.edit().putString("device_id", deviceId).apply();
    }

    public void setGpsState(String state) {
        preferences.edit().putString("gps_state", state).apply();
    }

    public String getGpsState() {
        return preferences.getString("gps_state", "0");
    }

    public void setSmsState(String state) {
        preferences.edit().putString("sms_state", state).apply();
    }

    public String getSmsState() {
        return preferences.getString("sms_state", "0");
    }

    public void setRecordState(String state) {
        preferences.edit().putString("record_state", state).apply();
    }

    public String getRecrdState() {
        return preferences.getString("record_state", "0");
    }

}
