package com.recycletracker.model;

import java.time.LocalDate;

/** Creates the correct {@link WasteItem} subclass from a category name or CSV line. */
public final class WasteFactory {

    public static final String[] CATEGORIES = {
        "Plastic", "Paper", "Glass", "Organic", "E-Waste", "General"
    };

    private WasteFactory() {
    }

    /** Returns a WasteItem reference; the real type depends on the category. */
    public static WasteItem create(String category, double weightKg, boolean contaminated, LocalDate date) {
        switch (category) {
            case "Plastic":
                return new PlasticWaste(weightKg, contaminated, date);
            case "Paper":
                return new PaperWaste(weightKg, contaminated, date);
            case "Glass":
                return new GlassWaste(weightKg, contaminated, date);
            case "Organic":
                return new OrganicWaste(weightKg, contaminated, date);
            case "E-Waste":
                return new EWaste(weightKg, contaminated, date);
            case "General":
                return new GeneralWaste(weightKg, contaminated, date);
            default:
                throw new IllegalArgumentException("Unknown waste category: " + category);
        }
    }

    /** Rebuilds an item from a line produced by {@link WasteItem#toCSV()}. */
    public static WasteItem fromCSV(String line) {
        String[] p = line.split(",");
        if (p.length < 4) {
            throw new IllegalArgumentException("Bad item line: " + line);
        }
        return create(p[0].trim(), Double.parseDouble(p[1]), Boolean.parseBoolean(p[2]),
                LocalDate.parse(p[3].trim()));
    }
}