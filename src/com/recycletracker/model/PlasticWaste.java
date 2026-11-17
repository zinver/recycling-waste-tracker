package com.recycletracker.model;

import java.time.LocalDate;

/** Plastic bottles, containers and packaging. */
public class PlasticWaste extends WasteItem {

    public PlasticWaste(double weightKg, boolean contaminated, LocalDate date) {
        super(weightKg, contaminated, date);
    }

    @Override
    public String getCategory() {
        return "Plastic";
    }

    // NOTE: emission factors in this project are illustrative placeholders.
    // Replace them with cited figures (e.g. a national or EPA WARM dataset)
    // and reference the source in the report.
    @Override
    public double getCO2FactorPerKg() {
        return 1.5;
    }

    @Override
    public int getPointsPerKg() {
        return 10;
    }

    @Override
    public boolean isRecyclable() {
        return true;
    }

    @Override
    public String getDisposalTip() {
        return "Rinse containers and remove caps and labels before recycling.";
    }
}