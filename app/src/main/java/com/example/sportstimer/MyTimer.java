package com.example.sportstimer;

/**
 * Diese Klasse dient dem MyTimer-Objekt, dessen jeweilige Eigenschaften fuer die Logik der
 * Threads und Runnables wichtig sind.
 *
 * @author Heiko Heitgress
 */

public class MyTimer {
    private int seconds;
    private int rounds;
    private boolean isTimeOut;
    private int timeout;
    private int currentRound;
    private String title;

    public MyTimer(int seconds, int rounds, int timeout, int currentRound, String title){
        this.seconds = seconds;
        this.rounds = rounds;
        if(timeout > 0){
            this.isTimeOut = true;
        }else {
            this.isTimeOut = false;
        }
        this.timeout = timeout;
        this.currentRound = currentRound;
        this.title = title;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getRounds() {
        return rounds;
    }

    public boolean isTimeOut() {
        return isTimeOut;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public String getTitle() {
        return title;
    }
}
