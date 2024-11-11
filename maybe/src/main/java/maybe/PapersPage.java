package maybe;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PapersPage extends JFrame {
    private int userId;

    public PapersPage(int userId) {
        this.userId = userId;
        setTitle("Published Papers");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        JLabel yearLabel = new JLabel("Year:");
        JTextField yearField = new JTextField();

        JLabel linkLabel = new JLabel("Paper Link:");
        JTextField linkField = new JTextField();

        JButton addButton = new JButton("Add Paper");
        addButton.addActionListener(e -> {
            String year = yearField.getText();
            String link = linkField.getText();
            addPaper(year, link);
        });

        JButton viewButton = new JButton("View Papers");
        viewButton.addActionListener(e -> viewPapers());

        add(yearLabel);
        add(yearField);
        add(linkLabel);
        add(linkField);
        add(addButton);
        add(viewButton);

        setLocationRelativeTo(null);
    }

    private void addPaper(String year, String link) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ProfileManager", "root", "Troikaa78@");
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO papers (user_id, year, link) VALUES (?, ?, ?)")) {

            stmt.setInt(1, userId);
            stmt.setInt(2, Integer.parseInt(year));
            stmt.setString(3, link);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Paper Added!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void viewPapers() {
        StringBuilder result = new StringBuilder();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ProfileManager", "root", "Troikaa78@");
             PreparedStatement stmt = conn.prepareStatement("SELECT year, link FROM papers WHERE user_id = ? ORDER BY year")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int year = rs.getInt("year");
                String link = rs.getString("link");
                result.append("Year: ").append(year).append(", Link: ").append(link).append("\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, result.length() > 0 ? result.toString() : "No Papers Found!");
    }
}
