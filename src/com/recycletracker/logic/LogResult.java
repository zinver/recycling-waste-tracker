package com.recycletracker.logic;

import com.recycletracker.model.DropOffResult;

/** Immutable summary of what happened when a user logged an item. */
public class LogResult {

    private final DropOffResult status;
    private final int pointsDelta;
    private final double co2Saved;
    private final String message;

    public LogResult(DropOffResult status, int pointsDelta, double co2Saved, String message) {
        this.status = status;
        this.pointsDelta = pointsDelta;
        this.co2Saved = co2Saved;
        this.message = message;
    }

    public DropOffResult getStatus() {
        return status;
    }

    public int getPointsDelta() {
        return pointsDelta;
    }

    public double getCo2Saved() {
        return co2Saved;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}