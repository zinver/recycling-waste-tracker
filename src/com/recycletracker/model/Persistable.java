package com.recycletracker.model;

/**
 * Abstraction for objects that can be written to a CSV line for file
 * persistence. Reading back is handled by the matching factory class.
 */
public interface Persistable {

    /** @return a single comma-separated line describing this object. */
    String toCSV();
}