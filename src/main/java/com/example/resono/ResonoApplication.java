package com.example.resono;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

public class ResonoApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(ResonoApplication.class.getResource("resono-login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 600);
            stage.setTitle("Resono: Login");
            stage.setScene(scene);
            stage.show();

            stage.setOnCloseRequest(event -> {
                event.consume();
                exitApp(stage);
//                stage.close();
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
    public void exitApp(Stage stage){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("You are about to logout!");
//        alert.setContentText("Do you want to save before exiting? ");

        if(alert.showAndWait().get() == ButtonType.OK){
            System.out.println("You have successfully logged out!");
            stage.close();
        }
    }
}