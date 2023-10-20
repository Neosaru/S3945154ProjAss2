package application;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/S3945154db";
    private static final String DATABASE_USER = "S3945154";
    private static final String DATABASE_PASSWORD = "Carlopass123!";
    
    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean removePostById(String postId) {
        String deleteSQL = "DELETE FROM posts WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setString(1, postId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Successfully removed post with ID: " + postId);
                return true;
            } else {
                System.out.println("No post found with ID: " + postId);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean checkCredentials(String enteredUsername, String enteredPassword) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, enteredUsername);
            pstmt.setString(2, enteredPassword);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
	}
    public boolean upgradeToVIP(User user) {
        String updateSQL = "UPDATE users SET is_vip = TRUE WHERE id = ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateProfile(User user) {
        String updateSQL = "UPDATE users SET first_name = ?, last_name = ?, username = ?, password = ? WHERE id = ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}