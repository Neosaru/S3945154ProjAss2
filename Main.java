package application;

//Logic for Dashboard

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private User loggedInUser;
    private List<String> posts = new ArrayList<>();
    private TextArea postArea;
    private ListView<String> postListView;
    private Button vipButton;
    private DatabaseManager dbManager = new DatabaseManager();
    private TextField usernameField;
    private PasswordField passwordField;
    
    
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");
        
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        
        registerButton.setOnAction(e -> openRegisterWindow());
        loginButton.setOnAction(e -> {
            if (login(usernameField.getText(), passwordField.getText())) {
                primaryStage.close();
                
                openMainAppWindow();
            }
        });

        layout.getChildren().addAll(
        		usernameField,
        		passwordField, 
        		loginButton, 
        		registerButton);

        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void restartApplication() {
        Stage primaryStage = new Stage();
        start(primaryStage);
    }	
	public void register(String newUsername, String newPassword) {
	    String checkQuery = "SELECT * FROM users WHERE username = ?";
	    try (PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkQuery)) {
	        checkStmt.setString(1, newUsername);
	        ResultSet rs = checkStmt.executeQuery();
	        if (rs.next()) {
	            System.out.println("Username already exists. Registration failed.");
	            return;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("Error checking for existing username.");
	        return;
	    }

	    String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
	    try (PreparedStatement insertStmt = dbManager.getConnection().prepareStatement(insertQuery)) {
	        insertStmt.setString(1, newUsername);
	        insertStmt.setString(2, newPassword);
	        insertStmt.executeUpdate();
	        showConfirm("Success", "User registered successfully", "Please close the window");
	    } catch (SQLException e) {
	        e.printStackTrace();
	        showError("Error", "Registration Failed");
	    }
	}

	private void openRegisterWindow() {
        Stage registerStage = new Stage();
        VBox registerLayout = new VBox(10);

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("New Username");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> register(newUsernameField.getText(), newPasswordField.getText()));

        registerLayout.getChildren().addAll(newUsernameField, newPasswordField, submitButton);

        Scene registerScene = new Scene(registerLayout, 300, 200);
        registerStage.setTitle("Register");
        registerStage.setScene(registerScene);
        registerStage.show();
    }
	private void openMainAppWindow() {
        Stage mainStage = new Stage();
        VBox mainLayout = new VBox(10);

        Label welcomeLabel = new Label("Welcome, " + loggedInUser.getUsername());
        mainLayout.getChildren().add(welcomeLabel);
        
        
        Button updateProfileWindowButton = new Button("Update Profile");
        updateProfileWindowButton.setOnAction(e -> openUpdateProfileWindow());
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            mainStage.close();
            restartApplication();
        });
        
        Button addPostButton = new Button("Add New Post");
        addPostButton.setOnAction(e -> openAddPostWindow());
        
        Button openRetrievePostWindowButton = new Button("Open Retrieve Post Window");
        openRetrievePostWindowButton.setOnAction(e -> openRetrievePostWindow());
        
        Button openRemovePostWindowButton = new Button("Open Remove Post Window");
        openRemovePostWindowButton.setOnAction(e -> openRemovePostWindow());
        
        Button openExportPostWindowButton = new Button("Open Export Post Window");
        openExportPostWindowButton.setOnAction(e -> openExportPostWindow());
        
        if (!loggedInUser.getVIP()) {
            Button vipFeatureButton = new Button("Upgrade to VIP!");
            vipFeatureButton.setOnAction(e -> {
            	upgradeToVIP();
            });
            mainLayout.getChildren().add(vipFeatureButton);
        }
        mainLayout.getChildren().addAll(
        	logoutButton,
        	updateProfileWindowButton,
        	addPostButton,
        	openRetrievePostWindowButton,
        	openRemovePostWindowButton,
        	openExportPostWindowButton
        );


        Scene mainScene = new Scene(mainLayout, 500, 400);
        mainStage.setTitle("User Dashboard");
        mainStage.setScene(mainScene);
        mainStage.show();
	}
	private void updateProfile() {
	    
	    String newUsername = usernameField.getText();
	    String newPassword = passwordField.getText();

	    loggedInUser.setUsername(newUsername);
	    loggedInUser.setPassword(newPassword);

	    
	    if (dbManager.updateProfile(loggedInUser)) {
	        showConfirm("Success","SUCCESS!", "Profile updated successfully.");
	    } else {
	        showError("Error", "Failed to update profile.");
	    }
	}
	public boolean login(String username, String password) {
	    String query = "SELECT * FROM users WHERE username = ? AND password = ?";
	    try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, password);

	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            showConfirm("Welcome", "WELCOME!", username);
	            
	            this.loggedInUser = new User(username, password);  
	            return true;
	        } else {
	            showError("Error","Invalid username or password.");
	            return false;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

    public void logout() {
        loggedInUser = null;
        postListView.getItems().clear();
        postArea.clear();
        vipButton.setDisable(false);
    }
    public void createPost(String content) {
        if (loggedInUser != null) {
            posts.add(content);
            postListView.getItems().add(content);
        } else {
            showError("Error", "Please login first.");
        }
    }
    private void openUpdateProfileWindow() {
        Stage updateProfileStage = new Stage();
        VBox updateProfileLayout = new VBox(10);

        TextField updateUsernameField = new TextField();
        updateUsernameField.setPromptText("Update Username");

        PasswordField updatePasswordField = new PasswordField();
        updatePasswordField.setPromptText("Update Password");

        Button submitUpdateButton = new Button("Submit Update");
        submitUpdateButton.setOnAction(e -> {
            updateProfile();
        });

        updateProfileLayout.getChildren().addAll(
            updateUsernameField, 
            updatePasswordField, 
            submitUpdateButton
        );

        Scene updateProfileScene = new Scene(updateProfileLayout, 300, 200);
        updateProfileStage.setTitle("Update Profile");
        updateProfileStage.setScene(updateProfileScene);
        updateProfileStage.show();
    }
    private void openAddPostWindow() {
        Stage addPostStage = new Stage();
        VBox addPostLayout = new VBox(10);

        TextField postIdField = new TextField();
        postIdField.setPromptText("Post ID");

        TextArea postContentField = new TextArea();
        postContentField.setPromptText("Post Content");

        TextField postAuthorField = new TextField();
        postAuthorField.setPromptText("Author");

        TextField postLikesField = new TextField();
        postLikesField.setPromptText("#Likes");

        TextField postSharesField = new TextField();
        postSharesField.setPromptText("#Shares");

        TextField postDateTimeField = new TextField();
        postDateTimeField.setPromptText("Date-Time");

        Button submitPostButton = new Button("Submit Post");
        submitPostButton.setOnAction(e -> {
            addPost(
                postIdField.getText(),
                postContentField.getText(),
                postAuthorField.getText(),
                postLikesField.getText(),
                postSharesField.getText(),
                postDateTimeField.getText()
            );
        });

        addPostLayout.getChildren().addAll(
            postIdField,
            postContentField,
            postAuthorField,
            postLikesField,
            postSharesField,
            postDateTimeField,
            submitPostButton
        );

        Scene addPostScene = new Scene(addPostLayout, 400, 400);
        addPostStage.setTitle("Add New Post");
        addPostStage.setScene(addPostScene);
        addPostStage.show();
    }
    private void addPost(String id, String content, String author, String likes, String shares, String dateTime) {
        try {
            Integer.parseInt(likes);
            Integer.parseInt(shares);
        } catch (NumberFormatException e) {
            showError("Error", "Likes and Shares must be numbers.");
            return;
        }

        String checkQuery = "SELECT * FROM posts WHERE ID = ?";
        try (PreparedStatement checkStmt = dbManager.getConnection().prepareStatement(checkQuery)) {
            checkStmt.setString(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showError("Error", "Post ID already exists.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Error checking for existing Post ID.");
            return;
        }

        String insertQuery = "INSERT INTO posts (ID, content, author, likes, shares, date_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = dbManager.getConnection().prepareStatement(insertQuery)) {
            insertStmt.setString(1, id);
            insertStmt.setString(2, content);
            insertStmt.setString(3, author);
            insertStmt.setInt(4, Integer.parseInt(likes));
            insertStmt.setInt(5, Integer.parseInt(shares));
            insertStmt.setString(6, dateTime);
            insertStmt.executeUpdate();
            showConfirm("Success", "SUCCESS!", "Post added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to add post.");
        }
    }
    private void retrievePostById(String postId) {
        String query = "SELECT * FROM posts WHERE ID = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String id = rs.getString("ID");
                String content = rs.getString("content");
                String author = rs.getString("author");
                int likes = rs.getInt("likes");
                int shares = rs.getInt("shares");
                String dateTime = rs.getString("date_time");
                
                showConfirm("Post Details", 
                            "ID: " + id + "\n" +
                            "Content: " + content + "\n" +
                            "Author: " + author + "\n" +
                            "Likes: " + likes + "\n" +
                            "Shares: " + shares + "\n" +
                            "Date-Time: " + dateTime);
            } else {
                showError("Error", "No post found with the given ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to retrieve post.");
        }
    }
    private void openRetrievePostWindow() {
        Stage retrievePostStage = new Stage();
        VBox retrievePostLayout = new VBox(10);

        TextField postIdField = new TextField();
        postIdField.setPromptText("Enter Post ID");

        Button retrievePostButton = new Button("Retrieve Post");
        retrievePostButton.setOnAction(e -> {
            retrievePostById(postIdField.getText());
        });

        retrievePostLayout.getChildren().addAll(
            new Label("Enter Post ID: "), postIdField,
            retrievePostButton
        );

        Scene retrievePostScene = new Scene(retrievePostLayout, 300, 200);
        retrievePostStage.setTitle("Retrieve Post by ID");
        retrievePostStage.setScene(retrievePostScene);
        retrievePostStage.show();
    }
    private void openRemovePostWindow() {
        Stage removePostStage = new Stage();
        VBox removePostLayout = new VBox(10);

        TextField postIdField = new TextField();
        postIdField.setPromptText("Enter Post ID");

        Button removePostButton = new Button("Remove Post");
        removePostButton.setOnAction(e -> {
            String postId = postIdField.getText();
            boolean success = dbManager.removePostById(postId);
            if (success) {
                showConfirm("Success", "SUCCESS!", "Post successfully removed");
            } else {
            	showConfirm("Error", "ERROR!", "Failed to remove post");
            }
        });

        removePostLayout.getChildren().addAll(
            new Label("Enter Post ID: "), postIdField,
            removePostButton
        );

        Scene removePostScene = new Scene(removePostLayout, 300, 200);
        removePostStage.setTitle("Remove Post by ID");
        removePostStage.setScene(removePostScene);
        removePostStage.show();
    }
	private void openExportPostWindow() {

        Stage exportPostStage = new Stage();
        VBox exportPostLayout = new VBox(10);
        TextField postIdField = new TextField();
        postIdField.setPromptText("Enter Post ID");

        Button exportPostButton = new Button("Export Post");
        exportPostButton.setOnAction(e -> {
            exportPostToCSV(postIdField.getText());
        });

        exportPostLayout.getChildren().addAll(
            new Label("Enter Post ID: "), 
            postIdField, 
            exportPostButton
        );

        Scene exportPostScene = new Scene(exportPostLayout, 300, 200);

        exportPostStage.setTitle("Export Post to CSV");
        exportPostStage.setScene(exportPostScene);
        exportPostStage.show();
    }
    private void exportPostToCSV(String postId) {
        String id = null, content = null, author = null, likes = null, shares = null, dateTime = null;

        String query = "SELECT * FROM posts WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(query)) {
            pstmt.setString(1, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getString("id");
                content = rs.getString("content");
                author = rs.getString("author");
                likes = rs.getString("likes");
                shares = rs.getString("shares");
                dateTime = rs.getString("dateTime");
            } else {
                showError("Error", "No post found with the given ID.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to retrieve post.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Post");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            try (FileWriter writer = new FileWriter(selectedFile)) {
                writer.append("ID,Content,Author,Likes,Shares,Date-Time\n");
                writer.append(id + "," + content + "," + author + "," + likes + "," + shares + "," + dateTime + "\n");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error", "Failed to write to CSV file.");
            }
        }
    }
    public void upgradeToVIP() {
        if (loggedInUser != null) {
            loggedInUser.setVIP(true);
            vipButton.setDisable(true);
            showConfirm("Success", "SUCCESS!", "Successfully upgraded to VIP");
        } else {
            showError("Error", "Failed to Upgrade");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void showConfirm(String title,String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void showConfirm(String title, String content) {
        showConfirm(title, null, content);
    }
}
