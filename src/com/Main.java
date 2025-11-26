package com;

import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.*;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    Button button;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World");
        button = new Button();
        button.setText("Say Hello World");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello OOP");
            }
        });
        StackPane layout = new StackPane();
        layout.getChildren().add(button);
        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}