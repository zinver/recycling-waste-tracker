package com.recycletracker.model;

import java.time.LocalDate;

/** Paper, newspaper and cardboard. */
public class PaperWaste extends WasteItem {

    public PaperWaste(double weightKg, boolean contaminated, LocalDate date) {
        super(weightKg, contaminated, date);
    }

    @Override
    public String getCategory() {
        return "Paper";
    }

    @Override
    public double getCO2FactorPerKg() {
        return 0.9;
    }

    @Override
    public int getPointsPerKg() {
        return 6;
    }

    @Override
    public boolean isRecyclable() {
        return true;
    }

    @Override
    public String getDisposalTip() {
        return "Keep paper dry, flatten cardboard, and leave out greasy food boxes.";
    }
}