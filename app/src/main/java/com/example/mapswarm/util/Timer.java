package com.example.mapswarm.util;

import android.os.CountDownTimer;
import android.widget.TextView;

public class Timer {
    private CountDownTimer countDownTimer;
    private Boolean timerRunning;
    private long timeLeftInMilliseconds;
    private TextView timerTextView;

    public Timer(Boolean timerRunning, long timeLeftInMilliseconds, TextView textView) {
        this.timerRunning = timerRunning;
        this.timeLeftInMilliseconds = timeLeftInMilliseconds;
        this.timerTextView = textView;
    }

    public void startStop() {
        if (timerRunning) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMilliseconds = l;
                int minutes = (int) timeLeftInMilliseconds/60000;
                int seconds = (int) timeLeftInMilliseconds % 60000 /1000;
                String timeLeftText;
                timeLeftText = "" + minutes;
                timeLeftText += ":";
                if (seconds < 10) timeLeftText += "0";
                timeLeftText += seconds;
                timerTextView.setText(timeLeftText);
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }
}
