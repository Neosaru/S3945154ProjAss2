package application;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class User {
	private String username;
	private String password;
	private boolean isVIP = false;
	private DatabaseManager dbManager;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean getVIP() {
		return isVIP;
	}
	public void setVIP(boolean isVIP) {
		this.isVIP = isVIP;
	}
	
	public boolean saveUser() {
        String insertSQL = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, this.username);
            pstmt.setString(2, this.password);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean authenticate(String username, String password) {
        DatabaseManager dbManager = new DatabaseManager();
        return dbManager.checkCredentials(username, password);
    }
	
	@Override
    public String toString() {
        return String.format("%s | %s", username, password);
    }
}
