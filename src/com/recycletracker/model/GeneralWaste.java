package com.recycletracker.model;

import java.time.LocalDate;

/** Non-recyclable waste that ends up in landfill. Tracked so users see the full picture. */
public class GeneralWaste extends WasteItem {

    public GeneralWaste(double weightKg, boolean contaminated, LocalDate date) {
        super(weightKg, contaminated, date);
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public double getCO2FactorPerKg() {
        return 0.0;
    }

    @Override
    public int getPointsPerKg() {
        return 0;
    }

    @Override
    public boolean isRecyclable() {
        return false;
    }

    /** Overridden: landfill waste neither earns points nor triggers penalties. */
    @Override
    public int calculatePoints() {
        return 0;
    }

    @Override
    public String getDisposalTip() {
        return "Landfill-bound. Try to reduce, reuse or repair before throwing away.";
    }
}