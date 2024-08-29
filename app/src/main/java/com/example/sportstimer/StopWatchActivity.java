package com.example.sportstimer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StopWatchActivity extends AppCompatActivity {

    private TextView textViewSWTime;
    private TextView textViewLaps;
    private Button startStopButton;
    private Button resetButton;
    private Button lapButton;
    private boolean isRunning;
    private boolean isPaused = false;
    private long runtime;
    private Handler handler = new Handler(Looper.getMainLooper());
    private long start;
    private String laps;
    private int lapCount = 0;
    private boolean displayMS;
    private String colorScheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        // Initialisiere die Bottom Navigation Bar
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.setSelectedItemId(R.id.navigation_bar_stopwatch);
        bnv.setOnItemSelectedListener(item -> {
          Intent intent;
          int id = item.getItemId();
          if (id == R.id.navigation_bar_stopwatch) {
              return true;
          } else {
              // Stoppe den Thread und setze ihn zurück, wenn die
              // Aktivität gewechselt wird, um Ressourcen zu sparen
              resetSW();
          }

        if (id == R.id.navigation_bar_destination_timer) {
          intent = new Intent(StopWatchActivity.this, MainActivity.class);
          startActivity(intent);
          return true;
          } else if (id == R.id.navigation_bar_destination_settings) {
              intent = new Intent(StopWatchActivity.this, SettingsActivity.class);
              startActivity(intent);
              return true;
          } else {
              return false;
          }
        });

        // Lies die Einstellungen
        SettingsUtility settingsUtility = new SettingsUtility();
        SharedPreferences sharedPref = this.getSharedPreferences("preferences",
                Context.MODE_PRIVATE);
        settingsUtility.initSettings(this);
        displayMS = sharedPref.getBoolean("displayMS", true);
        colorScheme = sharedPref.getString("colorScheme","Lavender / Midnight");

        // Initialisiere UI-Elemente
        textViewSWTime = findViewById(R.id.SW_textViewTime);
        textViewSWTime.setText(toSWLogic(0));
        textViewLaps = findViewById(R.id.SW_textViewLaps);
        startStopButton = findViewById(R.id.SW_startStopButton);
        resetButton = findViewById(R.id.SW_resetButton);
        lapButton = findViewById(R.id.SW_lapButton);

        // Passe die UI-Elemente an das gewuenschte Farbschema an
        SettingsUtility.applyColorToButton(this, colorScheme, startStopButton, true);
        SettingsUtility.applyColorToButton(this, colorScheme, lapButton, false);
        SettingsUtility.applyColorToButton(this, colorScheme, resetButton, true);




        // Setze onClick-Listener auf die Buttons
        startStopButton.setOnClickListener(view -> {
            if (!isRunning) {
                Log.d("swThread", "[startStopButton] pressed: starting sw...");
                isRunning = true;
                start = System.currentTimeMillis(); // Initialize start time
                handler.post(swRunnable);
                startStopButton.setText("STOP");
            } else {
                if (isPaused) {
                    Log.d("swThread", "[startStopButton] pressed: resuming sw...");
                    resumeSW();
                } else {
                    Log.d("swThread", "[startStopButton] pressed: pausing sw...");
                    pauseSW();
                }
            }
        });

        resetButton.setOnClickListener(view -> {
            resetSW();
            clearLaps();
        });

        lapButton.setOnClickListener(view -> {
            addLap();
        });
    }

    /**
     * Diese Runnable laeuft ohne zeitliches Limit so lange, bis isRunning = false ist. Sie
     * aktualisiert das UI entsprechend den Szenarien pausiert und laufend.
     */
    Runnable swRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                if (!isPaused) {
                    runtime = System.currentTimeMillis() - start;
                    textViewSWTime.setText(toSWLogic(runtime));
                    Log.d("swThread", "SW running: " + runtime);
                    handler.postDelayed(this, 25);
                } else {
                    Log.d("swThread", "PAUSED");
                    textViewSWTime.setText(toSWLogic(runtime));

                }
            }
        }
    };

    /**
     * Sendet das isPaused-Signal an die swRunnable
     */
    public void pauseSW() {
        if (!isPaused) {
            isPaused = true;
            Log.d("swThread", "[pauseSW] sending pause signal");
            startStopButton.setText("RESUME");
        } else {
            Log.e("swThread", "[resumeSW] attempted to pause already paused thread");
        }
    }

    /**
     * Ruft die swRunnable nach Pausierung wieder an dem Zeitpunkt der Pausierung auf
     */
    public void resumeSW() {
        if (isPaused) {
            Log.d("swThread", "[resumeSW] sending resume signal");
            isPaused = false;
            start = System.currentTimeMillis() - runtime;
            startStopButton.setText("STOP");
            handler.post(swRunnable);
        } else {
            Log.e("swThread", "[resumeSW] attempted to resume already running thread");
        }
    }

    /**
     * Setzt alle Funktionen der Stoppuhr auf den Ursprungszustand
     * zurueck und beendet die swRunnable.
     */
    public void resetSW() {
        isRunning = false;
        isPaused = false;
        clearLaps();
        startStopButton.setText("START");
        textViewSWTime.setText(toSWLogic(0));
        Log.d("swThread", "[resetSW]: called successfully");
    }

    /**
     * Wandelt, wie toMinuteLogic, Parameterwert in ein Digitaluhrformat um.
     * @param ms
     * @return einen String im Format mm:ss.ms{3}, z.B.: "01:30.500"
     */
    public String toSWLogic(long ms) {
        long min = ms / 60_000;
        long sec = (ms % 60_000) / 1000;
        long mil = ms % 1000;
        if(displayMS){
            return String.format("%02d:%02d.%03d", min, sec, mil);
        } else{
            return String.format("%02d:%02d", min, sec);
        }

    }

    /**
     * Fuegt eine neue Runde hinzu. Hierfuer werden lapCount (Anzahl der Runden)
     * und aktuelle runtime in Stoppuhrlogik in das TextView geschrieben. Der
     * lapCount wird um 1 inkrementiert, um auf den naechsten Aufruf vorzubereiten
     */
    public void addLap() {
        if(lapCount == 0)  {
            laps = "";
        }
        lapCount++;
        laps += lapCount + " | " + toSWLogic(runtime) + "\n";
        textViewLaps.setText(laps);
    }

    /**
     * Loescht die im textViewLaps angezeigten Runden
     * und setzt den lapCount wieder auf 0 zurueck
     */
    public void clearLaps() {
        lapCount = 0;
        laps = "";
        textViewLaps.setText(laps);
    }
}