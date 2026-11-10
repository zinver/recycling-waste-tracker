package com.recycletracker.model;

/** A shop, cafe or office. Higher volumes, so a bigger target and a higher multiplier. */
public class Business extends User {

    public Business(String id, String name) {
        super(id, name);
    }

    @Override
    public String getUserType() {
        return "Business";
    }

    @Override
    public double getRewardMultiplier() {
        return 1.25;
    }

    @Override
    public double getMonthlyTargetKg() {
        return 200.0;
    }

    @Override
    public int getGoalBonusPoints() {
        return 100;
    }
}