package com.recycletracker.model;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for anyone who logs waste. Concrete subclasses decide the
 * reward multiplier, monthly target and goal bonus, so the same call
 * ({@code user.calculatePoints()}) gives different results per user type.
 */
public abstract class User implements Rewardable, Persistable {

    private final String id;
    private String name;
    private final List<WasteItem> history = new ArrayList<>();

    protected User(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id is required.");
        }
        this.id = id;
        setName(name);
    }

    // ----- encapsulation -----

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name.trim().replace(",", " ");
    }

    /** Read-only view so outside code cannot bypass {@link #addItem}. */
    public List<WasteItem> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void addItem(WasteItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        history.add(item);
    }

    // ----- abstract behaviour per user type -----

    public abstract String getUserType();

    /** Multiplier applied to base points. */
    public abstract double getRewardMultiplier();

    /** Kilograms the user should recycle each month. */
    public abstract double getMonthlyTargetKg();

    /** Bonus points awarded when the monthly target is met. */
    public abstract int getGoalBonusPoints();

    // ----- calculations shared by all users -----

    /** Total weight that was actually recycled (recyclable and not contaminated). */
    public double getRecycledWeightKg() {
        double total = 0;
        for (WasteItem item : history) {
            if (item.isRecyclable() && !item.isContaminated()) {
                total += item.getWeightKg();
            }
        }
        return total;
    }

    public double getTotalCO2Saved() {
        double total = 0;
        for (WasteItem item : history) {
            total += item.calculateCO2Saved(); // polymorphic call
        }
        return total;
    }

    public double getMonthWeightKg(YearMonth month) {
        double total = 0;
        for (WasteItem item : history) {
            if (item.isRecyclable() && !item.isContaminated()
                    && YearMonth.from(item.getDate()).equals(month)) {
                total += item.getWeightKg();
            }
        }
        return total;
    }

    public double getCurrentMonthWeightKg() {
        return getMonthWeightKg(YearMonth.now());
    }

    public boolean hasMetMonthlyGoal() {
        return getCurrentMonthWeightKg() >= getMonthlyTargetKg();
    }

    /** @return progress towards this month's goal between 0.0 and 1.0. */
    public double getGoalProgress() {
        return Math.min(1.0, getCurrentMonthWeightKg() / getMonthlyTargetKg());
    }

    @Override
    public int calculatePoints() {
        int base = 0;
        for (WasteItem item : history) {
            base += item.calculatePoints(); // polymorphic call
        }
        int scaled = (int) Math.round(Math.max(0, base) * getRewardMultiplier());
        return scaled + (hasMetMonthlyGoal() ? getGoalBonusPoints() : 0);
    }

    @Override
    public String toCSV() {
        return getUserType() + "," + id + "," + name;
    }

    @Override
    public String toString() {
        return name + " (" + getUserType() + ")";
    }
}