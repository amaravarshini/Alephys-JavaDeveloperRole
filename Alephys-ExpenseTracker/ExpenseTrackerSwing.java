import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExpenseTrackerSwing extends JFrame {
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final java.util.List<Transaction> transactions = new ArrayList<>();

    private final JComboBox<String> typeCombo;
    private final JComboBox<String> categoryCombo;
    private final JTextField amountField;
    private final JTextField dateField;

    public ExpenseTrackerSwing() {
        setTitle("Expense Tracker");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Panel for input
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 10, 5));
        typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        categoryCombo = new JComboBox<>();
        updateCategories();

        typeCombo.addActionListener(e -> updateCategories());

        amountField = new JTextField();
        dateField = new JTextField(LocalDate.now().toString());

        JButton addButton = new JButton("Add Transaction");

        inputPanel.add(new JLabel("Type"));
        inputPanel.add(new JLabel("Category"));
        inputPanel.add(new JLabel("Amount (₹)"));
        inputPanel.add(new JLabel("Date (yyyy-MM-dd)"));
        inputPanel.add(new JLabel());

        inputPanel.add(typeCombo);
        inputPanel.add(categoryCombo);
        inputPanel.add(amountField);
        inputPanel.add(dateField);
        inputPanel.add(addButton);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Type", "Category", "Amount", "Date"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Bottom Panel for actions
        JPanel actionPanel = new JPanel();
        JButton saveButton = new JButton("Save to File");
        JButton loadButton = new JButton("Load from File");
        JButton summaryButton = new JButton("View Monthly Summary");

        actionPanel.add(saveButton);
        actionPanel.add(loadButton);
        actionPanel.add(summaryButton);

        // Add to frame
        setLayout(new BorderLayout(10, 10));
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        // Add transaction action
        addButton.addActionListener(e -> addTransaction());

        saveButton.addActionListener(e -> saveToFile());
        loadButton.addActionListener(e -> loadFromFile());
        summaryButton.addActionListener(e -> showSummaryDialog());
    }

    private void updateCategories() {
        categoryCombo.removeAllItems();
        String type = (String) typeCombo.getSelectedItem();
        if (type.equals("Income")) {
            categoryCombo.addItem("Salary");
            categoryCombo.addItem("Business");
            categoryCombo.addItem("Freelance");
            categoryCombo.addItem("Other");
        } else {
            categoryCombo.addItem("Food");
            categoryCombo.addItem("Rent");
            categoryCombo.addItem("Travel");
            categoryCombo.addItem("Entertainment");
            categoryCombo.addItem("Utilities");
            categoryCombo.addItem("Other");
        }
    }

    private void addTransaction() {
        try {
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText());
            LocalDate date = LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            Transaction t = new Transaction(type, category, amount, date);
            transactions.add(t);

            tableModel.addRow(new Object[]{t.type, t.category, t.amount, t.date});
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check the fields.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        amountField.setText("");
        dateField.setText(LocalDate.now().toString());
    }

    private void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (Transaction t : transactions) {
                    writer.println(t);
                }
                JOptionPane.showMessageDialog(this, "Data saved successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save: " + e.getMessage());
            }
        }
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                transactions.clear();
                tableModel.setRowCount(0);
                String line;
                while ((line = reader.readLine()) != null) {
                    Transaction t = Transaction.fromString(line);
                    transactions.add(t);
                    tableModel.addRow(new Object[]{t.type, t.category, t.amount, t.date});
                }
                JOptionPane.showMessageDialog(this, "Data loaded successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to load: " + e.getMessage());
            }
        }
    }

    private void showSummaryDialog() {
        Map<String, Double> incomeMap = new HashMap<>();
        Map<String, Double> expenseMap = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.date.getMonth() + " " + t.date.getYear();
            if (t.type.equals("Income")) {
                incomeMap.put(key, incomeMap.getOrDefault(key, 0.0) + t.amount);
            } else {
                expenseMap.put(key, expenseMap.getOrDefault(key, 0.0) + t.amount);
            }
        }

        StringBuilder sb = new StringBuilder();
        Set<String> months = new TreeSet<>();
        months.addAll(incomeMap.keySet());
        months.addAll(expenseMap.keySet());

        for (String month : months) {
            double income = incomeMap.getOrDefault(month, 0.0);
            double expense = expenseMap.getOrDefault(month, 0.0);
            sb.append("📅 ").append(month).append("\n")
              .append("   ➕ Income: ₹").append(String.format("%.2f", income)).append("\n")
              .append("   ➖ Expense: ₹").append(String.format("%.2f", expense)).append("\n")
              .append("   💰 Net: ₹").append(String.format("%.2f", (income - expense))).append("\n\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Monthly Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpenseTrackerSwing().setVisible(true));
    }
}

class Transaction {
    String type;
    String category;
    double amount;
    LocalDate date;

    public Transaction(String type, String category, double amount, LocalDate date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public static Transaction fromString(String line) {
        String[] parts = line.split(",");
        return new Transaction(
            parts[0],
            parts[1],
            Double.parseDouble(parts[2]),
            LocalDate.parse(parts[3])
        );
    }

    @Override
    public String toString() {
        return type + "," + category + "," + amount + "," + date;
    }
}
