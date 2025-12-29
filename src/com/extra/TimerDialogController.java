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

// Thiết lập giao diện timer
public class TimerDialogController {

    @FXML
    private TextField hourField, minuteField, secondField; // Ô nhập giờ, phút, giây
    @FXML
    private Label countdownLabel; // Dòng đếm ngược
    @FXML
    private Button cancelButton, startButton; // Nút bắt đầu và kết thúc đếm ngược

    private Stage dialogStage; // Cửa sổ hiện tại
    private Consumer<Integer> onTimeSelectedCallback; // interface chức năng, lưu callback

    @FXML
    public void initialize() {
        // Thiết lập bộ lọc nhập liệu
        setupInputFilter(hourField);
        setupInputFilter(minuteField);
        setupInputFilter(secondField);
    }

    // Khởi tạo dialog chọn thời gian
    public void setDialogStage(Stage dialogStage, boolean isTimerRunning, int currentSeconds,
            Consumer<Integer> callback) {
        this.dialogStage = dialogStage;
        this.onTimeSelectedCallback = callback;

        // Cập nhật giao diện dựa trên trạng thái Timer
        updateUIState(isTimerRunning);

        // Cập nhập thời gian đếm ngược
        if (isTimerRunning) {
            updateCountdownTime(currentSeconds); // Cập nhập thời gian đếm ngược hiện tại
            startButton.setText("Cập nhật"); // Đổi chữ nút Bắt đầu thành Cập nhật
        } else {
            // Reset về 00 nếu không chạy
            hourField.setText("00");
            minuteField.setText("00");
            secondField.setText("00");
            startButton.setText("Bắt đầu");
            Platform.runLater(() -> minuteField.requestFocus()); // Đặt con trỏ vào ô phút khi UI load xong
        }
    }

    // Hàm ẩn/hiện nút Hủy và dòng đếm ngược
    public void updateUIState(boolean isRunning) {
        cancelButton.setVisible(isRunning); // Quyết định nút có hiển thị hay không (T : hiện, F : ẩn)
        cancelButton.setManaged(isRunning); // Quan trọng: setManaged=false -> ẩn (cancel) -> không tính kc ->nút Bắt
                                            // đầu tự căn giữa

        countdownLabel.setVisible(isRunning); // Quyết định ẩn / hiển thị nút đếm ngược
        countdownLabel.setManaged(isRunning); // Quan trọng: setManaged=false -> ẩn (countdownLabel) -> không tính kc
                                              // ->layout tự căn giữa
    }

    // Hàm cập nhật text đếm ngược (MainController sẽ gọi hàm này liên tục)
    public void updateCountdownTime(int remainingSeconds) {
        if (remainingSeconds <= 0) {
            updateUIState(false); // Cập nhập UI
            return;
        }
        int h = remainingSeconds / 3600;
        int m = (remainingSeconds % 3600) / 60;
        int s = remainingSeconds % 60;

        // Format chuỗi: "Nhạc sẽ tắt sau: 00:12:30"
        String timeText = String.format("Nhạc sẽ tắt sau: %02d:%02d:%02d", h, m, s);
        Platform.runLater(() -> countdownLabel.setText(timeText)); // Cập nhập Label mới
    }

    @FXML
    private void handleStart() {
        int hours = parseTime(hourField); // Chuyển thành số nguyên
        int minutes = parseTime(minuteField); // Chuyển thành số nguyên
        int seconds = parseTime(secondField); // Chuyển thành số nguyên

        // Tính tổng ra GIÂY (Thay vì Phút)
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

    // Đóng dialog
    @FXML
    private void handleClose() {
        dialogStage.close();
    }

    // Gửi kết quả về controller và đóng Dialog
    private void sendResult(int minutes) {
        if (onTimeSelectedCallback != null)
            onTimeSelectedCallback.accept(minutes);
        dialogStage.close();
    }

    // Chuyển TextField thành số nguyên
    private int parseTime(TextField field) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // Thiết lập bộ lọc nhập liệu cho TextField
    private void setupInputFilter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText(); // nội dung mới của TextField nếu có thay đổi được áp dụng
            if (newText.matches("[0-9]*") && newText.length() <= 2)
                return change; // Chỉ cho phép nhập 2 số 0-9
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter)); // Chạy bộ lọc nhập liệu.
        field.setOnMouseClicked(e -> field.selectAll()); // Click chuột vào TextField -> bôi đen hết
        field.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (newVal)
                Platform.runLater(field::selectAll);
        }); // Đảm bảo bôi đen hết
    }
}