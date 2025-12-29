package com.users;

import com.MainController;
import com.musicPlayer.Playlist;
import com.musicPlayer.Song;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.io.IOException;
import java.util.ArrayList; // [MỚI]
import java.util.Comparator; // [MỚI]
import java.util.List;

public class UserProfileController {

    @FXML private Label usernameLabel;
    @FXML private FlowPane playlistContainer; 
    @FXML private VBox favoritesContainer;    
    @FXML private VBox historyContainer;      
    @FXML private FlowPane likedPlaylistContainer;
    @FXML private Label emptyLikedLabel;

    public void setUserData(User user) {
        if (user == null) return;
        
        usernameLabel.setText(user.getUsername());
        
        // --- 1. PLAYLIST CÁ NHÂN (Có sắp xếp & Slider) ---
        playlistContainer.getChildren().clear();
        if (user.getPlayLists() != null) {
            // A. [LOGIC SẮP XẾP MỚI] 
            // Copy danh sách ra để sắp xếp mà không làm hỏng dữ liệu gốc
            List<Playlist> sortedPlaylists = new ArrayList<>(user.getPlayLists());
            sortedPlaylists.sort((p1, p2) -> {
                String title1 = p1.getTitle();
                String title2 = p2.getTitle();

                // Ưu tiên 1: "Nhạc tải lên" luôn lên đầu
                if (title1.equals("Nhạc tải lên")) return -1;
                if (title2.equals("Nhạc tải lên")) return 1;

                // Ưu tiên 2: Sắp xếp Alphabet (A-Z)
                return title1.compareToIgnoreCase(title2);
            });

            // B. Vẽ lên giao diện (Dùng danh sách đã sắp xếp)
            HBox hbox = new HBox(15); 
            hbox.setPadding(new Insets(5)); 
            
            boolean hasPlaylist = false;
            for (Playlist p : sortedPlaylists) {
                if(p.getTitle().equals("Favorites")) continue; // Bỏ qua Favorites
                VBox card = createPlaylistCard(p);
                hbox.getChildren().add(card);
                hasPlaylist = true;
            }
            
            if (hasPlaylist) {
                ScrollPane slider = createHorizontalSlider(hbox);
                slider.prefWidthProperty().bind(playlistContainer.widthProperty());
                playlistContainer.getChildren().add(slider);
            } else {
                Label l = new Label("Chưa tạo playlist nào.");
                l.setStyle("-fx-text-fill: #808080; -fx-font-size: 16px; -fx-padding: 10;");
                playlistContainer.getChildren().add(l);
            }
        }
        
        // --- 2. PLAYLIST ĐÃ THÍCH ---
        likedPlaylistContainer.getChildren().clear();
        if (user.getLikedPlaylists() != null && !user.getLikedPlaylists().isEmpty()) {
            emptyLikedLabel.setVisible(false);
            emptyLikedLabel.setManaged(false);
            
            HBox hboxLiked = new HBox(15);
            hboxLiked.setPadding(new Insets(5));
            
            for (Playlist p : user.getLikedPlaylists()) {
                VBox card = createPlaylistCard(p);
                hboxLiked.getChildren().add(card);
            }
            
            ScrollPane sliderLiked = createHorizontalSlider(hboxLiked);
            sliderLiked.prefWidthProperty().bind(likedPlaylistContainer.widthProperty());
            likedPlaylistContainer.getChildren().add(sliderLiked);
            
        } else {
            emptyLikedLabel.setVisible(true);
            emptyLikedLabel.setManaged(true);
            emptyLikedLabel.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 20;");
        }

        // --- 3. FAVORITES SONGS ---
        favoritesContainer.getChildren().clear();
        Playlist favPlaylist = null;
        if (user.getPlayLists() != null) {
            for(Playlist p : user.getPlayLists()) {
                if(p.getTitle().equals("Favorites")) { favPlaylist = p; break; }
            }
        }
        
        if (favPlaylist != null && !favPlaylist.getSongs().isEmpty()) {
            List<Song> songs = favPlaylist.getSongs();
            int limit = Math.min(songs.size(), 15);
            for (int i = 0; i < limit; i++) {
                Node row = createSongRow(songs.get(i));
                if (row != null) favoritesContainer.getChildren().add(row);
            }
        } else {
            addEmptyLabel(favoritesContainer, "Chưa có bài hát yêu thích nào.");
        }

        // --- 4. HISTORY ---
        historyContainer.getChildren().clear();
        if (user.getHistory() != null) {
            List<Song> playedSongs = user.getHistory().getPlayedSongs();
            if (playedSongs != null && !playedSongs.isEmpty()) {
                int limit = Math.min(playedSongs.size(), 15);
                for (int i = 0; i < limit; i++) {
                    Node row = createSongRow(playedSongs.get(i));
                    if (row != null) historyContainer.getChildren().add(row);
                }
            } else {
                addEmptyLabel(historyContainer, "Chưa nghe bài hát nào gần đây.");
            }
        }
    }

    // --- CÁC HÀM HELPER ---

    private ScrollPane createHorizontalSlider(HBox contentBox) {
        ScrollPane scroller = new ScrollPane(contentBox);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 
        scroller.setFitToHeight(true); 
        scroller.setPannable(true);    
        scroller.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroller.getStyleClass().add("horizontal-scroll");
        return scroller;
    }

    private VBox createPlaylistCard(Playlist p) {
        VBox card = new VBox(5);
        card.setPrefSize(140, 190);
        card.setStyle("-fx-background-color: #282828; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
        
        ImageView img = new ImageView();
        try {
            img.setImage(new Image(getClass().getResourceAsStream("/com/icons/logo.png")));
        } catch (Exception e) {}
        img.setFitWidth(120); img.setFitHeight(120);
        
        Label name = new Label(p.getTitle());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        name.setWrapText(true);
        name.setMaxWidth(120);
        
        Label creator = new Label(p.getCreator());
        creator.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12px;");
        creator.setWrapText(true);
        creator.setMaxWidth(120);
        
        card.getChildren().addAll(img, name, creator);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3E3E3E; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #282828; -fx-background-radius: 8; -fx-padding: 10;"));
        
        card.setOnMouseClicked(e -> {
            System.out.println("🖱️ Đã chọn playlist: " + p.getTitle());
            MainController.getInstance().hienThiChiTietPlaylist(p);
        });
        
        return card;
    }

    private Node createSongRow(Song s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/SongRow.fxml"));
            Node row = loader.load();
            
            Label title = (Label) row.lookup("#rowTitle");
            Label artist = (Label) row.lookup("#rowArtist");
            Label duration = (Label) row.lookup("#rowDuration");
            
            if (title != null) title.setText(s.getTitle());
            if (artist != null) artist.setText(s.getArtist());
            if (duration != null) duration.setText(formatTime(s.getDuration()));
            
            javafx.scene.layout.HBox.setHgrow(row, javafx.scene.layout.Priority.ALWAYS);
            if (row instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) row).setMaxWidth(Double.MAX_VALUE);
            }

            row.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"); 
            row.setOnMouseEntered(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: #2a2a2a;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"));

            row.setOnMouseClicked(e -> {
                System.out.println("▶️ Đang phát từ Profile: " + s.getTitle());
                MainController.getInstance().choiBaiHatMoi(s);
            });
            
            return row;
        } catch (IOException e) {
            Label lbl = new Label(s.getTitle() + " - " + s.getArtist());
            lbl.setStyle("-fx-text-fill: #b3b3b3; -fx-padding: 5; -fx-cursor: hand;");
            lbl.setOnMouseClicked(ev -> MainController.getInstance().choiBaiHatMoi(s));
            return lbl;
        }
    }
    
    private void addEmptyLabel(VBox container, String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #808080; -fx-font-size: 16px; -fx-padding: 10;"); 
        container.getChildren().add(l);
    }

    private String formatTime(double seconds) {
        int m = (int) seconds / 60;
        int s = (int) seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    @FXML
    private void handleLogout() {
        MainController.getInstance().userLogout(); 
    }
    
    @FXML
    private void openFavorites() {
        MainController.getInstance().hienThiManHinhFavorites();
    }

    @FXML
    private void openHistory() {
        MainController.getInstance().hienThiManHinhHistory();
    }
}