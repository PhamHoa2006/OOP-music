package com;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. CẬP NHẬT: Vì file FXML nằm ngay cạnh file Main.java, chỉ cần gọi tên file
            String fxmlPath = "MainLayout.fxml"; 
            URL fxmlUrl = getClass().getResource(fxmlPath);

            // Đoạn debug để chắc chắn file đã được tìm thấy
            if (fxmlUrl == null) {
                System.out.println("❌ LỖI: Không tìm thấy file MainLayout.fxml!");
                System.out.println("👉 Code đang tìm file này ngay trong thư mục 'com'");
                System.out.println("👉 BẠN CẦN 'CLEAN WORKSPACE' ĐỂ VS CODE CẬP NHẬT FILE MỚI.");
                System.exit(1);
            } else {
                System.out.println("✅ Đã tìm thấy FXML: " + fxmlUrl);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            
            // 2. CẬP NHẬT: Load CSS (Nằm cùng thư mục -> chỉ cần tên file)
            URL cssUrl = getClass().getResource("Style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠️ Cảnh báo: Không tìm thấy Style.css trong thư mục com");
            }

            primaryStage.setTitle("OOP-Music - Project môn IT3100");
            
            // 3. CẬP NHẬT: Load Icon (Thư mục icons nằm cùng thư mục com -> icons/logo.png)
            try {
                URL iconUrl = getClass().getResource("icons/logo.png");
                if (iconUrl != null) {
                    primaryStage.getIcons().add(new Image(iconUrl.toString()));
                } else {
                    System.out.println("⚠️ Cảnh báo: Không tìm thấy icon tại com/icons/logo.png");
                }
            } catch (Exception ignored) {}

            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> System.exit(0));

        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}