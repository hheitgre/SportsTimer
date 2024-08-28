package com.example.sportstimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SettingsUtility {
    public void initSettings(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("preferences",
                Context.MODE_PRIVATE);

        if(preferences.getBoolean("firstRun", true)) {
            // Die App wird zum 1. Mal ausgefuehrt
            Log.d("SettingsActivity","[initSettings]: First run. Writing default" +
                    " settings");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("enableSoundAtEnd", true);
            editor.putBoolean("enableSoundAtStart", true);
            editor.putBoolean("firstRun", false);
            editor.putBoolean("displayMS", true);
        } else {
            Log.d("SettingsActivity","[initSettings]: Not the first run."
                    + " Reading settings...");
        }
    }
}
