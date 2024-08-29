package com.example.sportstimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Looper;



public class TimerActivity extends AppCompatActivity {

    private TextView statusField;    // Status: "FIGHT" / "COOLDOWN" / "FINISHED"
    private TextView timeField;
    private TextView roundsField;
    private ProgressBar progressBar;
    private TimerThread timerThread; // TODO: ?
    private TimerThread timeoutThread;
    private Handler handler = new Handler(Looper.getMainLooper());
    private MyTimer timer;
    private MyTimer timeout;
    private enum TimerState {
        TIMER,
        TIMEOUT,
        FINISHED
    }
    private TimerState currentState = TimerState.TIMER;
    private MediaPlayer mp;
    private boolean isSoundAtEndEnabled;
    private boolean isSoundAtStartEnabled;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        statusField = findViewById(R.id.textViewStatus);
        timeField = findViewById(R.id.textViewTimer);
        roundsField = findViewById(R.id.textViewRounds);
        progressBar = findViewById(R.id.progress_bar);

        // Lese die Nutzereinstellungen
        SettingsUtility settingsUtility = new SettingsUtility();
        SharedPreferences sharedPref = this.getSharedPreferences("preferences",
                Context.MODE_PRIVATE);
        isSoundAtEndEnabled = sharedPref.getBoolean("enableSoundAtEnd",true);
        isSoundAtStartEnabled = sharedPref.getBoolean("enableSoundAtStart",false);
        int test = sharedPref.getInt("test",-1);
        Log.d("Timeractivity SHAREDPREF: ", Integer.toString(test));
        settingsUtility.initSettings(this);


        // Der aus der MainActivity gesendete Intent wird hier ausgelesen.
        Intent intent = getIntent();
        int totalSeconds = intent.getIntExtra("totalSeconds", -1);
        int enteredRounds = intent.getIntExtra("enteredRounds", -1);
        int enteredTimeOut = intent.getIntExtra("enteredTimeOut", 0);

        /**
         * Fange "Zurueck"-Nutzereingaben ab. Setze den TimerState auf FINISHED, um die Threads zu
         * stoppen. Sende beiden Threads zusätzlich zur Sicherheit das .stopTimer()-Signal.
         * Signalisiere mit finish() die Beendigung der Aktivität in TimerProcess.java, um zurück
         * zur MainActivity zu gelangen.
         * @param enabled legt fest, ob dieser callback aktiv sein soll
         */
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(getApplicationContext(), "Going back", Toast.LENGTH_SHORT).show();
                Log.d("callback","Sending FINISHED signal and going back.");
                currentState = TimerState.FINISHED;
                timerThread.stopTimer();
                timeoutThread.stopTimer();
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);

        // Starte den Timer
        timer = new MyTimer(totalSeconds, enteredRounds, enteredTimeOut, 1, "FIGHT");
        timeout = new MyTimer(enteredTimeOut, 1, 0, 1, "TIMEOUT");
        timerThread = new TimerThread();
        timeoutThread = new TimerThread();
        handler.post(timerRunnable);

    }


    /**
     * Die Runnable dient dazu, den TimerThread besser steuern zu koennen. Sie sendet dem
     * TimerThread Impulse in Abhaengigkeit von der enum TimerState. Sofern der TimerThread kein
     * isCompleted-Signal gesendet hat und der currentState bei TIMER bleibt, laeuft der TimerThread
     * und die Runnable aktualisiert das UI.
     */
    private Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if(currentState == TimerState.TIMER) {
                    if (!timerThread.isRunning() && !timerThread.isCompleted()) {
                        timerThread.load(timer);
                        timerThread.start(timeoutThread);  // @param conflict
                    }
                }else if(currentState == TimerState.FINISHED){
                    timerThread.stopTimer();
                    Log.d("timerThread","Received FINISH signal.");
                }
                try {
                    timerThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (!timerThread.isCompleted()) {
                    if(timerThread.getProgress() < 100) {
                        Log.d("TimerProcess", "TimerThread not completed yet;" +
                                " runtime: " + timerThread.getRuntimeInSeconds() + " (" +
                                timerThread.getProgress() + "%)");
                        runOnUiThread(() -> {
                            progressBar.setProgress((int) Math.ceil(timerThread.getProgress()));
                            statusField.setText(timer.getTitle());
                            timeField.setText(toMinuteLogic(timer.getSeconds() -
                                    timerThread.getRuntimeInSeconds()));
                            roundsField.setText(timer.getCurrentRound() + "/" + timer.getRounds());
                        });
                        handler.postDelayed(this, 200);
                    } else {
                        timerThread.setCompleted(true);
                    }
                }else {
                    progressBar.setProgress(100);
                    timeField.setText(toMinuteLogic(0));
                    // Spiele Sound ab, sofern aktiviert
                    if(isSoundAtEndEnabled) {
                        Log.d("TimerActivity","[timerRunnable]: sound at end is enabled");
                        playSound(R.raw.bell);
                    } else {
                        Log.d("TimerActivity","[timerRunnable]: sound at end is" +
                                " NOT enabled");
                    }
                    Log.d("TimerProcess", "Round " +
                            timer.getCurrentRound() + " completed.");


                    if (timer.getCurrentRound() < timer.getRounds()) {
                        Log.d("TimerProcess","Es gibt noch Runden...");
                        timer.setCurrentRound(timer.getCurrentRound() +1);
                        // Gibt es eine Cooldown-Phase?
                        if(timer.isTimeOut()) {
                            timeoutThread.load(timeout);
                            timeoutThread.start(timerThread);
                            currentState = TimerState.TIMEOUT;
                            handler.postDelayed(timeoutRunnable,250);

                        }else {
                            // Speichere den Fortschritt im Timer und starte neue Runde

                            timerThread.load(timer);
                            currentState = TimerState.TIMER;
                            handler.post(timerRunnable);

                        }
                    }else{
                        // Alle Runden beendet
                        Log.d("TimerProcess", "Finished all rounds.");
                        statusField.setText("ALL ROUNDS COMPLETE");
                        currentState = TimerState.FINISHED;
                    }
                }

            }
        };

    /**
     * Diese Runnable hat den gleichen grundlegenden Aufgbau wie timerRunnable. Im Gegensatz
     * zur timerRunnable ruft sie den timeoutThread auf. Sie wird von der timeoutRunnable
     * gerufen, wenn eine Runde beendet ist und ein Timeout eingestellt wurde. Nach Abschluss des
     * Timeouts setzt sie den currentState zurück auf TIMER und ruft die TimerRunnable wieder auf.
     */
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentState == TimerState.TIMEOUT) {
                if (!timeoutThread.isCompleted()) {
                    runOnUiThread(() -> {
                        progressBar.setProgress((int)timeoutThread.getProgress());
                        statusField.setText(timeout.getTitle());
                        timeField.setText(
                                toMinuteLogic(timer.getTimeout() -
                                        timeoutThread.getRuntimeInSeconds()));
                        roundsField.setText("NEXT: " + timer.getCurrentRound() +
                                "/" + timer.getRounds());
                    });
                    handler.postDelayed(this, 250);
                } else {
                    // Timeout abgeschlossen, gehe zurück in regulären Timer
                    Log.d("TimerProcess", "Timeout completed.");
                    progressBar.setProgress(100);

                    // Der Ton fuer den Start der naechsten Runde wird am Ende der Timeout-Phase
                    // abgespielt
                    if(isSoundAtStartEnabled) {
                        Log.d("TimerActivity","[timerRunnable]: sound at start is enabled");
                        playSound(R.raw.beep_start);
                    }else {
                        Log.d("TimerActivity","[timerRunnable]: sound at start is not enabled");
                    }

                    timerThread.load(timer);
                    timerThread.start(timeoutThread);
                    currentState = TimerState.TIMER;
                    handler.post(timerRunnable);
                }
            }else if(currentState == TimerState.FINISHED){
                timeoutThread.stopTimer();
                Log.d("timeoutThread","Received FINISH signal.");
            }
        }
    };


    /**
     * Wandelt einen long-Wert in Minutenformat um. Der Eingabewert wird durch
     * 60 geteilt, um die Anzahl der Minuten zu ermitteln. Fuer die Anzahl der Sekunden wird
     * eine Modulo-Operation mit 60 auf die Anzahl der Sekunden angewendet.
     * @param sec die Hoehe der Sekunden, die umgewandelt werden sollen
     * @return ein fertiger String im Format "m:ss"
     */
    @SuppressLint("DefaultLocale")
    public String toMinuteLogic(long sec) {
        return String.format("%d:%02d", sec / 60, sec % 60);
    }


    /**
     * Spielt einen Klingel-Sound ab. Es wird zu Beginn ueberprueft, ob bereits eine mp-Instanz
     * vorhanden ist und diese ggf. freigegeben und entfernt, um eine Thread-Sicherheit
     * zu gewaehrleisten. Anschliessend wird eine neue Instanz erstellt und gestartet.
     * Mit dem setOnCompletionListener wird der MediaPlayer wieder freigegeben und entfernt,
     * nachdem der Sound abgespielt wurde.
     */

    protected void playSound(int rawID) {
        if (mp != null) {
            mp.release();
            mp = null;
        }

        mp = MediaPlayer.create(this, rawID);
        if (mp != null) {
            try {
                mp.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build());
                mp.start();
                mp.setOnCompletionListener(mediaPlayer -> {
                    mediaPlayer.release();
                    mp = null;
                });
            } catch (IllegalStateException e) {
                Log.e("MediaPlayer", "Error: " + e.getMessage());
                if (mp != null) {
                    mp.release();
                    mp = null;
                }
            }
        } else {
            Log.e("MediaPlayer", "Failed to create MediaPlayer instance");
        }
    }

    private void readPreferences() {

    }
}