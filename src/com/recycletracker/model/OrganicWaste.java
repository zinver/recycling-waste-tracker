package com.recycletracker.model;

import java.time.LocalDate;

/** Food scraps and garden waste that can be composted. */
public class OrganicWaste extends WasteItem {

    public OrganicWaste(double weightKg, boolean contaminated, LocalDate date) {
        super(weightKg, contaminated, date);
    }

    @Override
    public String getCategory() {
        return "Organic";
    }

    @Override
    public double getCO2FactorPerKg() {
        return 0.5;
    }

    @Override
    public int getPointsPerKg() {
        return 4;
    }

    @Override
    public boolean isRecyclable() {
        return true; // compostable
    }

    @Override
    public String getDisposalTip() {
        return "Compost food scraps; keep plastic bags and packaging out of the bin.";
    }
}