package com.recycletracker.model;

/** Reward tiers a user can reach, based on total points. */
public enum Tier {
    NONE("No tier yet", 0),
    BRONZE("Bronze", 100),
    SILVER("Silver", 300),
    GOLD("Gold", 600);

    private final String label;
    private final int minPoints;

    Tier(String label, int minPoints) {
        this.label = label;
        this.minPoints = minPoints;
    }

    public String getLabel() {
        return label;
    }

    public int getMinPoints() {
        return minPoints;
    }

    /** @return the highest tier whose threshold the given points reach. */
    public static Tier forPoints(int points) {
        Tier result = NONE;
        for (Tier t : values()) {
            if (points >= t.minPoints) {
                result = t;
            }
        }
        return result;
    }

    /** @return the next tier up, or null if this is already the top tier. */
    public Tier next() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i] : null;
    }
}