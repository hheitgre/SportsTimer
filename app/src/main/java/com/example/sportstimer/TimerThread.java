package com.example.sportstimer;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * In dieser Klasse werden die im Hintergrund laufenden Threads festgelegt, welche von den
 * Runnables in den Klassen TimerActivity, StopWatchActivity aufgerufen werden
 * @author Heiko Heitgress
 */

public class TimerThread extends Thread {
    private long start;
    private long limit;
    private long end;
    private Thread currentThread = null;
    private double progress;
    private boolean isCompleted = false;
    private long runtime;


    public TimerThread(){
        this.start = System.currentTimeMillis();
        this.end = this.start + this.limit;
        this.progress = 0;
        this.runtime = 0;
    }

    /**
     * Hiermit wird ein TimerThread initialisiert; es wird der Start- und Endzeitpunkt berechnet.
     * Ausserdem werden die Attribute des TimerThreads mit den Werten 0 bzw. false initialisiert.
     * @param timer: das Sekundenlimit des Timer-Objektes fuer die Berechnung des Zeitlimits
     */
    public void load(@NonNull MyTimer timer){
        stopTimer();
        this.start = System.currentTimeMillis();
        this.limit = timer.getSeconds() * 1000L;
        this.end = this.start + this.limit;
        this.progress = 0;
        this.runtime = 0;
        this.isCompleted = false;
        Log.d("TimerThread", "Loaded new timer: limit=" + limit + ", end=" + end +
                ", completed: " + isCompleted);
    }

    /**
     * Startet den TimerThread, welcher so lange laeuft, bis seine runtime das limit des MyTimer-
     * Objektes erreicht hat oder er interrupted wird (er die InterruptedException e catched).
     * @param conflict ist ein Thread, der zur Sicherheit beendet wird, wenn er noch aktiv ist,
     *                 um Rivalitaeten zwischen den Threads zu vermeiden
     */
    public synchronized void start(@Nullable TimerThread conflict){
        Log.d("TimerThread", "Starting thread...");
        if(conflict != null && conflict.isRunning()){
            conflict.stopTimer();
        }
        if(currentThread != null && currentThread.isAlive()){
            currentThread.interrupt();
            currentThread = null;
        }

        this.isCompleted = false;
        currentThread = new Thread(() -> {
            runtime = System.currentTimeMillis() - start;
            while(runtime < limit) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log.d("TimerThread", "Calling return");
                    return;
                }
                runtime = System.currentTimeMillis() - start;
                progress = ((double)runtime / (double) limit) * 100;
                Log.d("TimerThread", "Runtime: "
                        + runtime + "(" + progress + "%)");
            }
            this.isCompleted = true;
            Log.d("TimerThread", "Runde beendet!");
        });
        currentThread.start();
    }

    public void stopTimer(){
        if(currentThread != null) {
            currentThread.interrupt();
            currentThread = null;
        }
    }

    public boolean isRunning(){
        if(currentThread != null) {
            return (currentThread.isAlive());
        }else{
            return false;
        }
    }

    public double getProgress() {
        return this.progress;
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    public void setCompleted(boolean value){
        this.isCompleted = value;
    }

    public long getRuntimeInSeconds() {
        return (this.runtime / 1000);
    }
}