package com.example.mapswarm.util;

import android.os.CountDownTimer;
import android.widget.TextView;

public class MyTimer {
    private CountDownTimer countDownTimer;
    private Boolean timerRunning;
    private long timeLeftInMilliseconds;
    private TextView timerTextView;

    /**
     * Constructor for timer
     * @param timerRunning  whether the timer is running or not
     * @param timeLeftInMilliseconds remaining time
     * @param textView text view to update the time
     */
    public MyTimer(Boolean timerRunning, long timeLeftInMilliseconds, TextView textView) {
        this.timerRunning = timerRunning;
        this.timeLeftInMilliseconds = timeLeftInMilliseconds;
        this.timerTextView = textView;
    }

    /**
     * Start or Stop the timer
     */
    public void startStop() {
        if (timerRunning) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    /**
     * Stop the timer
     */
    private void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
    }

    /**
     * Start the timer
     */
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMilliseconds = l;
                updateTimer();
            }

            @Override
            public void onFinish() {

            }
        }.start();

        timerRunning = true;
    }

    /**
     * Update the timer
     */
    public void updateTimer() {
        int minutes = (int) timeLeftInMilliseconds/60000;
        int seconds = (int) timeLeftInMilliseconds % 60000 /1000;
        String timeLeftText;
        timeLeftText = "" + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;
        timerTextView.setText(timeLeftText);
    }

    /**
     * @return get the remaining time
     */
    public long getTimeLeftInMilliseconds() {
        return timeLeftInMilliseconds;
    }
}
