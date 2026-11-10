package com.recycletracker.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A drop-off point that only accepts certain categories and has a limited
 * capacity. Accepting or rejecting an item is the main decision logic of the
 * simulation.
 */
public class RecyclingCenter {

    private final String name;
    private final Set<String> acceptedCategories = new HashSet<>();
    private final double capacityKg;
    private double currentLoadKg;

    public RecyclingCenter(String name, double capacityKg, String... categories) {
        if (capacityKg <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.name = name;
        this.capacityKg = capacityKg;
        Collections.addAll(acceptedCategories, categories);
    }

    public String getName() {
        return name;
    }

    public double getCapacityKg() {
        return capacityKg;
    }

    public double getCurrentLoadKg() {
        return currentLoadKg;
    }

    /** Used when restoring saved data; the load is clamped to a valid range. */
    public void setCurrentLoadKg(double load) {
        this.currentLoadKg = Math.max(0, Math.min(load, capacityKg));
    }

    public Set<String> getAcceptedCategories() {
        return Collections.unmodifiableSet(acceptedCategories);
    }

    public boolean accepts(String category) {
        return acceptedCategories.contains(category);
    }

    public double getFillPercent() {
        return currentLoadKg / capacityKg * 100.0;
    }

    /** Tries to take an item; the centre's load only grows when it is accepted. */
    public DropOffResult accept(WasteItem item) {
        if (item.isContaminated()) {
            return DropOffResult.REJECTED_CONTAMINATED;
        }
        if (!accepts(item.getCategory())) {
            return DropOffResult.REJECTED_TYPE;
        }
        if (currentLoadKg + item.getWeightKg() > capacityKg) {
            return DropOffResult.REJECTED_FULL;
        }
        currentLoadKg += item.getWeightKg();
        return DropOffResult.ACCEPTED;
    }

    /** Simulates a collection truck emptying the centre. @return kg removed. */
    public double collect() {
        double removed = currentLoadKg;
        currentLoadKg = 0;
        return removed;
    }

    @Override
    public String toString() {
        return name;
    }
}