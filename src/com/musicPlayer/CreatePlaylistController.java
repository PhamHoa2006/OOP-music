package com.musicPlayer;
import com.users.PlaylistLibrary;
import com.musicPlayer.Playlist;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class CreatePlaylistController {

    @FXML private TextField nameField;
    @FXML private TextField descField; // Trường nhập mô tả (Check kỹ fx:id bên FXML nhé)
    @FXML private ComboBox<String> privacyBox; // Hộp chọn Công khai/Riêng tư
    @FXML private Label dialogTitleLbl;
    
    private Stage dialogStage;
    private Consumer<Playlist> onPlaylistCreated; // Callback trả về playlist khi tạo xong

    @FXML
    public void initialize() {
        // Setup dữ liệu cho ComboBox
        if (privacyBox != null) {
            privacyBox.getItems().addAll("Công khai", "Riêng tư");
            privacyBox.getSelectionModel().selectFirst(); // Mặc định chọn cái đầu
        }
    }

    // Hàm nhận Stage và Callback từ MainController truyền sang
    public void setDialogStage(Stage dialogStage, Consumer<Playlist> onPlaylistCreated) {
        this.dialogStage = dialogStage;
        this.onPlaylistCreated = onPlaylistCreated;
    }

    @FXML
    private void handleCreate() {
        if (nameField == null) return;
        
        String name = nameField.getText().trim();
        String description = "";
        if (descField != null) {
            description = descField.getText().trim();
        }

        // Validate cơ bản: Tên không được để trống
        if (name.isEmpty()) {
            nameField.setStyle("-fx-border-color: red;"); // Báo đỏ nếu rỗng
            nameField.setPromptText("Vui lòng nhập tên!");
            return;
        }
        
        // Kiểm tra xem tên có trùng lặp không
        for (Playlist p : PlaylistLibrary.getInstance().getAllPlaylists()) {
            // So sánh không phân biệt hoa thường (Ví dụ: "Rock" trùng với "rock")
            if (p.getTitle().equalsIgnoreCase(name)) {
                nameField.setStyle("-fx-border-color: red;");
                nameField.clear(); // Xóa tên vừa nhập đi
                nameField.setPromptText("Tên này đã tồn tại!"); // Cảnh báo
                return; // Dừng lại, không tạo playlist
            }
        }

        // Nếu vượt qua 2 bước trên thì Reset lại style (bỏ viền đỏ nếu có)
        nameField.setStyle(null);

        // 1. Tạo Playlist mới
        Playlist newPlaylist = new Playlist(name);
        
        // 2. [QUAN TRỌNG] Lưu mô tả vào
        newPlaylist.setDescription(description);
        
        // 3. Set người tạo mặc định (User đang login)
        newPlaylist.setCreator("pvq"); 

        // 4. (Tùy chọn) Xử lý Privacy nếu sau này cần
        // String privacy = privacyBox.getValue();
        // newPlaylist.setPrivacy(privacy); 

        // 5. Trả hàng về nơi sản xuất (MainController)
        if (onPlaylistCreated != null) {
            onPlaylistCreated.accept(newPlaylist);
        }

        // 6. Đóng Dialog
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
 // Hàm này để MainController gọi và đổi tên tiêu đề
    public void setDialogTitle(String title) {
        if (dialogTitleLbl != null) {
            dialogTitleLbl.setText(title);
        }
    }
}