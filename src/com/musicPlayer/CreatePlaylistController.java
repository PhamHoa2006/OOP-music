package com.musicPlayer;

import com.users.PlaylistLibrary;
import com.users.User;
import com.users.UserManager;
import com.musicPlayer.Playlist;
import javafx.scene.control.Alert;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreatePlaylistController {

    // --- FXML UI COMPONENTS ---
    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private ComboBox<String> privacyBox; // Đã có biến này
    @FXML private Label dialogTitleLbl;
    @FXML private Label errorLabel; 

    // --- LOGIC VARIABLES ---
    private Stage dialogStage;
    private Consumer<Playlist> onPlaylistCreated; 

    @FXML
    public void initialize() {
        // 1. Setup ComboBox (Chọn sẵn Công khai)
        if (privacyBox != null) {
            privacyBox.getItems().addAll("Công khai", "Riêng tư");
            privacyBox.getSelectionModel().selectFirst(); // Mặc định chọn cái đầu tiên
        }
        
        // 2. Ẩn label lỗi
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false); 
        }
    }

    public void setDialogStage(Stage dialogStage, Consumer<Playlist> onPlaylistCreated) {
        this.dialogStage = dialogStage;
        this.onPlaylistCreated = onPlaylistCreated;
    }
    
    public void setDialogTitle(String title) {
        if (dialogTitleLbl != null) dialogTitleLbl.setText(title);
    }

    @FXML
    private void handleCreate() {
        // 1. Lấy dữ liệu nhập
        String name = (nameField != null) ? nameField.getText().trim() : "";
        String description = (descField != null) ? descField.getText().trim() : "";

        // 2. Validate tên trống
        if (name.isEmpty()) {
            hienThiLoi("Tên không được để trống!");
            return;
        }

        // 3. Kiểm tra trùng tên
        User currentUser = UserManager.getInstance().getCurrentUser();
        boolean isDuplicate = false;

        if (currentUser != null) {
            for (Playlist p : currentUser.getPlayLists()) {
                if (p.getTitle().equalsIgnoreCase(name)) {
                    isDuplicate = true; break;
                }
            }
        } else {
            for (Playlist p : PlaylistLibrary.getInstance().getAllPlaylists()) {
                if (p.getTitle().equalsIgnoreCase(name)) {
                    isDuplicate = true; break;
                }
            }
        }

        if (isDuplicate) {
            hienThiLoi("Bạn đã có Playlist tên này rồi!");
            return;
        }

        resetLoi();

        // 4. Tạo Playlist mới
        Playlist newPlaylist = new Playlist(name);
        newPlaylist.setDescription(description);
        
        // --- [FIX QUAN TRỌNG] LƯU CHẾ ĐỘ RIÊNG TƯ ---
        if (privacyBox != null && privacyBox.getValue() != null) {
            String selectedPrivacy = privacyBox.getValue();
            newPlaylist.setPrivacy(selectedPrivacy); // Lưu vào playlist
            System.out.println("🔒 Đã set chế độ: " + selectedPrivacy);
        } else {
            newPlaylist.setPrivacy("Công khai"); // Mặc định phòng hờ
        }
        // --------------------------------------------
        
        // Set người tạo
        if (currentUser != null) {
            newPlaylist.setCreator(currentUser.getUsername());
        } else {
            newPlaylist.setCreator("Khách");
        }

        // 5. Trả hàng về
        if (onPlaylistCreated != null) {
            onPlaylistCreated.accept(newPlaylist);
        }

        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
    
    // --- CÁC HÀM HELPER ---
    
    private void hienThiLoi(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            errorLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12px; -fx-font-style: italic;");
            if (nameField != null) nameField.setStyle("-fx-border-color: #ff5555;");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(dialogStage);
            alert.setTitle("Lỗi tạo Playlist");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }

    private void resetLoi() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        if (nameField != null) {
            nameField.setStyle(null);
        }
    }
}