package com.musicPlayer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class CreatePlaylistController {

    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private ComboBox<String> privacyCombo;
    @FXML private Button createBtn;
    @FXML private Button cancelBtn;

    private Stage dialogStage;
    private Consumer<Playlist> onPlaylistCreated; // Cái "móc" để trả hàng về Main

    @FXML
    public void initialize() {
        // 1. Setup ComboBox Quyền riêng tư
        privacyCombo.getItems().addAll("Công khai", "Riêng tư");
        privacyCombo.getSelectionModel().selectFirst(); // Mặc định là Công khai

        // 2. Xử lý sự kiện nút bấm
        cancelBtn.setOnAction(e -> closeDialog());
        createBtn.setOnAction(e -> handleCreate());
        
        // Bonus: Bấm Enter ở ô tên thì cũng Tạo luôn cho tiện
        nameField.setOnAction(e -> handleCreate());
    }

    // Hàm set stage và callback (Được gọi từ MainController)
    public void setDialogStage(Stage dialogStage, Consumer<Playlist> onPlaylistCreated) {
        this.dialogStage = dialogStage;
        this.onPlaylistCreated = onPlaylistCreated;
    }

    private void handleCreate() {
        String name = nameField.getText().trim();
        String description = descField.getText().trim();
        // String privacy = privacyCombo.getValue(); // Hiện tại Playlist chưa có trường này, lấy để đó sau này nâng cấp

        // Validate cơ bản: Không được để trống tên
        if (name.isEmpty()) {
            nameField.setStyle("-fx-border-color: red; -fx-background-color: transparent; -fx-text-fill: white;");
            nameField.setPromptText("Vui lòng nhập tên!");
            return;
        }

        // Tạo Playlist mới
        // Lưu ý: Nếu class Playlist của ông chưa có field description thì dùng constructor cơ bản
        Playlist newPlaylist = new Playlist(name);
        
        // Nếu ông đã upgrade class Playlist có thêm description thì dùng:
        // Playlist newPlaylist = new Playlist(name, description);

        // Gọi Callback để trả Playlist về cho MainController xử lý tiếp
        if (onPlaylistCreated != null) {
            onPlaylistCreated.accept(newPlaylist);
        }

        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}