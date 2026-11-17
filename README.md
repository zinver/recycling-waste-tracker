# recycling-waste-tracker
# Recycling & Waste Tracker (SDG 12)

Java Swing application for **BIT1123 Object Oriented Programming - Final Project (Group)**.

**SDG 12: Responsible Consumption and Production.** Households and businesses log
the waste they drop off at recycling centres. The app decides whether each drop-off is
accepted, calculates reward points and CO2e saved, tracks monthly goals, and ranks users
on a leaderboard.

## Run

Requires JDK 17 or newer.

```bash
# Linux / macOS
./run.sh

# Windows
run.bat
```

Or manually:

```bash
javac -d out $(find src -name "*.java")
java -cp out com.recycletracker.ui.MainApp
```

On first launch the app creates a `data/` folder with demo users. Data is saved
automatically after every change and when the window closes.

## Project structure

```
src/com/recycletracker/
  model/   Rewardable, Persistable, Tier, DropOffResult
           WasteItem (abstract) -> PlasticWaste, PaperWaste, GlassWaste,
                                   OrganicWaste, EWaste, GeneralWaste
           User (abstract)      -> Household, Business
           RecyclingCenter, WasteFactory, UserFactory
  logic/   RecyclingService (simulation engine), RewardSystem, LogResult
  io/      FileManager (CSV persistence + report export)
  ui/      MainApp (Swing GUI)
test/      LogicCheck (headless checks, no JUnit needed)
```

## OOP requirements map

| Requirement | Where |
|---|---|
| Classes and objects (6-8+) | 20+ classes in `model`, `logic`, `io`, `ui` |
| Encapsulation | private fields with getters/setters (`User.setName` validates; `User.getHistory` is read-only; `WasteItem` is immutable) |
| Inheritance | `WasteItem` -> 6 subclasses; `User` -> `Household`, `Business` |
| Polymorphism | overriding in `EWaste.calculatePoints`, `GlassWaste.getContaminationPenalty`, `GeneralWaste.calculatePoints`, and every abstract method; runtime dispatch through `WasteItem` / `User` references in `User.calculatePoints` and `RecyclingService` |
| Abstraction | abstract classes `WasteItem`, `User`; interfaces `Rewardable`, `Persistable` |
| Collections | `HashMap<String, User>`, `ArrayList<RecyclingCenter>`, `HashSet<String>` (accepted categories), `LinkedHashMap` (category totals) |
| File handling | `FileManager` reads/writes `users.csv`, `items.csv`, `centers.csv`; exports `report.txt` |

## Simulation logic

- Centres accept only certain categories and have a capacity; items can be accepted or rejected (wrong type, centre full).
- Contaminated items are rejected and penalised (glass costs more than plastic).
- Non-recyclable general waste is recorded as landfill.
- Business accounts get a 1.25x multiplier, a bigger monthly target (200 kg) and a bigger goal bonus.
- Tiers: Bronze 100, Silver 300, Gold 600 points.

## Tests

```bash
javac -d out $(find src test -name "*.java")
java -cp out com.recycletracker.LogicCheck check-data
```

## Before submitting

- The CO2 factors in the `WasteItem` subclasses are **illustrative placeholders**. Replace them with figures from a cited source and reference it in the report.
- Add group member names and student IDs here.
- Attach the signed Student AI Declaration (AI use level 2).