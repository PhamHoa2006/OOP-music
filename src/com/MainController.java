package com;

import com.users.SongLibrary;
import com.musicPlayer.AudioPlayer;
import com.musicPlayer.Playlist;
import com.musicPlayer.Song;
import com.extra.Timer;
import com.extra.TimerListener;
import com.extra.TimerDialogController;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle; 
import javafx.scene.paint.ImagePattern; 
import javafx.animation.RotateTransition;
import javafx.animation.Interpolator;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser; 

import java.util.Collections;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MainController {

    // --- KHAI BÁO FXML (Đảm bảo ID khớp với MainLayout.fxml) ---
    @FXML private BorderPane mainRoot;
    @FXML private VBox rightSidebar; 

    // Player Controls
    @FXML private Button playButton, pauseButton, nextButton, prevButton;
    @FXML private Button shuffleButton, repeatButton, likeBtn;
    @FXML private Slider progressSlider, volumeSlider;
    @FXML private Label currentTimeLbl, totalTimeLbl;
    @FXML private Label currentSongLabel, currentArtistLabel;
    @FXML private ImageView miniThumbView;

    // Disc View
    @FXML private StackPane discContainer; 
    @FXML private Circle outerDiscCircle, innerDiscCircle;
    @FXML private ImageView discIconView;

    // Navigation
    @FXML private Button homeBtn, favoritesBtn, historyBtn, top100Btn;
    @FXML private Button logoBtn, backBtn, forwardBtn;
    @FXML private TextField searchField;

    // Right Sidebar
    @FXML private ToggleButton nextTabBtn, relatedTabBtn;
    @FXML private ToggleGroup tabGroup;
    @FXML private ScrollPane queueScrollPane, relatedScrollPane;
    @FXML private VBox queueContainerVBox, relatedContainerVBox, queueTabContent;

    // Top Bar Actions
    @FXML private Button timerBtn;
    @FXML private Button uploadBtn;

    // --- LOGIC VARIABLES ---
    private AudioPlayer player;
    private SongLibrary library;
    private boolean isRepeat = false;
    private boolean dangKeoThanhTruot = false;
    private List<Song> favoriteSongs = new ArrayList<>();

    // UI State
    private ScrollPane libraryView;
    private Node savedRightSidebar;
    private RotateTransition discRotation;
    
    // Navigation History
    private Stack<Runnable> backStack = new Stack<>();
    private Stack<Runnable> forwardStack = new Stack<>();
    private Runnable currentViewAction; 

    // Timer
    private Timer sleepTimer;
    private Timeline uiUpdateTimeline; 

    public void initialize() {
        System.out.println("🚀 [DEBUG] Bắt đầu khởi tạo MainController...");

        // 1. Setup Logic & Data
        savedRightSidebar = rightSidebar; 
        caiDatBackend();                  
        setupDiscAnimation();             

        // 2. Setup Timer System
        setupTimerSystem();

        // 3. Setup Events
        ganSuKienChoNut();
        if (uploadBtn != null) uploadBtn.setOnAction(e -> handleUpload());

        // 4. CHẮC CHẮN MỞ MÀN HÌNH HOME
        // Reset lại view để đảm bảo nó được vẽ mới
        libraryView = null; 
        hienThiManHinhHome();
        
        // Lưu trạng thái history
        currentViewAction = this::hienThiManHinhHome;
        updateNavigationButtons();
        
        System.out.println("✅ [DEBUG] Khởi tạo hoàn tất. Màn hình Home đã được gọi.");
    }

    private void caiDatBackend() {
        library = SongLibrary.getInstance();
        Playlist danhSachTong = new Playlist("ThuVienCuaToi");
        List<Song> tatCaBaiHat = library.getAllSongs();
        
        System.out.println("📚 [DEBUG] Số lượng bài hát trong thư viện: " + tatCaBaiHat.size());

        for (Song s : tatCaBaiHat) {
            danhSachTong.addSong(s);
        }

        player = new AudioPlayer(danhSachTong);
        player.setOnSongEnd(this::handleSongEnd); 
        daoTrangThaiNutPlay(false);
    }

    // --- SETUP GIAO DIỆN HOME (QUAN TRỌNG) ---
    private void chuanBiGiaoDienLibrary() {
        VBox mainContent = new VBox();
        mainContent.setSpacing(30);
        mainContent.setPadding(new Insets(20, 20, 50, 20));
        // Đặt màu nền rõ ràng để tránh bị trong suốt
        mainContent.setStyle("-fx-background-color: #121212;"); 

        List<Song> allSongs = library.getAllSongs();

        if (allSongs == null || allSongs.isEmpty()) {
            Label emptyLbl = new Label("Thư viện trống. Hãy kiểm tra folder data/Music hoặc nhấn Upload!");
            emptyLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            mainContent.getChildren().add(emptyLbl);
        } else {
            // Chia section
            if (allSongs.size() > 0) {
                List<Song> section1 = allSongs.subList(0, Math.min(allSongs.size(), 5));
                mainContent.getChildren().add(taoMotHangNgang("Dành cho bạn", section1));
            }
            if (allSongs.size() > 5) {
                List<Song> section2 = allSongs.subList(5, Math.min(allSongs.size(), 10));
                mainContent.getChildren().add(taoMotHangNgang("Được nghe nhiều nhất", section2));
            }
            mainContent.getChildren().add(taoMotHangNgang("Nghe lại", allSongs));
        }

        libraryView = new ScrollPane(mainContent);
        libraryView.setFitToWidth(true);
        libraryView.setPannable(true);
        // Style trong suốt cho ScrollPane để lộ màu nền VBox
        libraryView.setStyle("-fx-background: #121212; -fx-background-color: transparent;");
        libraryView.getStyleClass().add("main-scroll-pane");
    }

    private VBox taoMotHangNgang(String tieuDe, List<Song> danhSachBai) {
        VBox sectionBox = new VBox();
        sectionBox.setSpacing(15);

        Label lblTitle = new Label(tieuDe);
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        HBox cardRow = new HBox();
        cardRow.setSpacing(20);
        cardRow.setPadding(new Insets(5));

        for (Song s : danhSachBai) {
            int realIndex = library.getAllSongs().indexOf(s); 
            Node card = taoTheBaiHat(s, realIndex);
            if (card != null) cardRow.getChildren().add(card);
        }

        ScrollPane rowScroller = new ScrollPane(cardRow);
        rowScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rowScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rowScroller.setFitToHeight(true);
        rowScroller.setPannable(true);
        rowScroller.setStyle("-fx-background-color: transparent;");
        rowScroller.getStyleClass().add("horizontal-scroll");

        sectionBox.getChildren().addAll(lblTitle, rowScroller);
        return sectionBox;
    }

    private Node taoTheBaiHat(Song baiHat, int viTriIndex) {
        try {
            // Kiểm tra đường dẫn FXML
            if (getClass().getResource("MusicCard.fxml") == null) {
                System.err.println("❌ [LỖI] Không tìm thấy file MusicCard.fxml tại cùng thư mục MainController!");
                return null;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("MusicCard.fxml"));
            VBox theGoc = loader.load();

            Label lbTenBai = (Label) theGoc.lookup("#songTitle");
            Label lbCaSi = (Label) theGoc.lookup("#songArtist");
            ImageView anhBia = (ImageView) theGoc.lookup("#songImage");
            Button nutPlayTrenThe = (Button) theGoc.lookup("#playBtn");

            if (lbTenBai != null) lbTenBai.setText(baiHat.getTitle());
            if (lbCaSi != null) lbCaSi.setText(baiHat.getArtist());
            
            try {
                // Thử load ảnh, nếu lỗi thì bỏ qua
                anhBia.setImage(new Image(getClass().getResourceAsStream("icons/logo.png")));
            } catch (Exception e) {}

            nutPlayTrenThe.setOnAction(e -> choiBaiHatCuThe(viTriIndex));
            theGoc.setOnMouseClicked(e -> choiBaiHatCuThe(viTriIndex));
            return theGoc;
        } catch (IOException e) {
            System.err.println("❌ [LỖI] Không thể load thẻ bài hát: " + baiHat.getTitle());
            e.printStackTrace();
            return null;
        }
    }

    // --- SETUP TIMER SYSTEM ---
    private void setupTimerSystem() {
        sleepTimer = new Timer();
        
        sleepTimer.addListener(new TimerListener() {
            @Override
            public void onTimerFinished() {
                Platform.runLater(() -> {
                    if (player != null && player.isPlaying()) xuLyPause();
                    stopCountdownUI();
                    System.out.println("⏰ Hết giờ! Đã tắt nhạc.");
                });
            }
            @Override
            public void onTimerCancelled() {
                Platform.runLater(() -> stopCountdownUI());
            }
        });

        timerBtn.setOnAction(e -> showTimerDialog());
    }

    private void showTimerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/TimerDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Hẹn giờ");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(timerBtn.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UTILITY);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/Style.css").toExternalForm());
            dialogStage.setScene(scene);

            TimerDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, sleepTimer.isActive(), (selectedMinutes) -> {
                if (selectedMinutes == -1) {
                    stopCountdownUI();
                    sleepTimer.cancelTimer();
                } else if (selectedMinutes > 0) {
                    startCountdownUI(selectedMinutes);
                }
            });
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountdownUI(int minutes) {
        sleepTimer.setTimer(minutes * 60);
        if (uiUpdateTimeline != null) uiUpdateTimeline.stop();
        
        uiUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            int remaining = sleepTimer.getTimeRemaining();
            if (remaining <= 0) {
                stopCountdownUI();
                return;
            }
            String timeText = String.format("%02d:%02d", remaining / 60, remaining % 60);
            timerBtn.setText(timeText);
            
            // Style: Text màu xanh, Chữ đậm
            timerBtn.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-color: transparent;"); 
            timerBtn.setContentDisplay(ContentDisplay.RIGHT);
            timerBtn.setGraphicTextGap(5);
        }));
        
        uiUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        uiUpdateTimeline.play();
    }

    private void stopCountdownUI() {
        if (uiUpdateTimeline != null) uiUpdateTimeline.stop();
        timerBtn.setText(""); 
        timerBtn.setStyle("-fx-background-color: transparent;"); 
        timerBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); 
    }

    // --- UPLOAD LOGIC ---
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn bài hát để tải lên");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a"));
        
        File selectedFile = fileChooser.showOpenDialog(uploadBtn.getScene().getWindow());

        if (selectedFile != null) {
            try {
                File desDir = new File("data/Music");
                if (!desDir.exists()) desDir.mkdirs();

                String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName().replaceAll("\\s+", "_");
                File destFile = new File(desDir, newFileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String title = selectedFile.getName().replace(".mp3", ""); 
                Song newSong = new Song(title, "Unknown Artist", "Local Upload", 0, "data/Music/" + newFileName);

                library.addSong(newSong);
                
                // Refresh UI
                chuanBiGiaoDienLibrary(); 
                hienThiManHinhHome(); 
                
                System.out.println("✅ Upload thành công: " + title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --- NAVIGATION & VIEW ---
    private void chuyenManHinh(Runnable viewMethod) {
        if (currentViewAction == viewMethod) return;
        if (currentViewAction != null) backStack.push(currentViewAction);
        forwardStack.clear(); 
        currentViewAction = viewMethod;
        viewMethod.run();
        updateNavigationButtons();
    }

    private void handleBackNav() {
        if (!backStack.isEmpty()) {
            Runnable prevView = backStack.pop();
            forwardStack.push(currentViewAction);
            currentViewAction = prevView;
            prevView.run();
            updateNavigationButtons();
        }
    }

    private void handleForwardNav() {
        if (!forwardStack.isEmpty()) {
            Runnable nextView = forwardStack.pop();
            backStack.push(currentViewAction);
            currentViewAction = nextView;
            nextView.run();
            updateNavigationButtons();
        }
    }

    private void updateNavigationButtons() {
        backBtn.setDisable(backStack.isEmpty());
        forwardBtn.setDisable(forwardStack.isEmpty());
        backBtn.setOpacity(backStack.isEmpty() ? 0.3 : 1.0);
        forwardBtn.setOpacity(forwardStack.isEmpty() ? 0.3 : 1.0);
    }

    private void hienThiManHinhHome() {
        if (libraryView == null) chuanBiGiaoDienLibrary();
        mainRoot.setCenter(libraryView);
        mainRoot.setRight(null); 
    }

    private void hienThiManHinhPlayer() {
        if (discContainer != null) {
            mainRoot.setCenter(discContainer);
            mainRoot.setRight(savedRightSidebar); 
        }
    }
    
    private void hienThiManHinhFavorites() {
        VBox mainContent = new VBox();
        mainContent.setSpacing(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #121212;"); 

        Label title = new Label("Bài Hát Yêu Thích");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ImageView heartIcon = new ImageView(new Image(getClass().getResourceAsStream("icons/heart.png")));
        heartIcon.setFitHeight(30); 
        heartIcon.setFitWidth(30);
        
        HBox header = new HBox(15, heartIcon, title);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FlowPane gridPane = new FlowPane();
        gridPane.setHgap(20); 
        gridPane.setVgap(20); 
        gridPane.setPadding(new Insets(10, 0, 0, 0));

        if (favoriteSongs.isEmpty()) {
            Label emptyMsg = new Label("Chưa có bài hát nào được thả tim.");
            emptyMsg.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 16px;");
            mainContent.getChildren().addAll(header, emptyMsg);
        } else {
            for (Song s : favoriteSongs) {
                int realIndex = library.getAllSongs().indexOf(s);
                Node card = taoTheBaiHat(s, realIndex); 
                if (card != null) gridPane.getChildren().add(card);
            }
            mainContent.getChildren().addAll(header, gridPane);
        }

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: #121212; -fx-background-color: transparent;");
        scrollPane.getStyleClass().add("main-scroll-pane");

        mainRoot.setCenter(scrollPane);
        mainRoot.setRight(null);
    }

    // --- OTHER UI ELEMENTS ---
    private void ganSuKienChoNut() {
        playButton.setOnAction(e -> xuLyPlay());
        pauseButton.setOnAction(e -> xuLyPause());
        nextButton.setOnAction(e -> nextSong());
        prevButton.setOnAction(e -> {
            player.previous();
            capNhatGiaoDienDuoiCung();
        });
        repeatButton.setOnAction(e -> xulyRepeat());
        if (likeBtn != null) likeBtn.setOnAction(e -> toggleLike());

        volumeSlider.valueProperty().addListener((obs, cu, moi) -> {
            if (player != null) player.setVolume(moi.doubleValue() / 100.0);
        });

        progressSlider.setOnMousePressed(e -> dangKeoThanhTruot = true);
        progressSlider.setOnMouseReleased(e -> {
            if (player != null) player.seek((int) progressSlider.getValue());
            dangKeoThanhTruot = false;
        });

        homeBtn.setOnAction(e -> chuyenManHinh(this::hienThiManHinhHome));
        logoBtn.setOnAction(e -> chuyenManHinh(this::hienThiManHinhHome));
        favoritesBtn.setOnAction(e -> chuyenManHinh(this::hienThiManHinhFavorites));
        
        backBtn.setOnAction(e -> handleBackNav());
        forwardBtn.setOnAction(e -> handleForwardNav());

        nextTabBtn.setOnAction(e -> switchSidebarTab(true));
        relatedTabBtn.setOnAction(e -> switchSidebarTab(false));
    }

    private void switchSidebarTab(boolean isNextTab) {
        if (queueTabContent == null || relatedScrollPane == null) return;
        Node toShow = isNextTab ? queueTabContent : relatedScrollPane;
        Node toHide = isNextTab ? relatedScrollPane : queueTabContent;

        toHide.setVisible(false);
        toHide.setManaged(false);

        if (!isNextTab) loadRelatedSongs(); 
        else updateQueueView();

        toShow.setVisible(true);
        toShow.setManaged(true);
        toShow.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toShow);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    // --- PLAYER LOGIC ---
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
            xyLyHieuUngXoay(false);
        }
    }
    
    public void nextSong() {
        player.next();
        capNhatGiaoDienDuoiCung();
        player.play();
    }

    private void handleSongEnd() {
        Platform.runLater(() -> {
            if (isRepeat) {
                player.seek(0);
                player.play();
                capNhatGiaoDienDuoiCung();
            } else {
                nextSong();
            }
        });
    }

    private void xulyRepeat() {
        isRepeat = !isRepeat;
        if (isRepeat) repeatButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); 
        else repeatButton.setStyle(null); 
    }
    
    private void toggleLike() {
        Song currentSong = player.getCurrentSong();
        if (currentSong == null) return;
        if (favoriteSongs.contains(currentSong)) {
            favoriteSongs.remove(currentSong);
        } else {
            favoriteSongs.add(currentSong);
        }
        updateLikeButtonState();
        if (currentViewAction != null && currentViewAction.toString().contains("hienThiManHinhFavorites")) {
            hienThiManHinhFavorites();
        }
    }
    
    private void updateLikeButtonState() {
        if (likeBtn == null) return;
        Song s = player.getCurrentSong();
        if (s != null && favoriteSongs.contains(s)) {
            likeBtn.setStyle("-fx-opacity: 1.0; -fx-effect: dropshadow(three-pass-box, rgba(29,185,84,0.8), 10, 0, 0, 0);");
        } else {
            likeBtn.setStyle("-fx-opacity: 0.5;");
        }
    }

    private void choiBaiHatCuThe(int index) {
        chuyenManHinh(this::hienThiManHinhPlayer); 
        if (player.getMediaPlayer() != null) player.stop();

        Playlist danhSachMoi = new Playlist("CurrentQueue");
        List<Song> dsBaiHat = library.getAllSongs();
        for (Song s : dsBaiHat) danhSachMoi.addSong(s);
        
        player = new AudioPlayer(danhSachMoi);
        player.setOnSongEnd(this::handleSongEnd);
        
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
        updateLikeButtonState(); 
        if (nextTabBtn.isSelected()) updateQueueView();
    }

    private void capNhatAnhDiaNhac(Song s) {
        if (outerDiscCircle == null) return;
        try {
            Image img = new Image(getClass().getResourceAsStream("icons/logo.png")); 
            outerDiscCircle.setFill(new ImagePattern(img));
        } catch (Exception e) {
            outerDiscCircle.setStyle("-fx-fill: #e2e6e9;");
        }
    }

    private void updateQueueView() {
        if (queueContainerVBox == null || player == null) return;
        queueContainerVBox.getChildren().clear();
        List<Song> upcomingSongs = new ArrayList<>();
        Playlist currentPlaylist = player.getPlaylist();
        Song currentSong = player.getCurrentSong();

        if (currentPlaylist != null && currentSong != null) {
            List<Song> all = currentPlaylist.getSongs();
            int currentIndex = all.indexOf(currentSong);
            if (currentIndex >= 0 && currentIndex < all.size() - 1) {
                upcomingSongs.addAll(all.subList(currentIndex + 1, all.size()));
            }
        }
        if (upcomingSongs.size() < 10) {
            List<Song> randomSongs = library.getAllSongs();
            Collections.shuffle(randomSongs);
            for (Song s : randomSongs) {
                if (!upcomingSongs.contains(s) && !s.equals(currentSong)) {
                    upcomingSongs.add(s);
                    if (upcomingSongs.size() >= 15) break;
                }
            }
        }
        for (Song s : upcomingSongs) {
            Node row = taoDongBaiHat(s);
            if (row != null) queueContainerVBox.getChildren().add(row);
        }
    }

    private void loadRelatedSongs() {
        if (relatedContainerVBox == null) return;
        relatedContainerVBox.getChildren().clear();
        List<Song> randomSongs = new ArrayList<>(library.getAllSongs());
        Collections.shuffle(randomSongs);
        int count = 0;
        for (Song s : randomSongs) {
            if (player.getCurrentSong() != null && s.equals(player.getCurrentSong())) continue;
            Node row = taoDongBaiHat(s);
            if (row != null) {
                relatedContainerVBox.getChildren().add(row);
                count++;
            }
            if (count >= 20) break;
        }
    }

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

            row.setOnMouseClicked(e -> choiBaiHatMoi(s));
            return row;
        } catch (IOException e) { return null; }
    }

    private void choiBaiHatMoi(Song s) {
        Playlist p = new Playlist("Temp");
        p.addSong(s);
        player.setPlaylist(p);
        player.play();
        capNhatGiaoDienDuoiCung();
        chuyenManHinh(this::hienThiManHinhPlayer);
        updateQueueView();
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

    private void toMauThanhTruot(double currentSeconds, double totalSeconds) {
        if (totalSeconds > 0) {
            double percentage = (currentSeconds / totalSeconds) * 100;
            String style = String.format("-fx-background-color: linear-gradient(to right, #ffffff %.2f%%, #404040 %.2f%%);", percentage, percentage);
            Node track = progressSlider.lookup(".track");
            if (track != null) track.setStyle(style);
        }
    }

    private void setupDiscAnimation() {
        discRotation = new RotateTransition(Duration.seconds(20), outerDiscCircle);
        discRotation.setByAngle(360); 
        discRotation.setCycleCount(RotateTransition.INDEFINITE); 
        discRotation.setInterpolator(Interpolator.LINEAR); 
    }

    private void xyLyHieuUngXoay(boolean isPlaying) {
        if (discRotation == null) return;
        if (isPlaying) {
            if (discRotation.getStatus() != javafx.animation.Animation.Status.RUNNING) discRotation.play();
        } else {
            discRotation.pause();
        }
    }
    
    @FXML public void handleVolumeUp(javafx.scene.input.SwipeEvent event) {
        double current = volumeSlider.getValue();
        volumeSlider.setValue(Math.min(100, current + 10));
    }

    @FXML public void handleVolumeDown(javafx.scene.input.SwipeEvent event) {
        double current = volumeSlider.getValue();
        volumeSlider.setValue(Math.max(0, current - 10));
    }
}