package com.recycletracker;

import com.recycletracker.io.FileManager;
import com.recycletracker.logic.LogResult;
import com.recycletracker.logic.RecyclingService;
import com.recycletracker.model.DropOffResult;
import com.recycletracker.model.GlassWaste;
import com.recycletracker.model.Tier;
import com.recycletracker.model.User;
import com.recycletracker.model.WasteFactory;
import com.recycletracker.model.WasteItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Headless sanity checks for the logic layer (no JUnit needed).
 * Usage: java com.recycletracker.LogicCheck <temp-data-dir>
 */
public class LogicCheck {

    private static int failures = 0;

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) {
            failures++;
        }
    }

    public static void main(String[] args) throws Exception {
        String dir = args.length > 0 ? args[0] : "check-data";
        LocalDate today = LocalDate.now();

        // Runtime polymorphism through WasteItem references
        List<WasteItem> items = new ArrayList<>();
        for (String cat : WasteFactory.CATEGORIES) {
            items.add(WasteFactory.create(cat, 2.0, false, today));
        }
        check("Plastic 2kg = 20 pts", items.get(0).calculatePoints() == 20);
        check("E-Waste 2kg = 30 + 10 bonus", items.get(4).calculatePoints() == 40);
        check("General waste earns 0 pts and 0 CO2",
                items.get(5).calculatePoints() == 0 && items.get(5).calculateCO2Saved() == 0);
        check("Glass contamination penalty is -8 (override)",
                new GlassWaste(1, true, today).calculatePoints() == -8);
        check("Plastic contamination penalty is -5",
                WasteFactory.create("Plastic", 1, true, today).calculatePoints() == -5);

        // Service decisions
        RecyclingService s = new RecyclingService();
        User house = s.createUser("Household", "Test Home");
        User biz = s.createUser("Business", "Test Biz");

        LogResult r = s.logWaste(house.getId(), WasteFactory.create("Plastic", 10, false, today), RecyclingService.CENTRAL);
        check("Plastic accepted at central", r.getStatus() == DropOffResult.ACCEPTED && r.getPointsDelta() > 0);

        r = s.logWaste(house.getId(), WasteFactory.create("Organic", 1, false, today), RecyclingService.CENTRAL);
        check("Organic rejected at central (type)", r.getStatus() == DropOffResult.REJECTED_TYPE);
        check("Rejected item not recorded", house.getHistory().size() == 1);

        r = s.logWaste(house.getId(), WasteFactory.create("E-Waste", 500, false, today), RecyclingService.EWASTE_HUB);
        check("Oversized e-waste rejected (full)", r.getStatus() == DropOffResult.REJECTED_FULL);

        int before = house.calculatePoints();
        r = s.logWaste(house.getId(), WasteFactory.create("Glass", 1, true, today), RecyclingService.DEPOT);
        check("Contaminated item penalised and recorded",
                r.getStatus() == DropOffResult.REJECTED_CONTAMINATED
                        && house.calculatePoints() < before && house.getHistory().size() == 2);

        r = s.logWaste(house.getId(), WasteFactory.create("General", 3, false, today), RecyclingService.CENTRAL);
        check("General waste logged as landfill", r.getStatus() == DropOffResult.LANDFILL);

        // Same items, different user type -> different points (polymorphism on User)
        s.logWaste(biz.getId(), WasteFactory.create("Plastic", 10, false, today), RecyclingService.CENTRAL);
        check("Business multiplier 1.25 applied (100 -> 125)", biz.calculatePoints() == 125);

        // Monthly goal bonus
        s.logWaste(biz.getId(), WasteFactory.create("Paper", 200, false, today), RecyclingService.DEPOT);
        check("Business goal bonus awarded", biz.hasMetMonthlyGoal());

        // Tiers
        check("Tier thresholds", Tier.forPoints(0) == Tier.NONE && Tier.forPoints(100) == Tier.BRONZE
                && Tier.forPoints(300) == Tier.SILVER && Tier.forPoints(999) == Tier.GOLD);

        // Leaderboard order
        check("Leaderboard sorted by points",
                s.getLeaderboard().get(0).calculatePoints() >= s.getLeaderboard().get(1).calculatePoints());

        // Input validation
        boolean threw = false;
        try {
            WasteFactory.create("Plastic", -1, false, today);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("Negative weight rejected", threw);

        // File round trip
        FileManager fm = new FileManager(dir);
        fm.saveAll(s);
        RecyclingService loaded = new RecyclingService();
        check("Load reports data found", fm.loadAll(loaded));
        check("Users restored", loaded.getAllUsers().size() == 2);
        check("Points identical after reload",
                loaded.getUser(house.getId()).calculatePoints() == house.calculatePoints()
                        && loaded.getUser(biz.getId()).calculatePoints() == biz.calculatePoints());
        check("Centre load restored",
                loaded.findCenter(RecyclingService.CENTRAL).getCurrentLoadKg()
                        == s.findCenter(RecyclingService.CENTRAL).getCurrentLoadKg());
        check("New user id continues sequence", loaded.createUser("Household", "Next").getId().equals("U003"));

        // Demo seed + report
        RecyclingService demo = new RecyclingService();
        demo.seedDemoData();
        check("Demo data has 3 users", demo.getAllUsers().size() == 3);
        check("Report mentions SDG 12", demo.buildReport().contains("SDG 12"));
        check("Collect empties centre",
                demo.collectFromCenter(RecyclingService.CENTRAL) > 0
                        && demo.findCenter(RecyclingService.CENTRAL).getCurrentLoadKg() == 0);

        System.out.println(failures == 0 ? "\nAll checks passed." : "\n" + failures + " check(s) FAILED.");
        System.exit(failures == 0 ? 0 : 1);
    }
}