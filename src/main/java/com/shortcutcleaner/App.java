package com.shortcutcleaner;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;


public class App extends Application{

    private Scene scene;

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
      }

      public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("scanwindow"));
        stage.setMinWidth(615);
        stage.setMinHeight(400);
        stage.setTitle("Shortcut Cleaner");
        stage.setScene(scene);
        stage.show();
  }
}
