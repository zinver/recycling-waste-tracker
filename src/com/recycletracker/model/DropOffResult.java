package com.recycletracker.model;

/** Outcome of trying to drop a waste item at a recycling centre. */
public enum DropOffResult {
    ACCEPTED("Accepted for recycling"),
    REJECTED_TYPE("Rejected: this centre does not accept that waste type"),
    REJECTED_FULL("Rejected: the centre does not have enough capacity"),
    REJECTED_CONTAMINATED("Rejected: item is contaminated (penalty applied)"),
    LANDFILL("Recorded as landfill waste (not recyclable)");

    private final String description;

    DropOffResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}