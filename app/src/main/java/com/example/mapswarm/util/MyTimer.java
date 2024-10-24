package com.example.mapswarm.util;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyTimer {

    private CountDownTimer countDownTimer;
    private Boolean timerRunning;
    private long timeLeftInMilliseconds;
    private long timeLeftInMillisecondsInStart;
    private TextView timerTextView;
    private ProgressBar progressBar;

    /**
     * Constructor for timer
     * @param timerRunning  whether the timer is running or not
     * @param timeLeftInMilliseconds remaining time
     * @param textView text view to update the time
     */
    public MyTimer(Boolean timerRunning, long timeLeftInMilliseconds, TextView textView, ProgressBar progressBar) {
        this.timerRunning = timerRunning;
        this.timeLeftInMilliseconds = timeLeftInMilliseconds;
        this.timeLeftInMillisecondsInStart = timeLeftInMilliseconds;
        this.timerTextView = textView;
        this.progressBar = progressBar;
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
        if (seconds < 10) {
            timeLeftText += "0";
        }
        timeLeftText += seconds;
        if (timerTextView != null)
            timerTextView.setText(timeLeftText);
        if (progressBar != null)
            progressBar.setProgress((int) (timeLeftInMillisecondsInStart/1000 - timeLeftInMilliseconds/1000));
    }

    /**
     * @return get the remaining time
     */
    public long getTimeLeftInMilliseconds() {
        return timeLeftInMilliseconds;
    }

    /**
     * Get the count down timer
     * @return count down timer
     */
    public CountDownTimer getCountDownTimer() {
        return countDownTimer;
    }
}
