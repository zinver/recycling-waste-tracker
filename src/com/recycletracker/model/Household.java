package com.recycletracker.model;

/** A family or individual home. Smaller volumes, standard rewards. */
public class Household extends User {

    public Household(String id, String name) {
        super(id, name);
    }

    @Override
    public String getUserType() {
        return "Household";
    }

    @Override
    public double getRewardMultiplier() {
        return 1.0;
    }

    @Override
    public double getMonthlyTargetKg() {
        return 20.0;
    }

    @Override
    public int getGoalBonusPoints() {
        return 25;
    }
}