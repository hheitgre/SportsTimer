package com.example.sportstimer;

import static android.app.PendingIntent.getActivity;
import static com.example.sportstimer.R.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {
    private Switch switch_playSoundAtEnd;
    private Switch switch_playSoundAtStart;
    private Switch switch_displayMS;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialisiere die BottomNavigationView
        BottomNavigationView bnv;
        bnv = findViewById(id.bottom_navigation);
        bnv.setSelectedItemId(R.id.navigation_bar_destination_settings);
        bnv.setOnItemSelectedListener(item -> {
            Intent intent;
            int id = item.getItemId();
            if(id == R.id.navigation_bar_destination_settings){
                return true;
            }else if(id == R.id.navigation_bar_stopwatch) {
                intent = new Intent(SettingsActivity.this, StopWatchActivity.class);
                startActivity(intent);
                return true;
            }
            else if(id == R.id.navigation_bar_destination_timer) {
                intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }else {
                return false;
            }
        });

        // Lies die Einstellungen
        SettingsUtility settingsUtility = new SettingsUtility();
        SharedPreferences sharedPref = this.getSharedPreferences("preferences",
                Context.MODE_PRIVATE);
        settingsUtility.initSettings(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Initialisiere die UI-Elemente (Switches) \\
        // Sound am Ende der Runde
        switch_playSoundAtEnd = findViewById(id.switch_settings_endSound);
        switch_playSoundAtEnd.setChecked(sharedPref.getBoolean("enableSoundAtEnd", true));
        manipulateWithSwitch(switch_playSoundAtEnd, sharedPref, editor, "enableSoundAtEnd");

        // Sound zu Beginn einer neuen Runde
        switch_playSoundAtStart = findViewById(id.switch_settings_startSound);
        switch_playSoundAtStart.setChecked(sharedPref.getBoolean("enableSoundAtStart", false));
        manipulateWithSwitch(switch_playSoundAtStart, sharedPref, editor, "enableSoundAtStart");

        // Zeige ms bei Stoppuhr an
        switch_displayMS = findViewById(id.switch_settings_displayMS);
        switch_displayMS.setChecked(sharedPref.getBoolean("displayMS",true));
        manipulateWithSwitch(switch_displayMS, sharedPref, editor, "displayMS");


    }

    public static void manipulateWithSwitch(Switch sw, SharedPreferences sp,
                                            SharedPreferences.Editor editor, String setting) {
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(sw.isChecked()){
                    Log.d("SettingsActivity","[manipulateWithSwitch]: " +
                            "Switch " + setting + " activated!");
                    editor.putBoolean(setting,true);
                    editor.apply();
                }else {
                    Log.d("SettingsActivity","[manipulateWithSwitch]: " +
                            "Switch " + setting + " deactivated!");
                    editor.putBoolean(setting,false);
                    editor.apply();
                }
            }
        });
    }



}

