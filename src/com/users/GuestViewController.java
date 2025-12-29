package com.users;

import com.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
// Xử lý việc đăng nhập đăng ký của guest
public class GuestViewController {

    @FXML private Label titleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button actionBtn;
    @FXML private Label switchLabel;
    @FXML private Button switchModeBtn;

    private boolean isLoginMode = true;

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
            User loggedInUser = userManager.login(user, pass);
            if (loggedInUser != null) {
                System.out.println("✅ Đăng nhập thành công: " + user);
                // Gọi thẳng sang MainController để vào app luôn
                MainController.getInstance().setLoggedInUser(loggedInUser);
            } else {
                messageLabel.setText("Sai tài khoản hoặc mật khẩu!");
                messageLabel.setStyle("-fx-text-fill: #ff5555;");
            }
        } else {
            // --- XỬ LÝ ĐĂNG KÝ ---
            boolean success = userManager.register(user, pass);
            if (success) {
                messageLabel.setText("Đăng ký thành công! Đang tự động đăng nhập...");
                messageLabel.setStyle("-fx-text-fill: #1DB954;");
                
                // Đăng ký xong thì auto login luôn cho ngầu
                User newUser = userManager.login(user, pass);
                MainController.getInstance().setLoggedInUser(newUser);
            } else {
                messageLabel.setText("Tên đăng nhập đã tồn tại!");
                messageLabel.setStyle("-fx-text-fill: #ff5555;");
            }
        }
    }

    @FXML
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateUI();
        messageLabel.setText(""); // Xóa thông báo lỗi cũ
    }

    private void updateUI() {
        if (isLoginMode) {
            titleLabel.setText("Đăng nhập");
            actionBtn.setText("ĐĂNG NHẬP");
            switchLabel.setText("Chưa có tài khoản?");
            switchModeBtn.setText("Đăng ký ngay");
        } else {
            titleLabel.setText("Đăng ký");
            actionBtn.setText("ĐĂNG KÝ MIỄN PHÍ");
            switchLabel.setText("Đã có tài khoản?");
            switchModeBtn.setText("Đăng nhập tại đây");
        }
    }
}