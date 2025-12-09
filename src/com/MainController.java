package com;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainController {

    @FXML
    private FlowPane musicContainer; // Cái FlowPane vừa thêm trong FXML

    public void initialize() {
        // Giả lập load 10 bài hát
        try {
            for (int i = 0; i < 10; i++) {
                // Load MusicCard.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MusicCard.fxml"));
                VBox card = loader.load();
                
                // Add card vào FlowPane
                musicContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}