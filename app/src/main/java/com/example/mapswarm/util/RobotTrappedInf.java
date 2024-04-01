package com.example.mapswarm.util;

public class RobotTrappedInf {

    private double[] previousPosition;
    private long lastRecordedTime;
    private boolean isTrapped;

    public RobotTrappedInf(double[] previousPosition, long lastRecordedTime) {
        this.previousPosition = previousPosition;
        this.lastRecordedTime = lastRecordedTime;
    }

    public double[] getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(double[] previousPosition) {
        this.previousPosition = previousPosition;
    }

    public long getLastRecordedTime() {
        return lastRecordedTime;
    }

    public void setLastRecordedTime(long lastRecordedTime) {
        this.lastRecordedTime = lastRecordedTime;
    }

    public boolean isTrapped() {
        return isTrapped;
    }

    public void setTrapped(boolean trapped) {
        isTrapped = trapped;
    }
}
