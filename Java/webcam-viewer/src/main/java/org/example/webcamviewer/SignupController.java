package org.example.webcamviewer;


import com.mongodb.client.MongoCollection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.webcamviewer.MongoConnector;
import com.mongodb.client.model.Filters;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

import static org.example.webcamviewer.MongoConnector.database;


public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label signupStatus;

    @FXML
    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Scene loginScene = new Scene(loader.load());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login Page");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            signupStatus.setText("Please enter both fields.");
            signupStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        // Check if username already exists
        MongoCollection<Document> collection = database.getCollection("users");
        Document existingUser = collection.find(Filters.eq("username", username)).first();

        if (existingUser != null) {
            signupStatus.setText("Username already taken.");
            signupStatus.setStyle("-fx-text-fill: red;");
            return;
        }

        // Hash the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Document newUser = new Document("username", username)
                .append("password", hashedPassword);

        collection.insertOne(newUser);

        signupStatus.setText("Signup successful! Please log in.");
        signupStatus.setStyle("-fx-text-fill: green;");
    }
}

