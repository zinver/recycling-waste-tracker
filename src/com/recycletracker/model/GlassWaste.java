package com.recycletracker.model;

import java.time.LocalDate;

/** Glass bottles and jars. Contamination (ceramics, broken glass) costs more. */
public class GlassWaste extends WasteItem {

    public GlassWaste(double weightKg, boolean contaminated, LocalDate date) {
        super(weightKg, contaminated, date);
    }

    @Override
    public String getCategory() {
        return "Glass";
    }

    @Override
    public double getCO2FactorPerKg() {
        return 0.3;
    }

    @Override
    public int getPointsPerKg() {
        return 8;
    }

    @Override
    public boolean isRecyclable() {
        return true;
    }

    /** Overridden: ceramics and broken glass can spoil a whole batch. */
    @Override
    public int getContaminationPenalty() {
        return 8;
    }

    @Override
    public String getDisposalTip() {
        return "Separate by colour if asked, and keep ceramics and mirrors out.";
    }
}