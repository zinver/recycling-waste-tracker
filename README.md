# Recycling & Waste Tracker (SDG 12)

Java Swing application for **BIT1123 Object Oriented Programming - Final Project (Group)**, City University Malaysia.

**SDG 12: Responsible Consumption and Production.** Households and businesses log the waste they drop off at recycling centres. The app decides whether each drop-off is accepted, calculates reward points and CO2e saved, tracks monthly goals, and ranks users on a leaderboard.

## Group members

| Name | Student ID | Programme |
|---|---|---|
| Ahmed Fardin | 202502010015 | DIT |
| Sharma Ruder | 202501010273 | DIT |
| Hossain Sohag | 202407010084 | BIT |
| Islam Kamrul | 202509010044 | BIT |

Lecturer: Sir Nazmirul Izzad bin Nassir

## How to run

You need **JDK 17 or newer** installed. Check with `java -version` and `javac -version`.

**Windows:** open the project folder and double-click `run.bat` (or run it from Command Prompt).

**macOS / Linux:** open a terminal in the project folder and run:

```bash
bash run.sh
```

**Manually (any system):**

```bash
javac -d out $(find src -name "*.java")
java -cp out com.recycletracker.ui.MainApp
```

On first launch the app creates a `data/` folder with three demo users. Data is saved automatically after every change and when the window closes.

## What you can do in the app

- **Dashboard:** points, tier, monthly goal progress, weight per category and the item history.
- **Log Waste:** choose a user, waste type, weight and recycling centre, and see the result straight away.
- **Leaderboard:** all users ranked by points.
- **Recycling Centres:** capacity and current load of each centre, with a Collect waste button.
- **Report:** community totals, which can be exported to `report.txt`.

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

- Centres accept only certain categories and have a capacity. Items can be accepted or rejected (wrong type, centre full).
- Contaminated items are rejected and penalised (glass costs more than plastic).
- Non-recyclable general waste is recorded as landfill.
- Business accounts get a 1.25x multiplier, a bigger monthly target (200 kg) and a bigger goal bonus.
- Tiers: Bronze 100, Silver 300, Gold 600 points.

## Tests

```bash
javac -d out $(find src test -name "*.java")
java -cp out com.recycletracker.LogicCheck check-data
```

All 24 checks should pass and the run ends with `All checks passed.`

## Notes

- The CO2e factors in the waste classes are illustrative values chosen for the simulation. They are not audited emission figures.
