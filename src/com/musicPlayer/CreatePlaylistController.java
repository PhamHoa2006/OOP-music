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

    // FXML UI COMPONENTS
    @FXML
    private TextField nameField; // Tên Playlist
    @FXML
    private TextField descField; // Mô tả Playlist
    @FXML
    private ComboBox<String> privacyBox; // Chọn chế độ
    @FXML
    private Label dialogTitleLbl; // Tiêu đề Dialog
    @FXML
    private Label errorLabel; // Hiển thị lỗi dữ liệu không hợp lệ

    // LOGIC VARIABLES
    private Stage dialogStage; // Cửa sổ Dialog
    private Consumer<Playlist> onPlaylistCreated; // Callback logic

    @FXML
    public void initialize() {
        // 1. Setup ComboBox (Chọn sẵn Công khai)
        if (privacyBox != null) {
            privacyBox.getItems().addAll("Công khai", "Riêng tư"); // Thêm các chế độ
            privacyBox.getSelectionModel().selectFirst(); // Mặc định chọn cái đầu tiên
        }

        // 2. Ẩn label lỗi
        if (errorLabel != null) {
            errorLabel.setVisible(false); // Ẩn error Label khỏi giao diện
            errorLabel.setManaged(false); // Thu hồi không gian error Label chiếm
        }
    }

    // Khởi tạo Dialog
    public void setDialogStage(Stage dialogStage, Consumer<Playlist> onPlaylistCreated) {
        this.dialogStage = dialogStage;
        this.onPlaylistCreated = onPlaylistCreated;
    }

    // Đặt title cho Dialog
    public void setDialogTitle(String title) {
        if (dialogTitleLbl != null)
            dialogTitleLbl.setText(title);
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

        // a. Nếu là user
        if (currentUser != null) {
            // Kiểm tra trùng playlist đã có của user
            for (Playlist p : currentUser.getPlayLists()) {
                if (p.getTitle().equalsIgnoreCase(name)) {
                    isDuplicate = true;
                    break;
                }
            }
        }
        // b. Nếu là global
        else {
            // Kiểm tra xem có trùng tên với playlist nào trong thư viện bài hát không
            for (Playlist p : PlaylistLibrary.getInstance().getAllPlaylists()) {
                if (p.getTitle().equalsIgnoreCase(name)) {
                    isDuplicate = true;
                    break;
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

        // LƯU CHẾ ĐỘ RIÊNG TƯ
        if (privacyBox != null && privacyBox.getValue() != null) {
            String selectedPrivacy = privacyBox.getValue(); // Lấy trạng thái trong privacyBox
            newPlaylist.setPrivacy(selectedPrivacy); // Lưu vào playlist
            System.out.println("Đã set chế độ: " + selectedPrivacy);
        } else {
            newPlaylist.setPrivacy("Công khai"); // Chế độ mặc định phòng hờ
        }

        // Thiết lập tác giả playlist
        if (currentUser != null) {
            newPlaylist.setCreator(currentUser.getUsername());
        } else {
            newPlaylist.setCreator("Khách");
        }

        // 5.Callback: thông báo playlist mới được tạo
        if (onPlaylistCreated != null) {
            onPlaylistCreated.accept(newPlaylist);
        }

        // Đóng dialog
        closeDialog();
    }

    // Xử lí thao tác hủy: đóng Dialog
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    // Method Dialog
    private void closeDialog() {
        if (dialogStage != null)
            dialogStage.close();
    }

    // CÁC HÀM HELPER

    private void hienThiLoi(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg); // Thiết lập thông điệp
            errorLabel.setVisible(true);// Hiện ra UI
            errorLabel.setManaged(true);// Lấy không gian layout
            // Cài đặt Style: màu đỏ, cỡ 12px, font: italic
            errorLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12px; -fx-font-style: italic;");
            if (nameField != null)
                nameField.setStyle("-fx-border-color: #ff5555;"); // Highlight ô nhập sai
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING); // Mode: Warning -> màu vàng
            alert.initOwner(dialogStage); // Hiện trên Dialog
            // Thiết lập nội dung thông báo
            alert.setTitle("Lỗi tạo Playlist");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }

    private void resetLoi() {
        if (errorLabel != null) {
            errorLabel.setVisible(false); // Ẩn errorLabel
            errorLabel.setManaged(false); // Bỏ không gian layout đã chiếm của errorLabel
        }
        if (nameField != null) {
            nameField.setStyle(null); // Xóa style
        }
    }
}