package com; // 1. SỬA: Đặt đúng package com

// 2. SỬA: Import SongLibrary từ com.users (Không phải musicPlayer)
import com.users.SongLibrary;

// Các import khác giữ nguyên
import com.musicPlayer.AudioPlayer;
import com.musicPlayer.Playlist;
import com.musicPlayer.Song;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.ScrollPane;

import java.io.IOException;
import java.util.List;

public class MainController {

    // --- KHAI BÁO GIAO DIỆN ---
    @FXML private BorderPane mainRoot;
    
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button nextButton;
    @FXML private Button prevButton;
    @FXML private Button shuffleButton;
    @FXML private Button repeatButton;
    
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label currentTimeLbl;
    @FXML private Label totalTimeLbl;

    @FXML private Label currentSongLabel;
    @FXML private Label currentArtistLabel;
    @FXML private ImageView miniThumbView;

    @FXML private StackPane discContainer; 

    @FXML private Button homeBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;

    // --- BIẾN LOGIC ---
    private AudioPlayer player;
    private SongLibrary library;
    private boolean dangKeoThanhTruot = false;

    public void initialize() {
        // Cài đặt Backend
        caiDatBackend();

        // Gắn sự kiện
        ganSuKienChoNut();

        // Hiển thị danh sách nhạc
        hienThiDanhSachNhac();
    }

    private void caiDatBackend() {
        library = SongLibrary.getInstance(); // Lấy từ com.users.SongLibrary
        
        Playlist danhSachTong = new Playlist("ThuVienCuaToi");
        List<Song> tatCaBaiHat = library.getAllSongs();
        for (Song s : tatCaBaiHat) {
            danhSachTong.addSong(s);
        }

        player = new AudioPlayer(danhSachTong);
        
        player.setOnSongEnd(() -> {
            Platform.runLater(this::capNhatGiaoDienDuoiCung);
        });
        
        daoTrangThaiNutPlay(false);
    }

    private void ganSuKienChoNut() {
        playButton.setOnAction(e -> xuLyPlay());
        pauseButton.setOnAction(e -> xuLyPause());

        nextButton.setOnAction(e -> {
            player.next();
            capNhatGiaoDienDuoiCung();
        });
        
        prevButton.setOnAction(e -> {
            player.previous();
            capNhatGiaoDienDuoiCung();
        });

        volumeSlider.valueProperty().addListener((obs, cu, moi) -> {
            player.setVolume(moi.doubleValue() / 100.0);
        });

        progressSlider.setOnMousePressed(e -> dangKeoThanhTruot = true);
        progressSlider.setOnMouseReleased(e -> {
            player.seek((int) progressSlider.getValue());
            dangKeoThanhTruot = false;
        });

        homeBtn.setOnAction(e -> hienThiDanhSachNhac());
    }

    private void hienThiDanhSachNhac() {
        FlowPane luoiBaiHat = new FlowPane();
        luoiBaiHat.setHgap(20);
        luoiBaiHat.setVgap(20);
        luoiBaiHat.setPadding(new Insets(20));
        luoiBaiHat.setStyle("-fx-background-color: #121212;");

        List<Song> dsBaiHat = library.getAllSongs();
        for (int i = 0; i < dsBaiHat.size(); i++) {
            Node theBaiHat = taoTheBaiHat(dsBaiHat.get(i), i);
            if (theBaiHat != null) {
                luoiBaiHat.getChildren().add(theBaiHat);
            }
        }

        ScrollPane thanhCuon = new ScrollPane(luoiBaiHat);
        thanhCuon.setFitToWidth(true);
        thanhCuon.setStyle("-fx-background: #121212; -fx-border-color: transparent;");

        mainRoot.setCenter(thanhCuon);
    }

    private Node taoTheBaiHat(Song baiHat, int viTriIndex) {
        try {
            // Lưu ý: Đường dẫn này phải đúng nơi ông để file MusicCard.fxml
            // Nếu file fxml nằm trong src/resources/MusicCard.fxml thì để như dưới:
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MusicCard.fxml"));
            VBox theGoc = loader.load();

            Label lbTenBai = (Label) theGoc.lookup("#songTitle");
            Label lbCaSi = (Label) theGoc.lookup("#songArtist");
            ImageView anhBia = (ImageView) theGoc.lookup("#songImage");
            Button nutPlayTrenThe = (Button) theGoc.lookup("#playBtn");

            if (lbTenBai != null) lbTenBai.setText(baiHat.getTitle());
            if (lbCaSi != null) lbCaSi.setText(baiHat.getArtist());
            
            try {
                anhBia.setImage(new Image(getClass().getResourceAsStream("/resources/icons/logo.png")));
            } catch (Exception e) {}

            nutPlayTrenThe.setOnAction(e -> {
                choiBaiHatCuThe(viTriIndex);
            });

            return theGoc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void xuLyPlay() {
        player.play();
        daoTrangThaiNutPlay(true);
        batDauDongBoThoiGian();
    }

    private void xuLyPause() {
        player.pause();
        daoTrangThaiNutPlay(false);
    }

    private void choiBaiHatCuThe(int index) {
        // Dừng player cũ
        if (player.getMediaPlayer() != null) {
            player.stop();
        }

        // Tạo playlist mới để đảm bảo có nhạc
        Playlist danhSachMoi = new Playlist("CurrentQueue");
        List<Song> dsBaiHat = library.getAllSongs();
        
        // Nạp toàn bộ bài hát vào playlist player
        for (Song s : dsBaiHat) {
            danhSachMoi.addSong(s);
        }
        player.setPlaylist(danhSachMoi);

        // --- FIX LOGIC TUA ĐẾN BÀI CẦN HÁT ---
        // Hack: Player hiện tại chưa có hàm setIndex, nên ta dùng vòng lặp next()
        // Reset về 0 trước (cần thêm method reset vào AudioPlayer hoặc làm thủ công)
        // Cách thủ công an toàn nhất hiện tại:
        // Re-new player luôn cho sạch
        player = new AudioPlayer(danhSachMoi);
        player.setOnSongEnd(() -> Platform.runLater(this::capNhatGiaoDienDuoiCung));

        // Next đến đúng bài i
        for (int i = 0; i < index; i++) {
            player.next();
        }
        
        xuLyPlay(); // Phát nhạc
        capNhatGiaoDienDuoiCung(); // Hiện thông tin

        MediaPlayer mp = player.getMediaPlayer();
        if (mp != null) {
            // 1. Khi nhạc load xong thì set Max cho thanh trượt
            // (Phải set lại vì bài mới độ dài nó khác bài cũ)
            mp.setOnReady(() -> {
                // Thay 'timeSlider' bằng tên biến Slider trong FXML của ông
                progressSlider.setMax(mp.getTotalDuration().toSeconds());
            });

            // 2. Nhạc chạy đến đâu, Slider chạy đến đó
            mp.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
                // Chỉ chạy khi ông không đang dùng chuột kéo nó
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newVal.toSeconds());
                }
                toMauThanhTruot(newVal.toSeconds(), mp.getTotalDuration().toSeconds());
            });
        }

        // Khi nhả chuột ra thì tua nhạc
        progressSlider.setOnMouseReleased(event -> {
            if (player.getMediaPlayer() != null) {
                player.getMediaPlayer().seek(Duration.seconds(progressSlider.getValue()));
            }
        });
    }

    private void daoTrangThaiNutPlay(boolean dangPhatNhac) {
        playButton.setVisible(!dangPhatNhac);
        pauseButton.setVisible(dangPhatNhac);
    }

    private void capNhatGiaoDienDuoiCung() {
        Song s = player.getCurrentSong();
        if (s != null) {
            currentSongLabel.setText(s.getTitle());
            currentArtistLabel.setText(s.getArtist());
            totalTimeLbl.setText(doiGiaySangPhut(s.getDuration()));
            progressSlider.setMax(s.getDuration());
            
            try {
                miniThumbView.setImage(new Image(getClass().getResourceAsStream("/resources/icons/logo.png")));
            } catch (Exception ignored) {}
        }
        daoTrangThaiNutPlay(player.isPlaying());
        batDauDongBoThoiGian();
    }

    private void batDauDongBoThoiGian() {
        if (player.getMediaPlayer() != null) {
            player.getMediaPlayer().currentTimeProperty().addListener((obs, thoiGianCu, thoiGianMoi) -> {
                if (!dangKeoThanhTruot) {
                    progressSlider.setValue(thoiGianMoi.toSeconds());
                    currentTimeLbl.setText(doiGiaySangPhut(thoiGianMoi.toSeconds()));
                }
            });
        }
    }

    private String doiGiaySangPhut(double giay) {
        int phut = (int) giay / 60;
        int soGiayLe = (int) giay % 60;
        return String.format("%02d:%02d", phut, soGiayLe);
    }

    @FXML
    public void handleVolumeUp(javafx.scene.input.SwipeEvent event) {
        // Tăng âm lượng thêm 10%
        double current = volumeSlider.getValue();
        volumeSlider.setValue(Math.min(100, current + 10));
    }

    @FXML
    public void handleVolumeDown(javafx.scene.input.SwipeEvent event) {
        // Giảm âm lượng đi 10%
        double current = volumeSlider.getValue();
        volumeSlider.setValue(Math.max(0, current - 10));
    }

    private void toMauThanhTruot(double currentSeconds, double totalSeconds) {
        if (totalSeconds > 0) {
            // Tính phần trăm đã chạy (từ 0 đến 100)
            double percentage = (currentSeconds / totalSeconds) * 100;
            
            // Tạo chuỗi CSS: Bên trái màu TRẮNG, bên phải màu XÁM ĐẬM
            // Ông có thể đổi #ffffff (trắng) và #404040 (xám) thành mã màu ông thích
            String style = String.format("-fx-background-color: linear-gradient(to right, #ffffff %.2f%%, #404040 %.2f%%);", percentage, percentage);
            
            // Tìm cái "đường ray" (track) của slider và áp dụng màu
            Node track = progressSlider.lookup(".track");
            if (track != null) {
                track.setStyle(style);
            }
        }
    }
}
