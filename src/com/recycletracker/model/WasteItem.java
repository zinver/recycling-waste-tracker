package com.recycletracker.model;

import java.time.LocalDate;

/**
 * Abstract base class for every kind of waste a user can log.
 *
 * Subclasses supply the category-specific numbers (CO2 factor, points per kg,
 * recyclability, disposal tip) and may override the scoring methods, so code
 * that works with a {@code WasteItem} reference behaves differently depending
 * on the real object (runtime polymorphism).
 */
public abstract class WasteItem implements Rewardable, Persistable {

    private final double weightKg;
    private final boolean contaminated;
    private final LocalDate date;

    protected WasteItem(double weightKg, boolean contaminated, LocalDate date) {
        if (weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0 kg.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        this.weightKg = weightKg;
        this.contaminated = contaminated;
        this.date = date;
    }

    // ----- encapsulated state (getters only: items are immutable once logged) -----

    public double getWeightKg() {
        return weightKg;
    }

    public boolean isContaminated() {
        return contaminated;
    }

    public LocalDate getDate() {
        return date;
    }

    // ----- abstract behaviour each waste type must define -----

    /** @return display name of the category, e.g. "Plastic". */
    public abstract String getCategory();

    /** @return kg of CO2-equivalent saved per kg recycled. */
    public abstract double getCO2FactorPerKg();

    /** @return base reward points per kg when recycled correctly. */
    public abstract int getPointsPerKg();

    /** @return true if this waste can be recycled or composted. */
    public abstract boolean isRecyclable();

    /** @return a short, practical tip for disposing of this waste. */
    public abstract String getDisposalTip();

    // ----- shared behaviour (some subclasses override) -----

    /** @return points deducted when a contaminated item is logged. */
    public int getContaminationPenalty() {
        return 5;
    }

    /** @return kg CO2e saved; zero if contaminated or not recyclable. */
    public double calculateCO2Saved() {
        if (!isRecyclable() || contaminated) {
            return 0.0;
        }
        return weightKg * getCO2FactorPerKg();
    }

    @Override
    public int calculatePoints() {
        if (contaminated) {
            return -getContaminationPenalty();
        }
        if (!isRecyclable()) {
            return 0;
        }
        return (int) Math.round(weightKg * getPointsPerKg());
    }

    @Override
    public String toCSV() {
        return getCategory() + "," + weightKg + "," + contaminated + "," + date;
    }

    @Override
    public String toString() {
        return getCategory() + " " + weightKg + " kg" + (contaminated ? " (contaminated)" : "");
    }
}