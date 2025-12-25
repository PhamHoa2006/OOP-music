package com.users; // [QUAN TRỌNG] Đổi package

import com.MainController; // Import để gọi lại Main
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GuestViewController {

    @FXML
    private void openLogin() { showAuthDialog(true); }

    @FXML
    private void openRegister() { showAuthDialog(false); }

    private void showAuthDialog(boolean isLoginMode) {
        try {
            // Vì file này và AuthDialog cùng nằm trong com.users nên gọi tên trực tiếp được
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AuthDialog.fxml"));
            
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));

            AuthDialogController controller = loader.getController();
            
            // Set chế độ Login/Register (Ông cần thêm hàm setMode bên AuthDialogController nếu chưa có)
            // controller.setMode(isLoginMode); 

            controller.setDialogStage(stage, (user) -> {
                System.out.println("Login xong: " + user.getUsername());
                // Gọi ra ngoài Main để reload màn hình
                MainController.getInstance().setLoggedInUser(user);
            });

            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }
}