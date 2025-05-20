import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Transaction {
    enum Type { INCOME, EXPENSE }

    private Type type;
    private String category;
    private double amount;
    private LocalDate date;

    public Transaction(Type type, String category, double amount, LocalDate date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public Type getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return type + "," + category + "," + amount + "," + date;
    }

    public static Transaction fromString(String line) {
        String[] parts = line.split(",");
        return new Transaction(
            Type.valueOf(parts[0]),
            parts[1],
            Double.parseDouble(parts[2]),
            LocalDate.parse(parts[3])
        );
    }
}

public class ExpenseTracker {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println("📘 Welcome to the Expense Tracker!");
        while (true) {
            printMainMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> addTransaction(Transaction.Type.INCOME);
                case "2" -> addTransaction(Transaction.Type.EXPENSE);
                case "3" -> viewMonthlySummary();
                case "4" -> loadFromFile();
                case "5" -> saveToFile();
                case "6" -> {
                    System.out.println("👋 Exiting. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("❌ Invalid option. Try again.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n===== Main Menu =====");
        System.out.println("1️⃣  Add Income");
        System.out.println("2️⃣  Add Expense");
        System.out.println("3️⃣  View Monthly Summary");
        System.out.println("4️⃣  Load Transactions from File");
        System.out.println("5️⃣  Save Transactions to File");
        System.out.println("6️⃣  Exit");
        System.out.print("Select an option (1-6): ");
    }

    private static void addTransaction(Transaction.Type type) {
        System.out.println("\n--- Add " + (type == Transaction.Type.INCOME ? "Income" : "Expense") + " ---");
        double amount = getAmountInput();

        String category = chooseCategory(type);
        LocalDate date = getDateInput();

        transactions.add(new Transaction(type, category, amount, date));
        System.out.println("✅ Transaction added successfully!");
    }

    private static double getAmountInput() {
        while (true) {
            try {
                System.out.print("Enter amount (e.g., 2500.75): ₹");
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid amount. Try again.");
            }
        }
    }

    private static String chooseCategory(Transaction.Type type) {
        List<String> categories = type == Transaction.Type.INCOME ?
            List.of("Salary", "Business", "Freelance", "Other") :
            List.of("Food", "Rent", "Travel", "Entertainment", "Utilities", "Other");

        System.out.println("Choose a category:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, categories.get(i));
        }

        while (true) {
            System.out.print("Enter category number: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= categories.size()) {
                    return categories.get(choice - 1);
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("❌ Invalid choice. Try again.");
        }
    }

    private static LocalDate getDateInput() {
        while (true) {
            System.out.print("Enter date (yyyy-MM-dd): ");
            try {
                return LocalDate.parse(scanner.nextLine(), formatter);
            } catch (Exception e) {
                System.out.println("❌ Invalid date format. Try again.");
            }
        }
    }

    private static void viewMonthlySummary() {
        if (transactions.isEmpty()) {
            System.out.println("📭 No transactions available to summarize.");
            return;
        }

        Map<String, Double> incomeByMonth = new TreeMap<>();
        Map<String, Double> expenseByMonth = new TreeMap<>();

        for (Transaction t : transactions) {
            String month = t.getDate().getMonth() + " " + t.getDate().getYear();
            if (t.getType() == Transaction.Type.INCOME) {
                incomeByMonth.put(month, incomeByMonth.getOrDefault(month, 0.0) + t.getAmount());
            } else {
                expenseByMonth.put(month, expenseByMonth.getOrDefault(month, 0.0) + t.getAmount());
            }
        }

        System.out.println("\n📊 Monthly Summary:");
        Set<String> months = new TreeSet<>();
        months.addAll(incomeByMonth.keySet());
        months.addAll(expenseByMonth.keySet());

        for (String month : months) {
            double income = incomeByMonth.getOrDefault(month, 0.0);
            double expense = expenseByMonth.getOrDefault(month, 0.0);
            System.out.println("📅 " + month);
            System.out.printf("   ➕ Income: ₹%.2f%n", income);
            System.out.printf("   ➖ Expenses: ₹%.2f%n", expense);
            System.out.printf("   💰 Net Savings: ₹%.2f%n", (income - expense));
            System.out.println("-------------------------");
        }
    }

    private static void saveToFile() {
        System.out.print("Enter filename to save (e.g., data.txt): ");
        String filename = scanner.nextLine();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Transaction t : transactions) {
                writer.println(t.toString());
            }
            System.out.println("✅ Data saved to " + filename);
        } catch (IOException e) {
            System.out.println("❌ Failed to save file: " + e.getMessage());
        }
    }

    private static void loadFromFile() {
        System.out.print("Enter filename to load (e.g., data.txt): ");
        String filename = scanner.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                transactions.add(Transaction.fromString(line));
                count++;
            }
            System.out.println("✅ Loaded " + count + " transactions from " + filename);
        } catch (IOException e) {
            System.out.println("❌ Failed to load file: " + e.getMessage());
        }
    }
}
