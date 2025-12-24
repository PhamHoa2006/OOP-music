package com.extra;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class TimerDialogController {

    @FXML private TextField hourField, minuteField, secondField;
    @FXML private Label countdownLabel; // Dòng đếm ngược
    @FXML private Button cancelButton, startButton;

    private Stage dialogStage;
    private Consumer<Integer> onTimeSelectedCallback; 

    @FXML
    public void initialize() {
        setupInputFilter(hourField);
        setupInputFilter(minuteField);
        setupInputFilter(secondField);
    }

    public void setDialogStage(Stage dialogStage, boolean isTimerRunning, int currentSeconds, Consumer<Integer> callback) {
        this.dialogStage = dialogStage;
        this.onTimeSelectedCallback = callback;
        
        // Cập nhật giao diện dựa trên trạng thái Timer
        updateUIState(isTimerRunning);
        
        if (isTimerRunning) {
            updateCountdownTime(currentSeconds);
            startButton.setText("Cập nhật"); // Đổi chữ nút Bắt đầu thành Cập nhật
        } else {
            // Reset về 00 nếu không chạy
            hourField.setText("00");
            minuteField.setText("00");
            secondField.setText("00");
            startButton.setText("Bắt đầu");
            Platform.runLater(() -> minuteField.requestFocus());
        }
    }

    // Hàm ẩn/hiện nút Hủy và dòng đếm ngược
    public void updateUIState(boolean isRunning) {
        cancelButton.setVisible(isRunning);
        cancelButton.setManaged(isRunning); // Quan trọng: setManaged=false để nút Bắt đầu tự căn giữa
        
        countdownLabel.setVisible(isRunning);
        countdownLabel.setManaged(isRunning);
    }

    // Hàm cập nhật text đếm ngược (MainController sẽ gọi hàm này liên tục)
    public void updateCountdownTime(int remainingSeconds) {
        if (remainingSeconds <= 0) {
            updateUIState(false);
            return;
        }
        int h = remainingSeconds / 3600;
        int m = (remainingSeconds % 3600) / 60;
        int s = remainingSeconds % 60;
        
        // Format chuỗi: "Nhạc sẽ tắt sau: 00:12:30"
        String timeText = String.format("Nhạc sẽ tắt sau: %02d:%02d:%02d", h, m, s);
        Platform.runLater(() -> countdownLabel.setText(timeText));
    }

    @FXML
    private void handleStart() {
        int hours = parseTime(hourField);
        int minutes = parseTime(minuteField);
        int seconds = parseTime(secondField);

        // [SỬA LẠI] Tính tổng ra GIÂY (Thay vì Phút)
        long totalSeconds = (hours * 3600L) + (minutes * 60L) + seconds;

        // Nếu nhập toàn 0 -> Hủy
        if (totalSeconds == 0) {
            handleCancel();
        } else {
            // Gửi thẳng số giây về MainController (ép kiểu về int cho nhẹ)
            sendResult((int) totalSeconds);
        }
    }

    @FXML
    private void handleCancel() {
        sendResult(-1); // Gửi lệnh hủy
    }

    @FXML private void handleClose() { dialogStage.close(); }

    private void sendResult(int minutes) {
        if (onTimeSelectedCallback != null) onTimeSelectedCallback.accept(minutes);
        dialogStage.close();
    }

    // ... (Hàm parseTime và setupInputFilter giữ nguyên như cũ)
    private int parseTime(TextField field) {
        try { return Integer.parseInt(field.getText().trim()); } catch (Exception e) { return 0; }
    }
    
    private void setupInputFilter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[0-9]*") && newText.length() <= 2) return change;
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
        field.setOnMouseClicked(e -> field.selectAll());
        field.focusedProperty().addListener((o, oldVal, newVal) -> { if(newVal) Platform.runLater(field::selectAll); });
    }
}