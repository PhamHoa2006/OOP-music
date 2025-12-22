package com.extra;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.function.Consumer;

// Controller này chỉ quản lý logic của cái cửa sổ hẹn giờ
public class TimerDialogController {

    @FXML private TextField customMinutesField;
    @FXML private Button cancelButton;

    private Stage dialogStage;
    // Interface để gửi kết quả về MainController
    private Consumer<Integer> onTimeSelectedCallback; 

    // Hàm này được MainController gọi để thiết lập ban đầu
    public void setDialogStage(Stage dialogStage, boolean isTimerRunning, Consumer<Integer> callback) {
        this.dialogStage = dialogStage;
        this.onTimeSelectedCallback = callback;
        // Nếu timer đang chạy thì mới hiện nút Hủy
        cancelButton.setVisible(isTimerRunning);
    }

    // Xử lý các nút chọn nhanh
    @FXML private void handlePreset15() { selectTime(15); }
    @FXML private void handlePreset30() { selectTime(30); }
    @FXML private void handlePreset45() { selectTime(45); }
    @FXML private void handlePreset60() { selectTime(60); }

    // Xử lý nút "Bắt đầu" (nhập tay)
    @FXML
    private void handleCustomStart() {
        try {
            int minutes = Integer.parseInt(customMinutesField.getText());
            if (minutes > 0) {
                selectTime(minutes);
            } else {
                showError();
            }
        } catch (NumberFormatException e) {
            showError();
        }
    }

    // Xử lý nút "Hủy"
    @FXML
    private void handleCancel() {
        if (onTimeSelectedCallback != null) {
            onTimeSelectedCallback.accept(-1); // Gửi mã -1 để hủy
        }
        dialogStage.close();
    }

    // Hàm chung để gửi kết quả và đóng cửa sổ
    private void selectTime(int minutes) {
        if (onTimeSelectedCallback != null) {
            onTimeSelectedCallback.accept(minutes);
        }
        dialogStage.close();
    }

    private void showError() {
        customMinutesField.setStyle("-fx-background-color: #404040; -fx-text-fill: white; -fx-border-color: red;");
        customMinutesField.setPromptText("Vui lòng nhập số dương!");
    }
}