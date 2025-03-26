package com.example.resono;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private VBox loginBox;

    private Label errorLabel = new Label();

    @FXML
    public void initialize() {
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        loginBox.getChildren().add(errorLabel);
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Sample credentials
        String correctUsername = "admin";
        String correctPassword = "password";

        if (!username.equals(correctUsername) && !username.equals("user")) {
            errorLabel.setText("User not found");
            errorLabel.setVisible(true);
        } else if (!password.equals(correctPassword)) {
            errorLabel.setText("Invalid password");
            errorLabel.setVisible(true);
        } else {
            errorLabel.setVisible(false);

            // Redirect to Video Player Page for "user"
            if (username.equals("user")) {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("video-view.fxml"));
                Scene scene = new Scene(loader.load(), 800, 600);
                stage.setScene(scene);
                stage.setTitle("Resono - Video Player");
                stage.show();
            } else {
                System.out.println("Admin Login Successful");
            }
        }
    }

}
