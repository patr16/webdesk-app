package com.nic.webdesk;

import android.content.Context;
import android.content.SharedPreferences;
/*
----------------------------------------------------------------
Shared Preferences
----------------------------------------------------------------
Get instance
SharedPrefs prefs = SharedPrefs.getInstance(this);

Save data
prefs.setUsername("pat");
prefs.setFlag1(true);

Read data
String user = prefs.getUsername();
boolean f1 = prefs.getFlag1();

Clean alla data
prefs.clearAll();
----------------------------------------------------------------
 */
public class SharedPrefs {

    private static final String PREFS_NAME = "webdesk_prefs";
    private static SharedPrefs instance;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //-------------------------------- Keys
    private static final String KEY_AUTOLOG = "autoLog";

    //-------------------------------- Private constructor for singleton
    private SharedPrefs(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    //-------------------------------- Get instance singleton
    public static SharedPrefs getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefs(context);
        }
        return instance;
    }

    // ---------------------- autoLog ----------------------
    public void setAutoLog(boolean autoLog) {
        editor.putBoolean(KEY_AUTOLOG, autoLog).apply();
    }

    public boolean getAutoLog() {
        return preferences.getBoolean(KEY_AUTOLOG, false);
    }

    // ---------------------- Clear all ----------------------
    public void clearAll() {
        editor.clear().apply();
    }
}
