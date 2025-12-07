package com;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/resources/MainLayout.fxml")));

            Scene scene = new Scene(root);

            primaryStage.setTitle("OOP MUSIC - Project IT3100");

            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/icons/logo.png")));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Không tìm thấy icon logo, bỏ qua.");
            }

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("LỖI KHỞI ĐỘNG: Kiểm tra lại tên file hoặc đường dẫn!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}