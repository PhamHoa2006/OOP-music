package com;

import com.users.SongLibrary;
import com.musicPlayer.AudioPlayer;
import com.musicPlayer.Playlist;
import com.musicPlayer.Song;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import javafx.scene.shape.Circle; 
import javafx.scene.paint.ImagePattern; 
import javafx.animation.RotateTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ScrollPane;
import javafx.animation.FadeTransition;
import javafx.scene.Node;

import java.util.Collections;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;

public class MainController {

    // --- KHAI BÁO GIAO DIỆN ---
    @FXML private BorderPane mainRoot;
    
    // THÊM: Biến này để quản lý cột bên phải
    @FXML private VBox rightSidebar; 

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

    @FXML private Circle outerDiscCircle; // Vòng tròn lớn (sẽ chứa ảnh)
    @FXML private Circle innerDiscCircle; // Vòng tròn đen (giữ nguyên)
    @FXML private ImageView discIconView; // Hình nốt nhạc (giữ nguyên)

    @FXML private ToggleButton nextTabBtn;
    @FXML private ToggleButton relatedTabBtn;
    @FXML private ToggleGroup tabGroup;

    @FXML private ScrollPane queueScrollPane;
    @FXML private VBox queueContainerVBox;
    @FXML private ScrollPane relatedScrollPane;
    @FXML private VBox relatedContainerVBox;
    @FXML private VBox queueTabContent;

    // --- BIẾN LOGIC ---
    private AudioPlayer player;
    private SongLibrary library;
    private boolean dangKeoThanhTruot = false;

    // Biến lưu giữ giao diện để tráo đổi
    private ScrollPane libraryView; // Màn hình Home (DS bài hát)
    private Node savedRightSidebar; // Lưu cái cột bên phải để dùng lại

    private RotateTransition discRotation; // Biến để quản lý việc xoay

    public void initialize() {
        // 0. Lưu lại cái Sidebar bên phải vào bộ nhớ trước khi ẩn nó đi
        savedRightSidebar = rightSidebar;

        // 1. Cài đặt Backend
        caiDatBackend();

        // 2. Chuẩn bị giao diện danh sách nhạc
        chuanBiGiaoDienLibrary();

        // 3. Gắn sự kiện
        ganSuKienChoNut();

        // 4. Mặc định mở lên là Home (Ẩn cột phải)
        hienThiManHinhHome();

        setupDiscAnimation();

        nextTabBtn.setOnAction(e -> switchSidebarTab(true));
        relatedTabBtn.setOnAction(e -> switchSidebarTab(false));
    }

    private void switchSidebarTab(boolean isNextTab) {
        // Check null để tránh lỗi
        if (queueTabContent == null || relatedScrollPane == null) return;

        // 1. Xác định cái nào cần hiện (toShow), cái nào cần ẩn (toHide)
        Node toShow = isNextTab ? queueTabContent : relatedScrollPane;
        Node toHide = isNextTab ? relatedScrollPane : queueTabContent;

        // 2. Ẩn cái cũ đi ngay lập tức
        toHide.setVisible(false);
        toHide.setManaged(false); // Gỡ khỏi layout để không chiếm chỗ

        // 3. Load dữ liệu cho cái mới (Làm lúc này để khi hiện lên là có dữ liệu luôn)
        if (!isNextTab) {
            loadRelatedSongs(); 
        } else {
            updateQueueView();
        }

        // 4. Chuẩn bị hiệu ứng cho cái mới
        toShow.setVisible(true);
        toShow.setManaged(true);
        toShow.setOpacity(0); // Đặt về trong suốt trước khi diễn

        // 5. Chạy hiệu ứng Fade In (Hiện dần trong 200 mili giây)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toShow);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void caiDatBackend() {
        library = SongLibrary.getInstance();
        Playlist danhSachTong = new Playlist("ThuVienCuaToi");
        List<Song> tatCaBaiHat = library.getAllSongs();
        for (Song s : tatCaBaiHat) {
            danhSachTong.addSong(s);
        }

        player = new AudioPlayer(danhSachTong);
        player.setOnSongEnd(() -> Platform.runLater(this::capNhatGiaoDienDuoiCung));
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
            if (player != null) player.setVolume(moi.doubleValue() / 100.0);
        });

        progressSlider.setOnMousePressed(e -> dangKeoThanhTruot = true);
        progressSlider.setOnMouseReleased(e -> {
            if (player != null) player.seek((int) progressSlider.getValue());
            dangKeoThanhTruot = false;
        });

        // Bấm Home -> Về giao diện danh sách, ẨN CỘT PHẢI
        homeBtn.setOnAction(e -> hienThiManHinhHome());
    }

    // --- LOGIC CHUYỂN MÀN HÌNH ---

    // Chế độ 1: Home View (Chỉ có danh sách nhạc, KHÔNG CÓ CỘT PHẢI)
    private void hienThiManHinhHome() {
        if (libraryView == null) chuanBiGiaoDienLibrary();
        
        mainRoot.setCenter(libraryView); // Giữa là list nhạc
        mainRoot.setRight(null);         // Phải là NULL (Ẩn đi) -> BorderPane tự dãn Center ra
    }

    // Chế độ 2: Player View (Có đĩa nhạc + CÓ CỘT PHẢI)
    private void hienThiManHinhPlayer() {
        if (discContainer != null) {
            mainRoot.setCenter(discContainer); // Giữa là đĩa nhạc
            mainRoot.setRight(savedRightSidebar); // Lắp lại cột phải vào
        }
    }

    private void chuanBiGiaoDienLibrary() {
        // VBox chính chứa tất cả các hàng ngang (xếp dọc từ trên xuống)
        VBox mainContent = new VBox();
        mainContent.setSpacing(30); // Khoảng cách giữa các mục lớn
        mainContent.setPadding(new Insets(20, 20, 50, 20)); // Căn lề
        mainContent.setStyle("-fx-background-color: #121212;"); 

        // Lấy toàn bộ bài hát
        List<Song> allSongs = library.getAllSongs();

        // MỤC 1: Dành cho User (Lấy 5 bài đầu)
        if (allSongs.size() > 0) {
            List<Song> section1 = allSongs.subList(0, Math.min(allSongs.size(), 5));
            mainContent.getChildren().add(taoMotHangNgang("Dành cho bạn", section1));
        }

        // MỤC 2: Gợi ý hôm nay (Lấy các bài tiếp theo nếu có)
        if (allSongs.size() > 5) {
            List<Song> section2 = allSongs.subList(5, Math.min(allSongs.size(), 10));
            mainContent.getChildren().add(taoMotHangNgang("Những ca khúc được nghe nhiều nhất", section2));
        }

        // MỤC 3: Tất cả bài hát (Xếp ngang hết)
        mainContent.getChildren().add(taoMotHangNgang("Nghe lại", allSongs));

        // Bọc VBox chính vào trong ScrollPane dọc (để cuộn toàn trang)
        libraryView = new ScrollPane(mainContent);
        libraryView.setFitToWidth(true); // Để nó dãn full chiều ngang
        libraryView.setPannable(true);   // Cho phép dùng chuột kéo
        
        // Style cho ScrollPane chính (trong suốt, ẩn viền)
        libraryView.setStyle("-fx-background: #121212; -fx-background-color: transparent; -fx-border-color: transparent;");
        libraryView.getStyleClass().add("main-scroll-pane");
    }

    // Hàm phụ trợ: Tạo ra 1 Block giao diện gồm: Label Tiêu đề + ScrollPane Ngang
    private VBox taoMotHangNgang(String tieuDe, List<Song> danhSachBai) {
        VBox sectionBox = new VBox();
        sectionBox.setSpacing(15); // Khoảng cách giữa Tiêu đề và Hàng thẻ

        // 1. Tạo Tiêu đề (Label)
        Label lblTitle = new Label(tieuDe);
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
        
        // 2. Tạo Hàng ngang chứa các thẻ (HBox)
        javafx.scene.layout.HBox cardRow = new javafx.scene.layout.HBox();
        cardRow.setSpacing(20); // Khoảng cách giữa các thẻ
        cardRow.setPadding(new Insets(5)); // Chừa chút lề để bóng đổ không bị cắt

        // Tạo thẻ cho từng bài hát
        for (int i = 0; i < danhSachBai.size(); i++) {
            // Lưu ý: index thực tế cần tính toán lại nếu ông muốn logic Play đúng bài trong list tổng
            // Nhưng để đơn giản demo giao diện, ta cứ truyền index tạm hoặc tìm index thực trong library
            Song s = danhSachBai.get(i);
            int realIndex = library.getAllSongs().indexOf(s); 
            
            Node card = taoTheBaiHat(s, realIndex);
            if (card != null) {
                cardRow.getChildren().add(card);
            }
        }

        // 3. Bọc Hàng ngang vào ScrollPane (để cuộn ngang được)
        ScrollPane rowScroller = new ScrollPane(cardRow);
        rowScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Tắt thanh cuộn dọc
        rowScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Hiện thanh ngang khi cần
        rowScroller.setFitToHeight(true); // Ôm sát chiều cao
        rowScroller.setPannable(true);    // Kéo chuột được
        
        // CSS Style trực tiếp để ẩn nền xám
        rowScroller.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        
        // Thêm class để lát nữa chỉnh CSS cho thanh cuộn ngang nó đẹp (hoặc ẩn đi)
        rowScroller.getStyleClass().add("horizontal-scroll");

        // Gép lại: Tiêu đề trên, Scroller dưới
        sectionBox.getChildren().addAll(lblTitle, rowScroller);
        
        return sectionBox;
    }

    private Node taoTheBaiHat(Song baiHat, int viTriIndex) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MusicCard.fxml"));
            VBox theGoc = loader.load();

            Label lbTenBai = (Label) theGoc.lookup("#songTitle");
            Label lbCaSi = (Label) theGoc.lookup("#songArtist");
            ImageView anhBia = (ImageView) theGoc.lookup("#songImage");
            Button nutPlayTrenThe = (Button) theGoc.lookup("#playBtn");

            if (lbTenBai != null) lbTenBai.setText(baiHat.getTitle());
            if (lbCaSi != null) lbCaSi.setText(baiHat.getArtist());
            
            try {
                anhBia.setImage(new Image(getClass().getResourceAsStream("icons/logo.png")));
            } catch (Exception e) {}

            nutPlayTrenThe.setOnAction(e -> choiBaiHatCuThe(viTriIndex));
            theGoc.setOnMouseClicked(e -> choiBaiHatCuThe(viTriIndex));

            return theGoc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void xuLyPlay() {
        if (player != null) {
            player.play();
            daoTrangThaiNutPlay(true);
            batDauDongBoThoiGian();
            xyLyHieuUngXoay(true);
        }
    }

    private void xuLyPause() {
        if (player != null) {
            player.pause();
            daoTrangThaiNutPlay(false);
            xyLyHieuUngXoay(false); // <--- DỪNG QUAY
        }
    }

    private void choiBaiHatCuThe(int index) {
        // 1. Chuyển sang giao diện Player (Có cột phải)
        hienThiManHinhPlayer();

        // 2. Logic phát nhạc
        if (player.getMediaPlayer() != null) player.stop();

        Playlist danhSachMoi = new Playlist("CurrentQueue");
        List<Song> dsBaiHat = library.getAllSongs();
        for (Song s : dsBaiHat) danhSachMoi.addSong(s);
        
        player = new AudioPlayer(danhSachMoi);
        player.setOnSongEnd(() -> Platform.runLater(this::capNhatGiaoDienDuoiCung));

        for (int i = 0; i < index; i++) player.next();
        
        xuLyPlay();
        capNhatGiaoDienDuoiCung();

        MediaPlayer mp = player.getMediaPlayer();
        if (mp != null) {
            mp.setOnReady(() -> progressSlider.setMax(mp.getTotalDuration().toSeconds()));
            mp.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
                if (!dangKeoThanhTruot) progressSlider.setValue(newVal.toSeconds());
                toMauThanhTruot(newVal.toSeconds(), mp.getTotalDuration().toSeconds());
            });
        }

        updateQueueView();
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
            capNhatAnhDiaNhac(s);
        }
        daoTrangThaiNutPlay(player.isPlaying());
        batDauDongBoThoiGian();

        xyLyHieuUngXoay(player.isPlaying());
        if (nextTabBtn.isSelected()) updateQueueView();
    }

    private void capNhatAnhDiaNhac(Song s) {
        if (outerDiscCircle == null) return;
        
        try {
            // Logic lấy ảnh (Tạm thời dùng logo mặc định, sau này bạn thay bằng s.getImagePath())
            // Nếu bạn có ảnh bài hát thật thì dùng: new Image(s.getImagePath())
            Image img = new Image(getClass().getResourceAsStream("icons/logo.png")); 
            
            // Dùng ImagePattern để tô ảnh vào hình tròn
            outerDiscCircle.setFill(new ImagePattern(img));
            
        } catch (Exception e) {
            // Nếu lỗi thì để màu xám như cũ
            outerDiscCircle.setStyle("-fx-fill: #e2e6e9;");
        }
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

    @FXML public void handleVolumeUp(javafx.scene.input.SwipeEvent event) {
        double current = volumeSlider.getValue();
        volumeSlider.setValue(Math.min(100, current + 10));
    }

    @FXML public void handleVolumeDown(javafx.scene.input.SwipeEvent event) {
        double current = volumeSlider.getValue();
        volumeSlider.setValue(Math.max(0, current - 10));
    }

    private void toMauThanhTruot(double currentSeconds, double totalSeconds) {
        if (totalSeconds > 0) {
            double percentage = (currentSeconds / totalSeconds) * 100;
            String style = String.format("-fx-background-color: linear-gradient(to right, #ffffff %.2f%%, #404040 %.2f%%);", percentage, percentage);
            Node track = progressSlider.lookup(".track");
            if (track != null) track.setStyle(style);
        }
    }

    private void setupDiscAnimation() {
        // Tạo hiệu ứng xoay cho cả cái StackPane chứa đĩa (discContainer)
        // Xoay 1 vòng (360 độ) trong 10 giây
        discRotation = new RotateTransition(Duration.seconds(20), outerDiscCircle);
        
        discRotation.setByAngle(360); // Quay 360 độ
        discRotation.setCycleCount(RotateTransition.INDEFINITE); // Quay mãi mãi
        discRotation.setInterpolator(Interpolator.LINEAR); // Quay đều (không nhanh chậm)
    }

    // Hàm điều khiển quay/dừng
    private void xyLyHieuUngXoay(boolean isPlaying) {
        if (discRotation == null) return;

        if (isPlaying) {
            // Nếu đang quay dở thì chạy tiếp, nếu chưa thì bắt đầu
            if (discRotation.getStatus() != javafx.animation.Animation.Status.RUNNING) {
                discRotation.play();
            }
        } else {
            discRotation.pause(); // Dừng lại ở góc hiện tại
        }
    }

    private void updateQueueView() {
        if (queueContainerVBox == null || player == null) return;
        queueContainerVBox.getChildren().clear();

        List<Song> upcomingSongs = new ArrayList<>();
        Playlist currentPlaylist = player.getPlaylist();
        Song currentSong = player.getCurrentSong();

        // 1. Lấy các bài còn lại trong Playlist hiện tại
        if (currentPlaylist != null && currentSong != null) {
            List<Song> all = currentPlaylist.getSongs();
            int currentIndex = all.indexOf(currentSong);
            
            // Lấy từ bài tiếp theo đến hết
            if (currentIndex >= 0 && currentIndex < all.size() - 1) {
                upcomingSongs.addAll(all.subList(currentIndex + 1, all.size()));
            }
        }

        // 2. Nếu danh sách ít quá (dưới 10 bài), lấy thêm Random bù vào
        if (upcomingSongs.size() < 10) {
            List<Song> randomSongs = library.getAllSongs();
            Collections.shuffle(randomSongs);
            for (Song s : randomSongs) {
                if (!upcomingSongs.contains(s) && !s.equals(currentSong)) {
                    upcomingSongs.add(s);
                    if (upcomingSongs.size() >= 15) break; // Lấy đủ 15 bài thì thôi
                }
            }
        }

        // 3. Render lên giao diện
        for (Song s : upcomingSongs) {
            Node row = taoDongBaiHat(s);
            if (row != null) queueContainerVBox.getChildren().add(row);
        }
    }

    private void loadRelatedSongs() {
        if (relatedContainerVBox == null) return;
        relatedContainerVBox.getChildren().clear();

        // Luôn lấy Random 20 bài
        List<Song> randomSongs = new ArrayList<>(library.getAllSongs());
        Collections.shuffle(randomSongs);
        
        int count = 0;
        for (Song s : randomSongs) {
            // Tránh hiện bài đang phát
            if (player.getCurrentSong() != null && s.equals(player.getCurrentSong())) continue;
            
            Node row = taoDongBaiHat(s);
            if (row != null) {
                relatedContainerVBox.getChildren().add(row);
                count++;
            }
            if (count >= 20) break;
        }
    }

    // --- HÀM TẠO DÒNG BÀI HÁT (Giống MusicCard nhưng dùng SongRow.fxml) ---
    private Node taoDongBaiHat(Song s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SongRow.fxml"));
            Node row = loader.load();

            Label lbTitle = (Label) row.lookup("#rowTitle");
            Label lbArtist = (Label) row.lookup("#rowArtist");
            Label lbDuration = (Label) row.lookup("#rowDuration");
            ImageView img = (ImageView) row.lookup("#rowImg");

            if (lbTitle != null) lbTitle.setText(s.getTitle());
            if (lbArtist != null) lbArtist.setText(s.getArtist());
            if (lbDuration != null) lbDuration.setText(doiGiaySangPhut(s.getDuration()));
            
            try {
                if (img != null) img.setImage(new Image(getClass().getResourceAsStream("icons/logo.png")));
            } catch (Exception e) {}

            // Bấm vào dòng thì phát bài đó
            row.setOnMouseClicked(e -> {
                // Logic phát ngay lập tức bài này
                choiBaiHatMoi(s); 
            });

            return row;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hàm phụ trợ để chơi 1 bài bất kỳ từ sidebar
    private void choiBaiHatMoi(Song s) {
        // Tạo playlist tạm thời chỉ chứa bài đó (hoặc thêm vào queue tùy logic bạn muốn)
        // Ở đây làm đơn giản: Phát luôn bài đó
        Playlist p = new Playlist("Temp");
        p.addSong(s);
        player.setPlaylist(p);
        player.play();
        
        // Cập nhật lại UI
        capNhatGiaoDienDuoiCung();
        hienThiManHinhPlayer(); // Chuyển sang màn hình đĩa xoay
        
        // Cập nhật lại list Next/Related
        updateQueueView();
    }
}