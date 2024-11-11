package maybe;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class HomePage extends JFrame {
    private int userId;
    private JLabel imagePreviewLabel; // Updated to show the image preview in a box

    public HomePage(int userId) {
        this.userId = userId;

        setTitle("Home Page");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel bioLabel = new JLabel("Bio:");
        JTextArea bioField = new JTextArea(3, 20);

        JLabel achievementsLabel = new JLabel("Achievements:");
        JTextArea achievementsField = new JTextArea(3, 20);

        JLabel photoLabel = new JLabel("Profile Photo:");
        JButton uploadButton = new JButton("Upload Photo");
        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(150, 150));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Load existing photo if available
        loadProfilePhoto();

        uploadButton.addActionListener(e -> uploadPhoto());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String bio = bioField.getText();
            String achievements = achievementsField.getText();
            saveProfile(bio, achievements);
        });

        JButton nextButton = new JButton("Go to Publications");
        nextButton.addActionListener(e -> {
            new PapersPage(userId).setVisible(true);
            dispose();
        });

        // Adding components to layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(bioLabel, gbc);

        gbc.gridx = 1;
        add(new JScrollPane(bioField), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(achievementsLabel, gbc);

        gbc.gridx = 1;
        add(new JScrollPane(achievementsField), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(photoLabel, gbc);

        gbc.gridx = 1;
        add(uploadButton, gbc);

        gbc.gridx = 2;
        add(imagePreviewLabel, gbc); // Adding the image preview box

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(saveButton, gbc);

        gbc.gridy = 4;
        add(nextButton, gbc);

        setLocationRelativeTo(null);
    }

    private void loadProfilePhoto() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ProfileManager", "root", "password");
             PreparedStatement stmt = conn.prepareStatement("SELECT photo FROM profiles WHERE user_id = ?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String photoPath = rs.getString("photo");
                if (photoPath != null && !photoPath.isEmpty()) {
                    updatePhotoPreview(photoPath);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void uploadPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String photoPath = selectedFile.getAbsolutePath();

            // Save photo path in the database
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ProfileManager", "root", "password");
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE profiles SET photo = ? WHERE user_id = ? ON DUPLICATE KEY UPDATE photo = ?")) {

                stmt.setString(1, photoPath);
                stmt.setInt(2, userId);
                stmt.setString(3, photoPath);
                stmt.executeUpdate();

                // Update the photo preview
                updatePhotoPreview(photoPath);

                JOptionPane.showMessageDialog(this, "Photo uploaded successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updatePhotoPreview(String photoPath) {
        ImageIcon imageIcon = new ImageIcon(photoPath);
        Image image = imageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        imagePreviewLabel.setIcon(new ImageIcon(image));
    }

    private void saveProfile(String bio, String achievements) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ProfileManager", "root", "password");
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO profiles (user_id, bio, achievements) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE bio = ?, achievements = ?")) {

            stmt.setInt(1, userId);
            stmt.setString(2, bio);
            stmt.setString(3, achievements);
            stmt.setString(4, bio);
            stmt.setString(5, achievements);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Profile Saved!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
