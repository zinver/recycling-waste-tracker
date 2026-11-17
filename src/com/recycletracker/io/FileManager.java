package com.recycletracker.io;

import com.recycletracker.logic.RecyclingService;
import com.recycletracker.model.RecyclingCenter;
import com.recycletracker.model.User;
import com.recycletracker.model.UserFactory;
import com.recycletracker.model.WasteFactory;
import com.recycletracker.model.WasteItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Saves and loads the application state as three CSV files inside a data folder:
 * users.csv, items.csv and centers.csv. Also exports the text report.
 */
public class FileManager {

    private final File dir;

    public FileManager(String dirPath) {
        this.dir = new File(dirPath);
    }

    public File getDirectory() {
        return dir;
    }

    public void saveAll(RecyclingService service) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create data folder: " + dir.getAbsolutePath());
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(dir, "users.csv")))) {
            for (User u : service.getAllUsers()) {
                w.write(u.toCSV());
                w.newLine();
            }
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(dir, "items.csv")))) {
            for (User u : service.getAllUsers()) {
                for (WasteItem item : u.getHistory()) {
                    w.write(u.getId() + "," + item.toCSV());
                    w.newLine();
                }
            }
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(dir, "centers.csv")))) {
            for (RecyclingCenter c : service.getCenters()) {
                w.write(c.getName() + "," + c.getCurrentLoadKg());
                w.newLine();
            }
        }
    }

    /**
     * Loads saved data into the service.
     *
     * @return true if at least one user was loaded; false if there is no saved
     *         data (so the caller can seed demo data instead).
     */
    public boolean loadAll(RecyclingService service) {
        File usersFile = new File(dir, "users.csv");
        if (!usersFile.exists()) {
            return false;
        }
        service.clearUsers();
        try {
            readUsers(usersFile, service);
            readItems(new File(dir, "items.csv"), service);
            readCenters(new File(dir, "centers.csv"), service);
        } catch (IOException e) {
            System.err.println("Could not read saved data: " + e.getMessage());
            service.clearUsers();
            return false;
        }
        return !service.getAllUsers().isEmpty();
    }

    private void readUsers(File file, RecyclingService service) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    String[] p = line.split(",", 3);
                    service.registerUser(UserFactory.create(p[0].trim(), p[1].trim(), p[2]));
                } catch (RuntimeException e) {
                    System.err.println("Skipping bad user line: " + line);
                }
            }
        }
    }

    private void readItems(File file, RecyclingService service) throws IOException {
        if (!file.exists()) {
            return;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    String[] p = line.split(",", 2);
                    User user = service.getUser(p[0].trim());
                    if (user != null) {
                        user.addItem(WasteFactory.fromCSV(p[1]));
                    }
                } catch (RuntimeException e) {
                    System.err.println("Skipping bad item line: " + line);
                }
            }
        }
    }

    private void readCenters(File file, RecyclingService service) throws IOException {
        if (!file.exists()) {
            return;
        }
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    int comma = line.lastIndexOf(',');
                    RecyclingCenter c = service.findCenter(line.substring(0, comma));
                    if (c != null) {
                        c.setCurrentLoadKg(Double.parseDouble(line.substring(comma + 1)));
                    }
                } catch (RuntimeException e) {
                    System.err.println("Skipping bad centre line: " + line);
                }
            }
        }
    }

    /** Writes the report text to data/report.txt. @return the file written. */
    public File exportReport(String text) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create data folder: " + dir.getAbsolutePath());
        }
        File out = new File(dir, "report.txt");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
            w.write(text);
        }
        return out;
    }
}