package org.example.webcamviewer;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.webcamviewer.MongoConnector;
import com.mongodb.client.model.Filters;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;


public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginStatus;

    @FXML
    private void switchToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("signup.fxml"));
            Scene signupScene = new Scene(loader.load());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(signupScene);
            stage.setTitle("Signup Page");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Document user = MongoConnector.getUsersCollection()
                .find(Filters.eq("username", username)).first();

        if (user != null && BCrypt.checkpw(password, user.getString("password"))) {
            loginStatus.setText("Login successful!");
            loadDashboard();
        } else {
            loginStatus.setText("Invalid username or password.");
        }
    }

    @FXML
    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow(); // or wherever your current component is
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Dashboard");

            // Maximize window or set to full screen
            stage.setMaximized(true);

            // Center on screen (in case you're not using maximize)
            stage.centerOnScreen();

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

