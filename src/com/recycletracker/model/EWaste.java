package com.recycletracker.model;

import java.time.LocalDate;

/** Electronic waste: phones, cables, batteries, small appliances. */
public class EWaste extends WasteItem {

    /** Extra points for handing e-waste to a proper facility instead of the bin. */
    private static final int SAFE_HANDLING_BONUS = 10;

    public EWaste(double weightKg, boolean contaminated, LocalDate date) {
        super(weightKg, contaminated, date);
    }

    @Override
    public String getCategory() {
        return "E-Waste";
    }

    @Override
    public double getCO2FactorPerKg() {
        return 2.0;
    }

    @Override
    public int getPointsPerKg() {
        return 15;
    }

    @Override
    public boolean isRecyclable() {
        return true;
    }

    /** Overridden: adds a safe-handling bonus on top of the normal scoring. */
    @Override
    public int calculatePoints() {
        int base = super.calculatePoints();
        return isContaminated() ? base : base + SAFE_HANDLING_BONUS;
    }

    @Override
    public String getDisposalTip() {
        return "Never bin batteries or electronics; use a certified e-waste centre.";
    }
}