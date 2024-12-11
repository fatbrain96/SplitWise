import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class User {
    int id;
    String name;

    User(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Group {
    int groupId;
    String groupName;
    List<User> members;

    Group(int groupId, String groupName, List<User> members) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.members = members;
    }
}

class Expense {
    int groupId;
    User payer;
    double amount;
    List<User> split;

    Expense(int groupId, User payer, double amount, List<User> split) {
        this.groupId = groupId;
        this.payer = payer;
        this.amount = amount;
        this.split = split;
    }
}

class SplitwiseApp {
    private Map<Integer, User> users = new HashMap<>();
    private Map<Integer, Group> groups = new HashMap<>();
    private List<Expense> expenses = new ArrayList<>();
    private Map<Integer, Double> userBalances = new HashMap<>();

    public void addUser(int userId, String userName) {
        if (users.containsKey(userId)) {
            JOptionPane.showMessageDialog(null, "User with ID " + userId + " already exists.");
        } else {
            User user = new User(userId, userName);
            users.put(userId, user);
            userBalances.put(userId, 0.0);
            JOptionPane.showMessageDialog(null, "User " + userName + " added successfully.");
        }
    }

    public void createGroup(int groupId, String groupName, List<Integer> userIds) {
        List<User> groupMembers = new ArrayList<>();
        for (int userId : userIds) {
            if (users.containsKey(userId)) {
                groupMembers.add(users.get(userId));
            } else {
                JOptionPane.showMessageDialog(null, "User with ID " + userId + " not found.");
                return;
            }
        }
        Group group = new Group(groupId, groupName, groupMembers);
        groups.put(groupId, group);
        JOptionPane.showMessageDialog(null, "Group '" + groupName + "' created with users: " + groupMembers);
    }

    public void addExpense(int groupId, int payerId, double amount, List<Integer> splitUserIds) {
        if (!groups.containsKey(groupId)) {
            JOptionPane.showMessageDialog(null, "Group with ID " + groupId + " not found.");
            return;
        }
        if (!users.containsKey(payerId)) {
            JOptionPane.showMessageDialog(null, "User with ID " + payerId + " not found.");
            return;
        }

        Group group = groups.get(groupId);
        List<User> splitUsers = new ArrayList<>();
        for (int userId : splitUserIds) {
            if (users.containsKey(userId)) {
                splitUsers.add(users.get(userId));
            } else {
                JOptionPane.showMessageDialog(null, "User with ID " + userId + " not found.");
                return;
            }
        }

        Expense expense = new Expense(groupId, users.get(payerId), amount, splitUsers);
        expenses.add(expense);

        double splitAmount = amount / splitUsers.size();
        for (User user : splitUsers) {
            if (user.id == payerId) {
                userBalances.put(user.id, userBalances.get(user.id) + (amount - splitAmount));
            } else {
                userBalances.put(user.id, userBalances.get(user.id) - splitAmount);
            }
        }
        JOptionPane.showMessageDialog(null, "Expense of " + amount + " added to group '" + group.groupName + "' by " + users.get(payerId).name);
    }

    public String showBalance(int userId) {
        if (!users.containsKey(userId)) {
            return "User with ID " + userId + " not found.";
        }
        double balance = userBalances.get(userId);
        if (balance > 0) {
            return users.get(userId).name + " is owed " + balance + " units.";
        } else if (balance < 0) {
            return users.get(userId).name + " owes " + -balance + " units.";
        } else {
            return users.get(userId).name + " has no balance to settle.";
        }
    }

    public String showAllBalances() {
        StringBuilder result = new StringBuilder();
        for (User user : users.values()) {
            result.append(showBalance(user.id)).append("\n");
        }
        return result.toString();
    }

    public String showExpenses() {
        StringBuilder result = new StringBuilder();
        for (Expense expense : expenses) {
            Group group = groups.get(expense.groupId);
            String payerName = expense.payer.name;
            List<String> splitNames = new ArrayList<>();
            for (User user : expense.split) {
                splitNames.add(user.name);
            }
            result.append("Group: ").append(group.groupName)
                  .append(", Payer: ").append(payerName)
                  .append(", Amount: ").append(expense.amount)
                  .append(", Split: ").append(String.join(", ", splitNames)).append("\n");
        }
        return result.toString();
    }
}

class Splitwise {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Splitwise App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1080, 1920);

        SplitwiseApp app = new SplitwiseApp();
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Space between components
        gbc.anchor = GridBagConstraints.CENTER; // Center everything

        // Add User Section
        JTextField userIdField = new JTextField(10);
        JTextField userNameField = new JTextField(10);
        JButton addUserButton = new JButton("Add User");

        addUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int userId = Integer.parseInt(userIdField.getText());
                    String userName = userNameField.getText();
                    app.addUser(userId, userName);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid user ID.");
                }
            }
        });

        // Layout for user section
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1; // Ensuring consistent width
        panel.add(new JLabel("User ID:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Allow horizontal expansion
        gbc.weightx = 1.0;  // Allow the text field to grow horizontally
        panel.add(userIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("User Name:"), gbc);

        gbc.gridx = 1;
        panel.add(userNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Make the button span the full width
        panel.add(addUserButton, gbc);

        // Create Group Section
        JTextField groupIdField = new JTextField(10);
        JTextField groupNameField = new JTextField(10);
        JTextField groupUserIdsField = new JTextField(20);  // Increased width for better readability
        JButton createGroupButton = new JButton("Create Group");

        createGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int groupId = Integer.parseInt(groupIdField.getText());
                    String groupName = groupNameField.getText();
                    String[] userIds = groupUserIdsField.getText().split(",");
                    List<Integer> userIdsList = new ArrayList<>();
                    for (String id : userIds) {
                        userIdsList.add(Integer.parseInt(id.trim()));
                    }
                    app.createGroup(groupId, groupName, userIdsList);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid group ID or user IDs.");
                }
            }
        });

        // Layout for group section
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Group ID:"), gbc);

        gbc.gridx = 1;
        panel.add(groupIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Group Name:"), gbc);

        gbc.gridx = 1;
        panel.add(groupNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("User IDs (comma separated):"), gbc);

        gbc.gridx = 1;
        panel.add(groupUserIdsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(createGroupButton, gbc);

        // Add Expense Section
        JTextField expenseGroupIdField = new JTextField(10);
        JTextField expensePayerIdField = new JTextField(10);
        JTextField expenseAmountField = new JTextField(10);
        JTextField expenseUserIdsField = new JTextField(20);  // Increased width for better readability
        JButton addExpenseButton = new JButton("Add Expense");

        addExpenseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int groupId = Integer.parseInt(expenseGroupIdField.getText());
                    int payerId = Integer.parseInt(expensePayerIdField.getText());
                    double amount = Double.parseDouble(expenseAmountField.getText());
                    String[] userIds = expenseUserIdsField.getText().split(",");
                    List<Integer> userIdsList = new ArrayList<>();
                    for (String id : userIds) {
                        userIdsList.add(Integer.parseInt(id.trim()));
                    }
                    app.addExpense(groupId, payerId, amount, userIdsList);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input.");
                }
            }
        });

        // Layout for expense section
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(new JLabel("Expense Group ID:"), gbc);

        gbc.gridx = 1;
        panel.add(expenseGroupIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        panel.add(new JLabel("Payer ID:"), gbc);

        gbc.gridx = 1;
        panel.add(expensePayerIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        panel.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1;
        panel.add(expenseAmountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        panel.add(new JLabel("Split User IDs (comma separated):"), gbc);

        gbc.gridx = 1;
        panel.add(expenseUserIdsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        panel.add(addExpenseButton, gbc);

        // Output Area for Balances and Expenses
        JTextArea outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        // Show Balances and Expenses Buttons
        JButton showBalancesButton = new JButton("Show All Balances");
        showBalancesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputArea.setText(app.showAllBalances());
            }
        });

        JButton showExpensesButton = new JButton("Show Expenses");
        showExpensesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputArea.setText(app.showExpenses());
            }
        });

        gbc.gridy = 13;
        gbc.gridwidth = 1;
        panel.add(showBalancesButton, gbc);

        gbc.gridx = 1;
        panel.add(showExpensesButton, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }
}
