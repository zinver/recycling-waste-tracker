package com.recycletracker.model;

/**
 * Abstraction for anything that can earn (or lose) reward points.
 * Implemented by {@link WasteItem} (points for one item) and {@link User}
 * (total points for a person or organisation).
 */
public interface Rewardable {

    /** @return the reward points earned (negative values are penalties). */
    int calculatePoints();
}