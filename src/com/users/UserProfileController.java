package com.users; // [QUAN TRỌNG] Đổi package

import com.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserProfileController {
    @FXML private Label welcomeLabel;

    public void setUserData(User user) {
        if (user != null) welcomeLabel.setText("Xin chào, " + user.getUsername());
    }

    @FXML
    private void handleLogout() {
        UserManager.getInstance().logout(); // Nhớ viết hàm này bên UserManager
        MainController.getInstance().showUserScreen(); // Reload về Guest
    }
}