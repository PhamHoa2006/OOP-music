package com.musicPlayer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.function.BiConsumer;

public class UploadDialogController {

    @FXML private TextField titleField;
    @FXML private TextField artistField;

    private Stage dialogStage;
    // Callback trả về 2 tham số: (Title, Artist)
    private BiConsumer<String, String> onInfoConfirmed; 

    public void setDialogStage(Stage dialogStage, String defaultTitle, BiConsumer<String, String> callback) {
        this.dialogStage = dialogStage;
        this.onInfoConfirmed = callback;
        
        // Điền sẵn tên file vào ô title để đỡ phải gõ nếu lười
        if (defaultTitle != null) {
            titleField.setText(defaultTitle);
        }
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText().trim();
        String artist = artistField.getText().trim();

        // Validate cơ bản
        if (title.isEmpty()) title = "Unknown Title";
        if (artist.isEmpty()) artist = "Unknown Artist";

        if (onInfoConfirmed != null) {
            onInfoConfirmed.accept(title, artist);
        }
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}