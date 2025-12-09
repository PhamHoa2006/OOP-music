package com;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.Objects;
import com.users.History;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Load FXML và lấy controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UIDesign/MainLayout.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();

            // 2.1. Tạo History và truyền vào controller
            History userHistory = new History();
            controller.setUserHistory(userHistory);

            // 2.2. Tạo MediaPlayer và truyền vào controller
            Media media = new Media(new File("MusicResource/AllSongList").toURI().toString()); // ví dụ file nhạc là "music.mp3"
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            controller.setMediaPlayer(mediaPlayer); // controller lưu biến MediaPlayer

            // 3. Tạo Scene
            Scene scene = new Scene(root);

            // 4. Stage setup
            primaryStage.setTitle("OOP MUSIC - Project IT3100");

            // 5. Thêm icon nếu có
            try {
                Image icon = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/resources/icons/logo.png"))
                );
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