package com.recycletracker.logic;

import com.recycletracker.model.Tier;
import com.recycletracker.model.User;

/** Turns raw points into tiers, progress messages and monthly goal status. */
public class RewardSystem {

    public Tier getTier(int points) {
        return Tier.forPoints(points);
    }

    /** @return points still needed for the next tier, or 0 at the top tier. */
    public int pointsToNextTier(int points) {
        Tier next = getTier(points).next();
        return next == null ? 0 : next.getMinPoints() - points;
    }

    public String describeProgress(int points) {
        Tier next = getTier(points).next();
        if (next == null) {
            return "Top tier reached!";
        }
        return pointsToNextTier(points) + " points to " + next.getLabel();
    }

    /** Decision logic for the monthly recycling goal. */
    public String monthlyGoalStatus(User user) {
        double done = user.getCurrentMonthWeightKg();
        double target = user.getMonthlyTargetKg();
        if (user.hasMetMonthlyGoal()) {
            return "Goal met! +" + user.getGoalBonusPoints() + " bonus points";
        }
        return String.format("%.1f kg to go this month", target - done);
    }
}