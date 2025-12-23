package com.users;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class AuthDialogController {

    @FXML private Label titleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button actionBtn;
    @FXML private Label switchLabel;
    @FXML private Button switchModeBtn;

    private Stage dialogStage;
    private boolean isLoginMode = true; // Mặc định là Login
    private Consumer<User> onLoginSuccess; // Callback trả về User khi login thành công

    public void setDialogStage(Stage dialogStage, Consumer<User> onLoginSuccess) {
        this.dialogStage = dialogStage;
        this.onLoginSuccess = onLoginSuccess;
        updateUI();
    }

    @FXML
    private void handleAction() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            messageLabel.setStyle("-fx-text-fill: #ff5555;");
            return;
        }

        UserManager userManager = UserManager.getInstance();

        if (isLoginMode) {
            // --- XỬ LÝ ĐĂNG NHẬP ---
            User loggedUser = userManager.login(user, pass);
            if (loggedUser != null) {
                messageLabel.setText("Đăng nhập thành công!");
                messageLabel.setStyle("-fx-text-fill: #1DB954;");
                
                if (onLoginSuccess != null) {
                    onLoginSuccess.accept(loggedUser);
                }
                dialogStage.close();
            } else {
                messageLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
                messageLabel.setStyle("-fx-text-fill: #ff5555;");
            }
        } else {
            // --- XỬ LÝ ĐĂNG KÝ ---
            boolean success = userManager.register(user, pass);
            if (success) {
                messageLabel.setText("Đăng ký thành công! Hãy đăng nhập.");
                messageLabel.setStyle("-fx-text-fill: #1DB954;");
                // Chuyển ngay sang chế độ đăng nhập để tiện cho user
                toggleMode();
                usernameField.setText(user);
                passwordField.clear();
            } else {
                messageLabel.setText("Tên đăng nhập đã tồn tại hoặc lỗi!");
                messageLabel.setStyle("-fx-text-fill: #ff5555;");
            }
        }
    }

    @FXML
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateUI();
        messageLabel.setText(""); // Xóa thông báo cũ
    }

    private void updateUI() {
        if (isLoginMode) {
            titleLabel.setText("Đăng Nhập");
            actionBtn.setText("ĐĂNG NHẬP");
            switchLabel.setText("Chưa có tài khoản?");
            switchModeBtn.setText("Đăng ký ngay");
        } else {
            titleLabel.setText("Đăng Ký");
            actionBtn.setText("ĐĂNG KÝ");
            switchLabel.setText("Đã có tài khoản?");
            switchModeBtn.setText("Đăng nhập");
        }
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}