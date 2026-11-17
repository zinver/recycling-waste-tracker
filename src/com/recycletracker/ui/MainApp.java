package com.recycletracker.ui;

import com.recycletracker.io.FileManager;
import com.recycletracker.logic.LogResult;
import com.recycletracker.logic.RecyclingService;
import com.recycletracker.logic.RewardSystem;
import com.recycletracker.model.RecyclingCenter;
import com.recycletracker.model.Tier;
import com.recycletracker.model.User;
import com.recycletracker.model.UserFactory;
import com.recycletracker.model.WasteFactory;
import com.recycletracker.model.WasteItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swing GUI for the Recycling &amp; Waste Tracker (SDG 12).
 * Tabs: Dashboard, Log Waste, Leaderboard, Centres, Report.
 */
public class MainApp extends JFrame {

    private static final Color GREEN = new Color(46, 125, 50);

    private final RecyclingService service = new RecyclingService();
    private final FileManager fileManager = new FileManager("data");
    private final RewardSystem rewardSystem = new RewardSystem();

    private final JComboBox<User> userCombo = new JComboBox<>();
    private final JLabel statusLabel = new JLabel(" ");

    // Dashboard
    private final JLabel typeLabel = valueLabel();
    private final JLabel pointsLabel = valueLabel();
    private final JLabel tierLabel = valueLabel();
    private final JLabel nextTierLabel = valueLabel();
    private final JLabel weightLabel = valueLabel();
    private final JLabel co2Label = valueLabel();
    private final JLabel goalLabel = valueLabel();
    private final JProgressBar goalBar = new JProgressBar(0, 100);
    private final BarChartPanel chart = new BarChartPanel();
    private final DefaultTableModel historyModel = tableModel(
            "Date", "Category", "Weight (kg)", "Contaminated", "Base points", "CO2e saved (kg)");

    // Log waste
    private final JComboBox<String> categoryCombo = new JComboBox<>(WasteFactory.CATEGORIES);
    private final JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 500.0, 0.1));
    private final JCheckBox contaminatedBox = new JCheckBox("Item is contaminated (food residue, mixed materials)");
    private final JComboBox<RecyclingCenter> centerCombo = new JComboBox<>();
    private final JLabel previewLabel = new JLabel(" ");
    private final JLabel tipLabel = new JLabel(" ");
    private final JTextArea logOutput = new JTextArea(8, 40);

    // Leaderboard, centres, report
    private final DefaultTableModel boardModel = tableModel(
            "Rank", "Name", "Type", "Points", "Tier", "Recycled (kg)", "CO2e saved (kg)");
    private final DefaultTableModel centerModel = tableModel(
            "Centre", "Accepts", "Load (kg)", "Capacity (kg)", "Full");
    private final JTable centerTable = new JTable(centerModel);
    private final JTextArea reportArea = new JTextArea();

    private boolean refreshing = false;

    public MainApp() {
        super("Recycling & Waste Tracker - SDG 12");
        if (!fileManager.loadAll(service)) {
            service.seedDemoData();
            saveQuietly();
        }
        buildUi();
        refreshAll();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveQuietly();
                dispose();
                System.exit(0);
            }
        });
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ UI

    private void buildUi() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBackground(GREEN);
        JLabel title = new JLabel("Recycling & Waste Tracker");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        top.add(title);
        JLabel sdg = new JLabel("  |  SDG 12: Responsible Consumption and Production    Active user:");
        sdg.setForeground(Color.WHITE);
        top.add(sdg);
        top.add(userCombo);
        JButton addUserBtn = new JButton("+ New User");
        addUserBtn.addActionListener(e -> addUser());
        top.add(addUserBtn);
        userCombo.addActionListener(e -> {
            if (!refreshing) {
                refreshDashboard();
            }
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", buildDashboardTab());
        tabs.addTab("Log Waste", buildLogTab());
        tabs.addTab("Leaderboard", buildLeaderboardTab());
        tabs.addTab("Recycling Centres", buildCentersTab());
        tabs.addTab("Report", buildReportTab());
        tabs.addChangeListener(e -> refreshAll());

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildDashboardTab() {
        JPanel stats = new JPanel(new GridLayout(0, 2, 10, 6));
        stats.setBorder(BorderFactory.createTitledBorder("Your impact"));
        addStat(stats, "Account type", typeLabel);
        addStat(stats, "Total points", pointsLabel);
        addStat(stats, "Reward tier", tierLabel);
        addStat(stats, "Next tier", nextTierLabel);
        addStat(stats, "Total recycled", weightLabel);
        addStat(stats, "CO2e saved", co2Label);
        addStat(stats, "Monthly goal", goalLabel);
        goalBar.setStringPainted(true);
        goalBar.setForeground(GREEN);
        stats.add(new JLabel("Goal progress (this month)"));
        stats.add(goalBar);

        JTable historyTable = new JTable(historyModel);
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createTitledBorder("History (latest first)"));

        JPanel upper = new JPanel(new GridLayout(1, 2, 10, 0));
        upper.add(stats);
        upper.add(chart);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, historyScroll);
        split.setResizeWeight(0.5);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLogTab() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Log a drop-off"));
        addRow(form, 0, "Waste category", categoryCombo);
        weightSpinner.setEditor(new JSpinner.NumberEditor(weightSpinner, "0.0"));
        weightSpinner.setPreferredSize(new Dimension(100, 26));
        JPanel weightRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        weightRow.add(weightSpinner);
        addRow(form, 1, "Weight (kg)", weightRow);
        addRow(form, 2, "Condition", contaminatedBox);
        addRow(form, 3, "Recycling centre", centerCombo);
        addRow(form, 4, "Preview", previewLabel);
        addRow(form, 5, "Tip", tipLabel);

        JButton logBtn = new JButton("Log Waste");
        logBtn.addActionListener(e -> logWaste());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 6;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 6, 6, 6);
        form.add(logBtn, c);

        categoryCombo.addActionListener(e -> updatePreview());
        weightSpinner.addChangeListener(e -> updatePreview());
        contaminatedBox.addActionListener(e -> updatePreview());

        logOutput.setEditable(false);
        logOutput.setLineWrap(true);
        logOutput.setWrapStyleWord(true);
        JScrollPane outScroll = new JScrollPane(logOutput);
        outScroll.setBorder(BorderFactory.createTitledBorder("Result"));

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(form, BorderLayout.NORTH);
        panel.add(outScroll, BorderLayout.CENTER);
        updatePreview();
        return panel;
    }

    private JPanel buildLeaderboardTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(new JTable(boardModel)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCentersTab() {
        JButton collectBtn = new JButton("Collect waste (empty selected centre)");
        collectBtn.addActionListener(e -> collectSelectedCenter());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(collectBtn);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(centerTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildReportTab() {
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshAll());
        JButton exportBtn = new JButton("Export to file");
        exportBtn.addActionListener(e -> exportReport());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(refreshBtn);
        bottom.add(exportBtn);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------------------------------------------------- actions

    private void addUser() {
        JTextField nameField = new JTextField(16);
        JComboBox<String> typeBox = new JComboBox<>(UserFactory.TYPES);
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Name:"));
        p.add(nameField);
        p.add(new JLabel("Type:"));
        p.add(typeBox);
        int choice = JOptionPane.showConfirmDialog(this, p, "New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            User u = service.createUser((String) typeBox.getSelectedItem(), nameField.getText());
            saveQuietly();
            refreshAll();
            userCombo.setSelectedItem(u);
            statusLabel.setText("Created " + u);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot create user", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void logWaste() {
        User user = (User) userCombo.getSelectedItem();
        RecyclingCenter center = (RecyclingCenter) centerCombo.getSelectedItem();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Create or select a user first.");
            return;
        }
        try {
            WasteItem item = WasteFactory.create(
                    (String) categoryCombo.getSelectedItem(), currentWeight(),
                    contaminatedBox.isSelected(), LocalDate.now());
            LogResult result = service.logWaste(user.getId(), item, center.getName());
            logOutput.append(user.getName() + ": " + result.getMessage() + "\n\n");
            saveQuietly();
            refreshAll();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot log waste", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void collectSelectedCenter() {
        int row = centerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a centre in the table first.");
            return;
        }
        String name = (String) centerModel.getValueAt(row, 0);
        double removed = service.collectFromCenter(name);
        saveQuietly();
        refreshAll();
        statusLabel.setText(String.format("Collected %.1f kg from %s", removed, name));
    }

    private void exportReport() {
        try {
            File f = fileManager.exportReport(service.buildReport());
            JOptionPane.showMessageDialog(this, "Report saved to:\n" + f.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save report: " + ex.getMessage(),
                    "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveQuietly() {
        try {
            fileManager.saveAll(service);
        } catch (IOException ex) {
            statusLabel.setText("Save failed: " + ex.getMessage());
        }
    }

    private double currentWeight() {
        return ((Number) weightSpinner.getValue()).doubleValue();
    }

    private void updatePreview() {
        try {
            WasteItem item = WasteFactory.create((String) categoryCombo.getSelectedItem(),
                    currentWeight(), contaminatedBox.isSelected(), LocalDate.now());
            previewLabel.setText(String.format("%+d base points, %.2f kg CO2e saved (if accepted)",
                    item.calculatePoints(), item.calculateCO2Saved()));
            tipLabel.setText("<html>" + item.getDisposalTip() + "</html>");
        } catch (IllegalArgumentException ex) {
            previewLabel.setText(ex.getMessage());
        }
    }

    // ------------------------------------------------------------- refresh

    private void refreshAll() {
        refreshing = true;
        try {
            refreshUserCombo();
            refreshCenterCombo();
            refreshDashboard();
            refreshLeaderboard();
            refreshCenters();
            reportArea.setText(service.buildReport());
            reportArea.setCaretPosition(0);
        } finally {
            refreshing = false;
        }
    }

    private void refreshUserCombo() {
        User selected = (User) userCombo.getSelectedItem();
        userCombo.removeAllItems();
        for (User u : service.getAllUsers()) {
            userCombo.addItem(u);
            if (selected != null && u.getId().equals(selected.getId())) {
                selected = u;
            }
        }
        if (selected != null) {
            userCombo.setSelectedItem(selected);
        }
    }

    private void refreshCenterCombo() {
        RecyclingCenter selected = (RecyclingCenter) centerCombo.getSelectedItem();
        centerCombo.removeAllItems();
        for (RecyclingCenter c : service.getCenters()) {
            centerCombo.addItem(c);
        }
        if (selected != null) {
            centerCombo.setSelectedItem(service.findCenter(selected.getName()));
        }
    }

    private void refreshDashboard() {
        historyModel.setRowCount(0);
        User u = (User) userCombo.getSelectedItem();
        if (u == null) {
            chart.setData(new LinkedHashMap<>());
            return;
        }
        int points = u.calculatePoints();
        Tier tier = rewardSystem.getTier(points);
        typeLabel.setText(u.getUserType() + " (x" + u.getRewardMultiplier() + " points)");
        pointsLabel.setText(points + " pts");
        tierLabel.setText(tier.getLabel());
        nextTierLabel.setText(rewardSystem.describeProgress(points));
        weightLabel.setText(String.format("%.1f kg", u.getRecycledWeightKg()));
        co2Label.setText(String.format("%.1f kg", u.getTotalCO2Saved()));
        goalLabel.setText(rewardSystem.monthlyGoalStatus(u));
        goalBar.setValue((int) Math.round(u.getGoalProgress() * 100));
        goalBar.setString(String.format("%.1f / %.0f kg", u.getCurrentMonthWeightKg(), u.getMonthlyTargetKg()));

        List<WasteItem> history = u.getHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            WasteItem item = history.get(i);
            historyModel.addRow(new Object[]{
                item.getDate(), item.getCategory(), item.getWeightKg(),
                item.isContaminated() ? "Yes" : "No",
                item.calculatePoints(), String.format("%.2f", item.calculateCO2Saved())});
        }
        chart.setData(service.weightByCategory(u));
    }

    private void refreshLeaderboard() {
        boardModel.setRowCount(0);
        int rank = 1;
        for (User u : service.getLeaderboard()) {
            int pts = u.calculatePoints();
            boardModel.addRow(new Object[]{
                rank++, u.getName(), u.getUserType(), pts, rewardSystem.getTier(pts).getLabel(),
                String.format("%.1f", u.getRecycledWeightKg()),
                String.format("%.1f", u.getTotalCO2Saved())});
        }
    }

    private void refreshCenters() {
        int selected = centerTable.getSelectedRow();
        centerModel.setRowCount(0);
        for (RecyclingCenter c : service.getCenters()) {
            centerModel.addRow(new Object[]{
                c.getName(), String.join(", ", c.getAcceptedCategories()),
                String.format("%.1f", c.getCurrentLoadKg()), String.format("%.0f", c.getCapacityKg()),
                String.format("%.0f%%", c.getFillPercent())});
        }
        if (selected >= 0 && selected < centerModel.getRowCount()) {
            centerTable.setRowSelectionInterval(selected, selected);
        }
    }

    // ------------------------------------------------------------- helpers

    private static JLabel valueLabel() {
        JLabel l = new JLabel("-");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private static void addStat(JPanel panel, String caption, JLabel value) {
        panel.add(new JLabel(caption));
        panel.add(value);
    }

    private static void addRow(JPanel panel, int row, String caption, java.awt.Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(caption), c);
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    private static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Simple bar chart drawn with Java2D: weight recycled per category. */
    private static class BarChartPanel extends JPanel {

        private static final Color[] COLORS = {
            new Color(30, 136, 229), new Color(158, 158, 158), new Color(0, 150, 136),
            new Color(121, 85, 72), new Color(255, 160, 0), new Color(96, 125, 139)
        };

        private Map<String, Double> data = new LinkedHashMap<>();

        BarChartPanel() {
            setBorder(BorderFactory.createTitledBorder("Weight by category (kg)"));
            setPreferredSize(new Dimension(400, 220));
        }

        void setData(Map<String, Double> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double max = 0;
            for (double v : data.values()) {
                max = Math.max(max, v);
            }
            int w = getWidth();
            int h = getHeight();
            if (max <= 0) {
                g2.drawString("No data yet", w / 2 - 30, h / 2);
                g2.dispose();
                return;
            }

            int left = 15;
            int top = 40;
            int bottom = 28;
            int slot = (w - left * 2) / data.size();
            int barW = Math.max(12, slot - 14);
            int i = 0;
            for (Map.Entry<String, Double> e : data.entrySet()) {
                int barH = (int) ((h - top - bottom) * (e.getValue() / max));
                int x = left + i * slot + 7;
                int y = h - bottom - barH;
                g2.setColor(COLORS[i % COLORS.length]);
                g2.fillRect(x, y, barW, barH);
                g2.setColor(getForeground());
                g2.drawString(String.format("%.1f", e.getValue()), x, y - 4);
                g2.drawString(e.getKey(), x, h - bottom + 16);
                i++;
            }
            g2.drawLine(left, h - bottom, w - left, h - bottom);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------- main

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }
            new MainApp().setVisible(true);
        });
    }
}