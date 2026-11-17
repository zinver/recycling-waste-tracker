package com.recycletracker.logic;

import com.recycletracker.model.DropOffResult;
import com.recycletracker.model.RecyclingCenter;
import com.recycletracker.model.User;
import com.recycletracker.model.UserFactory;
import com.recycletracker.model.WasteFactory;
import com.recycletracker.model.WasteItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The simulation engine. Holds the users (HashMap) and recycling centres
 * (ArrayList), routes logged waste to centres, and builds leaderboards and
 * reports. The GUI only talks to this class, never to the data directly.
 */
public class RecyclingService {

    public static final String CENTRAL = "Green Point Central";
    public static final String EWASTE_HUB = "Eco E-Waste Hub";
    public static final String COMPOST = "Community Compost Yard";
    public static final String DEPOT = "Paper & Glass Depot";

    private static final Pattern ID_PATTERN = Pattern.compile("U(\\d+)");

    private final HashMap<String, User> users = new HashMap<>();
    private final List<RecyclingCenter> centers = new ArrayList<>();
    private int idCounter = 0;

    public RecyclingService() {
        centers.add(new RecyclingCenter(CENTRAL, 500, "Plastic", "Paper", "Glass"));
        centers.add(new RecyclingCenter(EWASTE_HUB, 150, "E-Waste"));
        centers.add(new RecyclingCenter(COMPOST, 300, "Organic"));
        centers.add(new RecyclingCenter(DEPOT, 400, "Paper", "Glass"));
    }

    // ---------------------------------------------------------------- users

    public User createUser(String type, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        User user = UserFactory.create(type, String.format("U%03d", idCounter + 1), name);
        registerUser(user);
        return user;
    }

    /** Adds an existing user object (used when loading from file). */
    public void registerUser(User user) {
        users.put(user.getId(), user);
        Matcher m = ID_PATTERN.matcher(user.getId());
        if (m.matches()) {
            idCounter = Math.max(idCounter, Integer.parseInt(m.group(1)));
        }
    }

    public void clearUsers() {
        users.clear();
        idCounter = 0;
    }

    public User getUser(String id) {
        return users.get(id);
    }

    /** @return all users sorted by id so the GUI order is stable. */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>(users.values());
        list.sort((a, b) -> a.getId().compareTo(b.getId()));
        return list;
    }

    public List<User> getLeaderboard() {
        List<User> list = new ArrayList<>(users.values());
        list.sort((a, b) -> {
            int cmp = Integer.compare(b.calculatePoints(), a.calculatePoints());
            return cmp != 0 ? cmp : a.getName().compareToIgnoreCase(b.getName());
        });
        return list;
    }

    // -------------------------------------------------------------- centres

    public List<RecyclingCenter> getCenters() {
        return centers;
    }

    public RecyclingCenter findCenter(String name) {
        for (RecyclingCenter c : centers) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    /** @return kg removed from the centre. */
    public double collectFromCenter(String name) {
        RecyclingCenter c = findCenter(name);
        if (c == null) {
            throw new IllegalArgumentException("Unknown centre: " + name);
        }
        return c.collect();
    }

    // --------------------------------------------------------- simulation

    /**
     * Logs one item for a user at a centre and decides what happens:
     * landfill waste is just recorded, contaminated items are penalised,
     * and other items are accepted or rejected by the centre.
     * Items rejected for type or capacity are NOT recorded, so the user can
     * try another centre.
     */
    public LogResult logWaste(String userId, WasteItem item, String centerName) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Unknown user: " + userId);
        }
        RecyclingCenter center = findCenter(centerName);
        if (center == null) {
            throw new IllegalArgumentException("Unknown centre: " + centerName);
        }

        int before = user.calculatePoints();
        DropOffResult status = item.isRecyclable() ? center.accept(item) : DropOffResult.LANDFILL;

        boolean record = status == DropOffResult.ACCEPTED
                || status == DropOffResult.REJECTED_CONTAMINATED
                || status == DropOffResult.LANDFILL;
        if (record) {
            user.addItem(item);
        }

        int delta = user.calculatePoints() - before;
        double co2 = status == DropOffResult.ACCEPTED ? item.calculateCO2Saved() : 0.0;

        StringBuilder msg = new StringBuilder(status.getDescription());
        if (status == DropOffResult.ACCEPTED) {
            msg.append(String.format(": %s -> %s. %+d points, %.2f kg CO2e saved.",
                    item, center.getName(), delta, co2));
        } else if (status == DropOffResult.REJECTED_CONTAMINATED) {
            msg.append(String.format(". %+d points. Tip: %s", delta, item.getDisposalTip()));
        } else if (status == DropOffResult.LANDFILL) {
            msg.append(". Tip: ").append(item.getDisposalTip());
        } else {
            msg.append(". Try a different centre.");
        }
        return new LogResult(status, delta, co2, msg.toString());
    }

    // ------------------------------------------------------------ analysis

    /** Weight per category (non-contaminated items). Pass null for all users. */
    public Map<String, Double> weightByCategory(User onlyUser) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (String cat : WasteFactory.CATEGORIES) {
            map.put(cat, 0.0);
        }
        List<User> source = onlyUser == null ? getAllUsers() : List.of(onlyUser);
        for (User u : source) {
            for (WasteItem item : u.getHistory()) {
                if (!item.isContaminated()) {
                    map.merge(item.getCategory(), item.getWeightKg(), Double::sum);
                }
            }
        }
        return map;
    }

    public String buildReport() {
        List<User> all = getAllUsers();
        int itemCount = 0;
        double recycled = 0;
        double co2 = 0;
        double notRecycled = 0;
        int goalsMet = 0;
        for (User u : all) {
            itemCount += u.getHistory().size();
            recycled += u.getRecycledWeightKg();
            co2 += u.getTotalCO2Saved();
            if (u.hasMetMonthlyGoal()) {
                goalsMet++;
            }
            for (WasteItem item : u.getHistory()) {
                if (!item.isRecyclable() || item.isContaminated()) {
                    notRecycled += item.getWeightKg();
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("RECYCLING & WASTE TRACKER - COMMUNITY REPORT (SDG 12)\n");
        sb.append("Generated: ").append(LocalDate.now()).append("\n");
        sb.append("=".repeat(54)).append("\n\n");
        sb.append(String.format("Users registered       : %d%n", all.size()));
        sb.append(String.format("Items logged           : %d%n", itemCount));
        sb.append(String.format("Total recycled         : %.1f kg%n", recycled));
        sb.append(String.format("Not recycled (landfill / contaminated): %.1f kg%n", notRecycled));
        sb.append(String.format("CO2e saved             : %.1f kg%n", co2));
        sb.append(String.format("Monthly goals met      : %d of %d users%n%n", goalsMet, all.size()));

        sb.append("Weight by category (kg)\n");
        for (Map.Entry<String, Double> e : weightByCategory(null).entrySet()) {
            sb.append(String.format("  %-10s %8.1f%n", e.getKey(), e.getValue()));
        }

        sb.append("\nLeaderboard (top 3)\n");
        List<User> board = getLeaderboard();
        for (int i = 0; i < Math.min(3, board.size()); i++) {
            User u = board.get(i);
            sb.append(String.format("  %d. %-22s %5d pts%n", i + 1, u.getName(), u.calculatePoints()));
        }

        sb.append("\nRecycling centres\n");
        for (RecyclingCenter c : centers) {
            sb.append(String.format("  %-24s %6.1f / %.0f kg (%.0f%% full)%n",
                    c.getName(), c.getCurrentLoadKg(), c.getCapacityKg(), c.getFillPercent()));
        }
        return sb.toString();
    }

    // ---------------------------------------------------------- demo data

    /** Fills the app with sample data so the first launch is not empty. */
    public void seedDemoData() {
        User aisyah = createUser("Household", "Aisyah Rahman");
        User daniel = createUser("Household", "Daniel Tan");
        User cafe = createUser("Business", "GreenLeaf Cafe");

        seed(aisyah, "Plastic", 4.5, false, 3, CENTRAL);
        seed(aisyah, "Paper", 7.0, false, 2, DEPOT);
        seed(aisyah, "Glass", 3.2, false, 2, DEPOT);
        seed(aisyah, "Organic", 5.0, false, 1, COMPOST);
        seed(aisyah, "E-Waste", 2.1, false, 0, EWASTE_HUB);

        seed(daniel, "Plastic", 2.0, false, 3, CENTRAL);
        seed(daniel, "Paper", 3.5, false, 1, CENTRAL);
        seed(daniel, "General", 6.0, false, 1, CENTRAL);
        seed(daniel, "Glass", 1.5, true, 0, DEPOT);

        seed(cafe, "Plastic", 60.0, false, 3, CENTRAL);
        seed(cafe, "Paper", 90.0, false, 2, DEPOT);
        seed(cafe, "Glass", 40.0, false, 1, DEPOT);
        seed(cafe, "Organic", 30.0, false, 1, COMPOST);
        seed(cafe, "Plastic", 3.0, true, 0, CENTRAL);
    }

    private void seed(User user, String category, double kg, boolean contaminated, int daysAgo, String center) {
        WasteItem item = WasteFactory.create(category, kg, contaminated, LocalDate.now().minusDays(daysAgo));
        logWaste(user.getId(), item, center);
    }
}