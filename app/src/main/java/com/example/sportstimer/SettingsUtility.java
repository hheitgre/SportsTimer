package com.example.sportstimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

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
            editor.putString("colorSchemes","Lavender / Midnight");
        } else {
            Log.d("SettingsActivity","[initSettings]: Not the first run."
                    + " Reading settings...");
        }
    }

    /**
     * Diese Methode beschleunigt das Setzen der Hintergrundfarben gem. dem vom Nutzer
     * gewuenschten Farbschema. Es wird das anzuwendende Farbschema und der Knopf uebergeben.
     * Der Knopf erhaelt je nach boolean primary die primaere oder sekundaere Farbe des Schemas.
     * @param context uebergibt der Methode den Kontext der aktuellen Activity
     * @param colorScheme ist der colorScheme-String, je Klasse aus den sharedPreferences gelesen
     * @param button ist der Knopf, dessen Farbe angepasst werden soll
     * @param primary legt fest, ob die Primaer- oder Sekundaerfarbe gesetzt wird
     */
    public static void applyColorToButton(Context context,
                                          @NonNull String colorScheme,
                                          Button button, boolean primary){
        int colorToSet = -1;
        switch (colorScheme) {
            case "Lavender / Midnight":
                colorToSet = primary ? ContextCompat.getColor(context, R.color.midnight) :
                        ContextCompat.getColor(context, R.color.lavender);
                break;
            case "Light Blue / Dark Blue":
                colorToSet = primary ? ContextCompat.getColor(context, R.color.blue) :
                        ContextCompat.getColor(context, R.color.babyBlue);
                break;
            case "Grey / Black":
                colorToSet = primary ? ContextCompat.getColor(context, R.color.black) :
                        ContextCompat.getColor(context, R.color.grey);
                break;
        }
        if(colorToSet != -1){
            button.setBackgroundTintList(ColorStateList.valueOf(colorToSet));
        } else{
            Log.e("SettingsUtility","[applyColorToButton]: " +
                    "Couldn't retrieve color scheme: " +  colorScheme);
        }
    }
}
