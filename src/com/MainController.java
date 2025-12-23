package com;

import com.users.SongLibrary;
import com.musicPlayer.AudioPlayer;
import com.musicPlayer.Playlist;
import com.musicPlayer.Song;
import com.extra.Timer;
import com.extra.TimerListener;
import com.extra.TimerDialogController;
import com.users.History;
import com.musicPlayer.CreatePlaylistController;
import com.musicPlayer.UploadDialogController;
import java.util.function.BiConsumer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
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
import javafx.scene.media.Media;       // <--- THÊM DÒNG NÀY
import javafx.scene.media.MediaPlayer;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

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
    @FXML private ListView<Playlist> playlistListView; // List Playlist bên trái

    // Top Bar Actions
    @FXML private Button timerBtn;
    @FXML private Button uploadBtn;
    @FXML private Button newPlaylistBtn;
    @FXML private ComboBox<String> privacyBox;

    // --- LOGIC VARIABLES ---
    private AudioPlayer player;
    private SongLibrary library;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
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

    private History historyManager;
    private TimerDialogController currentTimerDialog;
    private boolean isUpdatingTimer = false;

    public void initialize() {
        System.out.println("🚀 [DEBUG] Bắt đầu khởi tạo MainController...");

        // 1. Setup Logic & Data
        savedRightSidebar = rightSidebar; 
        caiDatBackend();                  
        setupDiscAnimation();             

        // 2. Setup Timer System & Sidebar Playlist
        setupTimerSystem();
        setupPlaylistListView();

        // 3. Setup Events
        ganSuKienChoNut();
        if (uploadBtn != null) uploadBtn.setOnAction(e -> handleUpload());
        
        // 4. CHẮC CHẮN MỞ MÀN HÌNH HOME KHI KHỞI ĐỘNG
        libraryView = null; 
        hienThiManHinhHome();
        
        // Lưu trạng thái history ban đầu
        currentViewAction = this::hienThiManHinhHome;
        updateNavigationButtons();
        
        System.out.println("✅ [DEBUG] Khởi tạo hoàn tất.");
    }

    private void caiDatBackend() {
        library = SongLibrary.getInstance();
        historyManager = new History();
        Playlist danhSachTong = new Playlist("ThuVienCuaToi");
        List<Song> tatCaBaiHat = library.getAllSongs();
        
        for (Song s : tatCaBaiHat) {
            danhSachTong.addSong(s);
        }

        player = new AudioPlayer(danhSachTong);
        player.setOnSongEnd(this::handleSongEnd); 
        daoTrangThaiNutPlay(false);
    }

    // --- SETUP GIAO DIỆN HOME ---
    private void chuanBiGiaoDienLibrary() {
        VBox mainContent = new VBox();
        mainContent.setSpacing(30);
        mainContent.setPadding(new Insets(20, 20, 50, 20));
        mainContent.setStyle("-fx-background-color: #121212;"); 

        List<Song> allSongs = library.getAllSongs();

        if (allSongs == null || allSongs.isEmpty()) {
            Label emptyLbl = new Label("Thư viện trống. Hãy nhấn Upload để thêm nhạc!");
            emptyLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            mainContent.getChildren().add(emptyLbl);
        } else {
            // Chia section hiển thị
            if (allSongs.size() > 0) {
                List<Song> section1 = allSongs.subList(0, Math.min(allSongs.size(), 5));
                mainContent.getChildren().add(taoMotHangNgang("Dành cho bạn", section1));
            }
            if (allSongs.size() > 5) {
                List<Song> section2 = allSongs.subList(5, Math.min(allSongs.size(), 10));
                mainContent.getChildren().add(taoMotHangNgang("Được nghe nhiều nhất", section2));
            }
            mainContent.getChildren().add(taoMotHangNgang("Tất cả bài hát", allSongs));
        }

        libraryView = new ScrollPane(mainContent);
        libraryView.setFitToWidth(true);
        libraryView.setPannable(true);
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
            return null;
        }
    }

    // --- SỰ KIỆN NÚT BẤM (GỌN GÀNG) ---
    private void ganSuKienChoNut() {
        // --- CÁC NÚT ĐIỀU KHIỂN NHẠC (Giữ nguyên) ---
        playButton.setOnAction(e -> xuLyPlay());
        pauseButton.setOnAction(e -> xuLyPause());
        nextButton.setOnAction(e -> nextSong());
        prevButton.setOnAction(e -> {
            player.previous();
            capNhatGiaoDienDuoiCung();
        });
        repeatButton.setOnAction(e -> xulyRepeat());
        if (likeBtn != null) likeBtn.setOnAction(e -> toggleLike());
        shuffleButton.setOnAction(e -> toggleShuffle()); 

        // --- SLIDER (Giữ nguyên) ---
        volumeSlider.valueProperty().addListener((obs, cu, moi) -> {
            if (player != null) player.setVolume(moi.doubleValue() / 100.0);
        });

        progressSlider.setOnMousePressed(e -> dangKeoThanhTruot = true);
        progressSlider.setOnMouseReleased(e -> {
            if (player != null) player.seek((int) progressSlider.getValue());
            dangKeoThanhTruot = false;
        });

        // --- [QUAN TRỌNG] SỬA LỖI ĐIỀU HƯỚNG TẠI ĐÂY ---
        
        // 1. Home là màn hình thuần túy (không tự chuyển cảnh bên trong) -> Cần bọc chuyenManHinh
        homeBtn.setOnAction(e -> { 
            chuyenManHinh(this::hienThiManHinhHome); 
            huyChonPlaylist(); 
        });
        logoBtn.setOnAction(e -> { 
            chuyenManHinh(this::hienThiManHinhHome); 
            huyChonPlaylist(); 
        });

        // 2. Favorites, Top100, History: Bên trong hàm đã gọi chuyenManHinh rồi -> GỌI TRỰC TIẾP (Bỏ bọc)
        favoritesBtn.setOnAction(e -> { 
            hienThiManHinhFavorites(); // Sửa ở đây
            huyChonPlaylist(); 
        });
        
        top100Btn.setOnAction(e -> { 
            hienThiManHinhTop100();    // Sửa ở đây
            huyChonPlaylist(); 
        });
        
        historyBtn.setOnAction(e -> { 
            hienThiManHinhHistory();   // Sửa ở đây
            huyChonPlaylist(); 
        });
        
        // Back/Forward (Giữ nguyên)
        backBtn.setOnAction(e -> handleBackNav());
        forwardBtn.setOnAction(e -> handleForwardNav());

        // Sidebar Right (Giữ nguyên)
        nextTabBtn.setOnAction(e -> switchSidebarTab(true));
        relatedTabBtn.setOnAction(e -> switchSidebarTab(false));
        
        // Playlist Action (Giữ nguyên)
        if (newPlaylistBtn != null) newPlaylistBtn.setOnAction(e -> showCreatePlaylistDialog());
        if (searchField != null) {
            // Bắt sự kiện thay đổi text (Gõ chữ nào tìm chữ đấy)
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                xuLyTimKiem(newValue);
            });
        }
    }

    private void huyChonPlaylist() {
        if (playlistListView != null) {
            playlistListView.getSelectionModel().clearSelection();
        }
    }

    // --- MÀN HÌNH PLAYLIST CHI TIẾT (V3 PRO MAX: SORT + NULLPOINTER FIX) ---
    private void hienThiChiTietPlaylist(Playlist p) {
        if (p == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PlaylistView.fxml"));
            HBox viewRoot = loader.load(); 
            
            // Dùng getNamespace() để lấy ID chuẩn xác
            java.util.Map<String, Object> namespace = loader.getNamespace();
            
            Label titleLbl = (Label) namespace.get("detailPlaylistTitle");
            Label creatorLbl = (Label) namespace.get("detailPlaylistCreator");
            Label descLbl = (Label) namespace.get("detailPlaylistDesc");
            ImageView coverImg = (ImageView) namespace.get("detailPlaylistImg");
            
            Button playAllBtn = (Button) namespace.get("detailPlayAllBtn");
            Button shuffleBtn = (Button) namespace.get("detailShuffleBtn"); // <--- NÚT ÔNG CẦN ĐÂY
            Button sortBtn = (Button) namespace.get("sortBtn"); 
            
            VBox songContainer = (VBox) namespace.get("detailSongContainer");

            // 1. GÁN DỮ LIỆU CƠ BẢN
            if (titleLbl != null) titleLbl.setText(p.getTitle());
            if (creatorLbl != null) creatorLbl.setText(p.getCreator() + " • " + p.getSize() + " bài hát");
            
            // Xử lý mô tả
            if (descLbl != null) {
                String desc = p.getDescription();
                if (desc != null && !desc.trim().isEmpty()) {
                    descLbl.setText(desc);
                    descLbl.setVisible(true);
                    descLbl.setManaged(true);
                } else {
                    descLbl.setVisible(false);
                    descLbl.setManaged(false);
                }
            }
            
            // Xử lý ảnh bìa
            if (coverImg != null) {
                String imgPath = "icons/logo.png";
                String title = p.getTitle().toLowerCase();
                if (title.contains("yêu thích") || title.contains("favorite")) imgPath = "icons/heart.png";
                else if (title.contains("gần đây") || title.contains("history")) imgPath = "icons/history.png";
                else if (title.contains("top 100") || title.contains("bxh")) imgPath = "icons/trending.png";
                try { coverImg.setImage(new Image(getClass().getResourceAsStream(imgPath))); } catch (Exception e) {}
            }

            // --- 2. XỬ LÝ NÚT "PHÁT TẤT CẢ" ---
            if (playAllBtn != null) {
                playAllBtn.setOnAction(e -> {
                    if (!p.getSongs().isEmpty()) {
                        // Phát theo thứ tự bình thường
                        player.setPlaylist(p);
                        player.play();
                        capNhatGiaoDienDuoiCung();
                        chuyenManHinh(this::hienThiManHinhPlayer);
                    }
                });
            }

            // --- 3. [MỚI] XỬ LÝ NÚT "TRỘN BÀI" (SHUFFLE) Ở HEADER ---
            if (shuffleBtn != null) {
                shuffleBtn.setOnAction(e -> {
                    if (!p.getSongs().isEmpty()) {
                        // 1. Tạo playlist tạm (để không làm hỏng thứ tự playlist gốc)
                        Playlist shuffledPlaylist = new Playlist(p.getTitle() + " (Shuffle)");
                        
                        // 2. Copy bài hát sang list mới
                        List<Song> tempList = new ArrayList<>(p.getSongs());
                        
                        // 3. Xáo trộn
                        Collections.shuffle(tempList);
                        
                        // 4. Add vào playlist tạm
                        for (Song s : tempList) shuffledPlaylist.addSong(s);
                        
                        // 5. Phát playlist tạm này
                        player.setPlaylist(shuffledPlaylist);
                        player.play();
                        
                        // 6. Cập nhật UI
                        capNhatGiaoDienDuoiCung();
                        
                        // Tùy chọn: Bật luôn nút shuffle ở player dưới đáy cho đồng bộ
                        if (!isShuffle) toggleShuffle(); 
                        
                        chuyenManHinh(this::hienThiManHinhPlayer);
                        System.out.println("🔀 Đã phát trộn bài từ playlist: " + p.getTitle());
                    }
                });
            }

            // 4. XỬ LÝ NÚT SẮP XẾP (SORT) - Bên phải
            if (sortBtn != null) {
                if (p.getTitle().toLowerCase().contains("top 100")) {
                    sortBtn.setVisible(false);
                    sortBtn.setManaged(false);
                } else {
                    sortBtn.setVisible(true);
                    sortBtn.setManaged(true);
                    ContextMenu sortMenu = new ContextMenu();
                    sortMenu.setStyle("-fx-background-color: #282828; -fx-text-fill: white;");
                    MenuItem sortAZ = new MenuItem("Tên bài hát (A-Z)");
                    MenuItem sortArtist = new MenuItem("Nghệ sĩ (A-Z)");
                    MenuItem sortTime = new MenuItem("Thời lượng (Ngắn -> Dài)");

                    sortAZ.setOnAction(e -> {
                        p.getSongs().sort(Comparator.comparing(Song::getTitle));
                        refreshSongList(songContainer, p);
                    });
                    sortArtist.setOnAction(e -> {
                        p.getSongs().sort(Comparator.comparing(Song::getArtist));
                        refreshSongList(songContainer, p);
                    });
                    sortTime.setOnAction(e -> {
                        p.getSongs().sort(Comparator.comparingDouble(Song::getDuration));
                        refreshSongList(songContainer, p);
                    });
                    sortMenu.getItems().addAll(sortAZ, sortArtist, sortTime);
                    sortBtn.setOnAction(e -> sortMenu.show(sortBtn, Side.BOTTOM, 0, 0));
                }
            }

            // 5. LOAD DANH SÁCH BÀI HÁT
            refreshSongList(songContainer, p);

            // 6. HIỂN THỊ
            chuyenManHinh(() -> {
                mainRoot.setCenter(viewRoot);
                mainRoot.setRight(null);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper: Vẽ lại danh sách bài hát (Dùng cho cả lúc mới load và lúc Sort)
    private void refreshSongList(VBox container, Playlist p) {
        if (container == null || p == null) return;
        container.getChildren().clear();
        List<Song> songs = p.getSongs();

        if (songs.isEmpty()) {
            Label emptyMsg = new Label("Danh sách trống!");
            emptyMsg.setStyle("-fx-text-fill: #808080; -fx-padding: 20;");
            container.getChildren().add(emptyMsg);
        } else {
            for (int i = 0; i < songs.size(); i++) {
                Song s = songs.get(i);
                Node row = taoDongBaiHat(s);
                if (row != null) {
                    row.setStyle("-fx-background-color: transparent; -fx-padding: 5 10; -fx-border-color: transparent transparent #1a1a1a transparent;");
                    container.getChildren().add(row);
                }
            }
        }
    }

    // --- CÁC MÀN HÌNH "ẢO" (Top 100, Fav, History) ---
    private void hienThiManHinhFavorites() {
        Playlist favPlaylist = new Playlist("Bài hát yêu thích");
        favPlaylist.setCreator("Tuyển tập của bạn");
        favPlaylist.setDescription("Những bài hát bạn đã thả tim ❤️");
        if (favoriteSongs != null) {
            for (Song s : favoriteSongs) favPlaylist.addSong(s);
        }
        hienThiChiTietPlaylist(favPlaylist);
    }

    private void hienThiManHinhHistory() {
        List<Song> historyList = historyManager.getPlayedSongs();
        Playlist historyPlaylist = new Playlist("Nghe gần đây");
        historyPlaylist.setCreator("Lịch sử phát");
        if (historyList != null) {
            int limit = Math.min(historyList.size(), 50);
            for (int i = 0; i < limit; i++) historyPlaylist.addSong(historyList.get(i));
        }
        hienThiChiTietPlaylist(historyPlaylist);
    }

    private void hienThiManHinhTop100() {
        List<Song> allSongs = new ArrayList<>(library.getAllSongs());
        allSongs.sort((s1, s2) -> Integer.compare(s2.getPlayCount(), s1.getPlayCount())); // Giảm dần
        Playlist topPlaylist = new Playlist("Top 100 - BXH");
        topPlaylist.setCreator("MUSEEK Charts");
        topPlaylist.setDescription("Danh sách 100 bài hát được nghe nhiều nhất 🏆");
        int limit = Math.min(allSongs.size(), 100);
        for (int i = 0; i < limit; i++) topPlaylist.addSong(allSongs.get(i));
        hienThiChiTietPlaylist(topPlaylist);
    }

    // --- SETUP SIDEBAR PLAYLIST LIST ---
    private void setupPlaylistListView() {
        playlistListView.getStyleClass().add("playlist-list");
        playlistListView.setCellFactory(param -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null); setStyle("-fx-background-color: transparent;");
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("PlaylistRow.fxml"));
                        VBox root = loader.load();
                        Label nameLbl = (Label) root.lookup("#playlistName");
                        Label creatorLbl = (Label) root.lookup("#playlistCreator");
                        nameLbl.setText(item.getTitle());
                        creatorLbl.setText(item.getCreator());
                        setGraphic(root); setText(null);
                    } catch (IOException e) { setText(item.getTitle()); }
                }
            }
        });

        playlistListView.setOnMouseClicked(event -> {
            Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null) {
                hienThiChiTietPlaylist(selectedPlaylist); 
            }
        });
    }

    private void showCreatePlaylistDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreatePlaylistDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainRoot.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(page));

            CreatePlaylistController controller = loader.getController();
            controller.setDialogStage(dialogStage, (newPlaylist) -> {
                newPlaylist.setCreator("pvq"); // Set cứng tên ông hoặc lấy từ UserManager
                if (playlistListView != null) playlistListView.getItems().add(newPlaylist);
            });
            dialogStage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- CÁC HÀM HỖ TRỢ KHÁC (PLAYER, TIMER, UPLOAD...) ---
    // (Giữ nguyên logic cũ nhưng sắp xếp lại cho gọn)

    private void setupTimerSystem() {
        // Khởi tạo timer rỗng ban đầu
        sleepTimer = new Timer(); 
        
        // Chỉ xử lý việc bấm nút mở Dialog
        timerBtn.setOnAction(e -> showTimerDialog());
    }

    private void showTimerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/TimerDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL); // KHÔNG dùng blocking modal để Main còn chạy update được? 
            // À KHÔNG, showAndWait() sẽ chặn luồng, nhưng Timeline chạy thread khác nên vẫn update được UI.
            dialogStage.initOwner(timerBtn.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(root));
            
            // Lấy controller
            currentTimerDialog = loader.getController();
            
            // Truyền trạng thái hiện tại (Đang chạy hay không, còn bao nhiêu giây)
            boolean isRunning = sleepTimer.isActive();
            int remaining = sleepTimer.getTimeRemaining();
            
            currentTimerDialog.setDialogStage(dialogStage, isRunning, remaining, (val) -> {
                if (val == -1) { 
                    stopCountdownUI(); 
                    sleepTimer.cancelTimer(); 
                } else if (val > 0) { 
                    startCountdownUI(val); 
                }
                currentTimerDialog = null; // Reset khi đóng
            });
            
            dialogStage.showAndWait();
            currentTimerDialog = null; // Đảm bảo null khi tắt

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3. Sửa hàm startCountdownUI để update cho Dialog nếu đang mở
    // Tham số totalSeconds là GIÂY
    private void startCountdownUI(int totalSeconds) {
        // 1. Hủy Timer cũ (nếu đang chạy)
        if (sleepTimer != null) {
            // Chỉ cần cancel, không cần remove listener vì listener mới bên dưới được xử lý an toàn rồi
            sleepTimer.cancelTimer(); 
        }

        // 2. TẠO MỚI HOÀN TOÀN (Instance mới)
        sleepTimer = new Timer();
        sleepTimer.setTimer(totalSeconds);

        // 3. Gán sự kiện cho Timer mới này
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
                // [QUAN TRỌNG] Để RỖNG ở đây.
                // Khi ta bấm cập nhật, timer cũ bị cancel -> Nó chạy vào đây -> Không làm gì cả -> UI không bị tắt.
            }
        });

        // 4. Setup Timeline cập nhật giao diện (Đồng hồ đếm ngược)
        if (uiUpdateTimeline != null) uiUpdateTimeline.stop();
        
        uiUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            int remaining = sleepTimer.getTimeRemaining();
            
            // Nếu hết giờ -> Tắt UI
            if (remaining <= 0) {
                stopCountdownUI();
                return;
            }
            
            // Format thời gian hiển thị
            String timeText;
            if (remaining >= 3600) {
                 timeText = String.format("%02d:%02d:%02d", remaining/3600, (remaining%3600)/60, remaining%60);
            } else {
                 timeText = String.format("%02d:%02d", remaining/60, remaining%60);
            }
            
            // Cập nhật nút bé ở góc (Căn giữa không bị lệch)
            timerBtn.setText(timeText);
            timerBtn.setAlignment(javafx.geometry.Pos.CENTER); 
            timerBtn.setContentDisplay(ContentDisplay.RIGHT); 
            timerBtn.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold; -fx-background-color: transparent; -fx-alignment: center;");
            
            // Cập nhật ngược lại cho Dialog to (nếu đang mở)
            if (currentTimerDialog != null) {
                currentTimerDialog.updateCountdownTime(remaining);
            }
        }));
        
        uiUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        uiUpdateTimeline.play();
    }

    private void stopCountdownUI() {
        if (uiUpdateTimeline != null) uiUpdateTimeline.stop();
        
        // Nếu muốn hủy timer logic luôn
        if (sleepTimer != null) sleepTimer.cancelTimer();

        timerBtn.setText(""); 
        timerBtn.setStyle("-fx-background-color: transparent;"); 
        timerBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); // Chỉ hiện icon đồng hồ
        timerBtn.setAlignment(javafx.geometry.Pos.CENTER); // Vẫn căn giữa cho đẹp
    }

    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nhạc từ máy tính");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a"));
        
        File selectedFile = fileChooser.showOpenDialog(uploadBtn.getScene().getWindow());

        if (selectedFile != null) {
            // Hiển thị Popup nhập thông tin
            showUploadInfoDialog(selectedFile);
        }
    }

    private void showUploadInfoDialog(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/UploadDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainRoot.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED); // Không viền cho đẹp
            dialogStage.setScene(new Scene(root));

            UploadDialogController controller = loader.getController();
            
            // Lấy tên file bỏ đuôi .mp3 để làm gợi ý
            String defaultTitle = file.getName().replaceFirst("[.][^.]+$", "");

            // Xử lý khi user bấm LƯU
            controller.setDialogStage(dialogStage, defaultTitle, (title, artist) -> {
                processFileAndAddLibrary(file, title, artist);
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm xử lý backend: Copy file + Tạo Song + Add Playlist
    // Hàm xử lý backend: Copy file -> Đọc Duration -> Tạo Song -> Add Playlist
    private void processFileAndAddLibrary(File sourceFile, String title, String artist) {
        try {
            // 1. Copy file vào thư mục data/Music (Giữ nguyên logic an toàn)
            File desDir = new File("data/Music");
            if (!desDir.exists()) desDir.mkdirs();

            String originalName = sourceFile.getName();
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) extension = originalName.substring(i);
            
            // Tên file theo timestamp
            String newFileName = System.currentTimeMillis() + extension; 
            File destFile = new File(desDir, newFileName);
            
            // Thực hiện Copy
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 2. [MỚI - QUAN TRỌNG] Tạo Player tạm để "ngửi" xem file dài bao nhiêu
            Media tempMedia = new Media(destFile.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(tempMedia);
            
            // Khi Player tạm đọc xong thông tin file:
            tempPlayer.setOnReady(() -> {
                // a. Lấy thời lượng chuẩn (Giây)
                double realDuration = tempMedia.getDuration().toSeconds();
                
                // b. Tạo bài hát với thời lượng chuẩn (Thay vì số 0)
                Song newSong = new Song(title, artist, "Local Upload", realDuration, "data/Music/" + newFileName);

                // c. Thêm vào thư viện & Playlist
                library.addSong(newSong);
                addToUploadPlaylist(newSong);

                // d. Refresh giao diện
                // Lưu ý: Code này chạy bất đồng bộ nên phải refresh ở trong này mới nhận dữ liệu mới
                Platform.runLater(() -> {
                    chuanBiGiaoDienLibrary();
                    // Tự động chuyển đến màn hình Playlist Upload để user thấy thành quả
                    Playlist uploadPl = null;
                    for(Playlist p : playlistListView.getItems()) {
                        if(p.getTitle().equals("Nhạc tải lên")) uploadPl = p;
                    }
                    if (uploadPl != null) hienThiChiTietPlaylist(uploadPl);
                });
                
                System.out.println("✅ Upload thành công: " + title + " (" + realDuration + "s)");
                
                // e. Dọn dẹp player tạm để đỡ tốn RAM
                tempPlayer.dispose(); 
            });

            // Xử lý trường hợp file lỗi không đọc được
            tempPlayer.setOnError(() -> {
                 System.err.println("❌ Lỗi đọc file: " + title);
                 // Vẫn thêm vào nhưng chấp nhận duration = 0 (Fallback)
                 Song newSong = new Song(title, artist, "Local Upload", 0, "data/Music/" + newFileName);
                 library.addSong(newSong);
                 addToUploadPlaylist(newSong);
                 Platform.runLater(this::hienThiManHinhHome);
                 tempPlayer.dispose();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm tìm hoặc tạo Playlist "Nhạc tải lên"
    private void addToUploadPlaylist(Song s) {
        String uploadPlaylistName = "Nhạc tải lên"; // Tên cố định
        Playlist targetPlaylist = null;

        // 1. Tìm xem đã có playlist này trong list view chưa
        if (playlistListView != null) {
            for (Playlist p : playlistListView.getItems()) {
                if (p.getTitle().equals(uploadPlaylistName)) {
                    targetPlaylist = p;
                    break;
                }
            }
        }

        // 2. Nếu chưa có -> Tạo mới
        if (targetPlaylist == null) {
            targetPlaylist = new Playlist(uploadPlaylistName);
            targetPlaylist.setCreator("Hệ thống");
            targetPlaylist.setDescription("Các bài hát bạn đã tải lên từ máy tính 💻");
            
            // Thêm vào ListView bên trái
            if (playlistListView != null) {
                playlistListView.getItems().add(targetPlaylist);
            }
            
            // Nếu ông có quản lý danh sách playlist trong Library thì add vào đó luôn
            // library.addPlaylist(targetPlaylist); 
        }

        // 3. Thêm bài hát vào playlist
        targetPlaylist.addSong(s);
    }

    // Navigation Helper
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
            Runnable prev = backStack.pop();
            forwardStack.push(currentViewAction);
            currentViewAction = prev;
            prev.run();
            updateNavigationButtons();
        }
    }
    
    private void handleForwardNav() {
        if (!forwardStack.isEmpty()) {
            Runnable next = forwardStack.pop();
            backStack.push(currentViewAction);
            currentViewAction = next;
            next.run();
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

    private void switchSidebarTab(boolean isNextTab) {
        if (queueTabContent == null || relatedScrollPane == null) return;
        Node toShow = isNextTab ? queueTabContent : relatedScrollPane;
        Node toHide = isNextTab ? relatedScrollPane : queueTabContent;
        toHide.setVisible(false); toHide.setManaged(false);
        if (!isNextTab) loadRelatedSongs(); else updateQueueView();
        toShow.setVisible(true); toShow.setManaged(true);
    }

    // Player Helpers
    private void xuLyPlay() { if (player != null) { player.play(); daoTrangThaiNutPlay(true); xyLyHieuUngXoay(true); batDauDongBoThoiGian(); } }
    private void xuLyPause() { if (player != null) { player.pause(); daoTrangThaiNutPlay(false); xyLyHieuUngXoay(false); } }
    public void nextSong() { player.next(); capNhatGiaoDienDuoiCung(); player.play(); }
    
    private void handleSongEnd() {
        Platform.runLater(() -> {
            if (isRepeat) { player.seek(0); player.play(); capNhatGiaoDienDuoiCung(); } 
            else nextSong();
        });
    }

    private void xulyRepeat() {
        isRepeat = !isRepeat;
        repeatButton.setStyle(isRepeat ? "-fx-background-color: #4CAF50; -fx-text-fill: white;" : null);
    }

    // Hàm xử lý nút Trộn bài (Shuffle) ở thanh player bên dưới
    private void toggleShuffle() {
        isShuffle = !isShuffle; // Đảo trạng thái tắt/bật

        if (isShuffle) {
            // 1. Đổi màu nút sang Xanh (Bật)
            if (shuffleButton != null) 
                shuffleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            
            // 2. Logic tráo bài (Nếu đang nghe thì giữ nguyên bài hiện tại)
            if (player != null && player.getPlaylist() != null) {
                List<Song> currentList = player.getPlaylist().getSongs();
                Song currentSong = player.getCurrentSong();
                
                if (!currentList.isEmpty()) {
                    // Copy ra list tạm để trộn
                    List<Song> tempList = new ArrayList<>(currentList);
                    Collections.shuffle(tempList); 
                    
                    // Nếu đang hát, đưa bài hiện tại về đúng vị trí cũ để không bị ngắt nhạc
                    if (currentSong != null) {
                        tempList.remove(currentSong);
                        // Chèn lại vào vị trí đầu hoặc vị trí hiện tại
                        tempList.add(0, currentSong); 
                        // Reset player về bài đầu tiên (là bài đang hát)
                        player.stop(); // Stop nhẹ để reset index nội bộ
                        // Nạp lại list mới
                        player.getPlaylist().getSongs().clear();
                        player.getPlaylist().getSongs().addAll(tempList);
                        
                        player.play(); // Play tiếp
                    }
                }
            }
        } else {
            // 1. Đổi màu về mặc định (Tắt)
            if (shuffleButton != null) 
                shuffleButton.setStyle(null); 
            
            // 2. Sắp xếp lại A-Z (Un-shuffle)
            if (player != null && player.getPlaylist() != null) {
                player.getPlaylist().getSongs().sort(Comparator.comparing(Song::getTitle));
            }
        }
        
        // Cập nhật danh sách chờ bên phải
        updateQueueView();
    }

    private void toggleLike() {
        Song s = player.getCurrentSong();
        if (s == null) return;
        if (favoriteSongs.contains(s)) favoriteSongs.remove(s); else favoriteSongs.add(s);
        updateLikeButtonState();
    }
    
    private void updateLikeButtonState() {
        if (likeBtn == null) return;
        Song s = player.getCurrentSong();
        likeBtn.setStyle((s != null && favoriteSongs.contains(s)) ? "-fx-opacity: 1.0; -fx-effect: dropshadow(three-pass-box, rgba(29,185,84,0.8), 10, 0, 0, 0);" : "-fx-opacity: 0.5;");
    }

    private void choiBaiHatCuThe(int index) {
        chuyenManHinh(this::hienThiManHinhPlayer);
        if (player.getMediaPlayer() != null) player.stop();
        Playlist q = new Playlist("Queue");
        for (Song s : library.getAllSongs()) q.addSong(s);
        player = new AudioPlayer(q);
        player.setOnSongEnd(this::handleSongEnd);
        for(int i=0; i<index; i++) player.next();
        xuLyPlay();
        capNhatGiaoDienDuoiCung();
        setupSliderEvents();
        updateQueueView();
    }

    private void choiBaiHatMoi(Song s) {
        Playlist p = new Playlist("Temp"); p.addSong(s);
        player.setPlaylist(p); player.play();
        capNhatGiaoDienDuoiCung();
        chuyenManHinh(this::hienThiManHinhPlayer);
        updateQueueView();
    }
    
    private void setupSliderEvents() {
        MediaPlayer mp = player.getMediaPlayer();
        if (mp != null) {
            mp.setOnReady(() -> progressSlider.setMax(mp.getTotalDuration().toSeconds()));
            mp.currentTimeProperty().addListener((o, old, val) -> {
                if (!dangKeoThanhTruot) {
                    progressSlider.setValue(val.toSeconds());
                    toMauThanhTruot(val.toSeconds(), mp.getTotalDuration().toSeconds());
                }
            });
        }
    }

    private void daoTrangThaiNutPlay(boolean playing) {
        playButton.setVisible(!playing);
        pauseButton.setVisible(playing);
    }

    private void capNhatGiaoDienDuoiCung() {
        Song s = player.getCurrentSong();
        if (s != null) {
            currentSongLabel.setText(s.getTitle());
            currentArtistLabel.setText(s.getArtist());
            totalTimeLbl.setText(doiGiaySangPhut(s.getDuration()));
            progressSlider.setMax(s.getDuration());
            capNhatAnhDiaNhac(s);
            historyManager.addSong(s);
        }
        daoTrangThaiNutPlay(player.isPlaying());
        xyLyHieuUngXoay(player.isPlaying());
        updateLikeButtonState();
        if (nextTabBtn.isSelected()) updateQueueView();
        batDauDongBoThoiGian();
    }
    
    private void capNhatAnhDiaNhac(Song s) {
        if (outerDiscCircle == null) return;
        try { outerDiscCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("icons/logo.png")))); } 
        catch (Exception e) { outerDiscCircle.setStyle("-fx-fill: #e2e6e9;"); }
    }

    private void batDauDongBoThoiGian() {
        MediaPlayer mp = player.getMediaPlayer();
        
        if (mp != null) {
            // 1. Xóa các listener cũ (nếu cần, nhưng tạo mới MediaPlayer thì nó tự mất)
            
            // 2. [QUAN TRỌNG] Tự động cập nhật Max thanh trượt khi file load xong
            mp.totalDurationProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.toSeconds() > 0) {
                    // Cập nhật giới hạn thanh trượt
                    progressSlider.setMax(newVal.toSeconds());
                    totalTimeLbl.setText(doiGiaySangPhut(newVal.toSeconds()));

                    // Fix lỗi 00:00 cho bài hát tải lên (Lưu ngược vào data)
                    Song currentSong = player.getCurrentSong();
                    if (currentSong != null && currentSong.getDuration() == 0) {
                        currentSong.setDuration(newVal.toSeconds());
                    }
                }
            });

            // 3. Logic chạy thanh trượt theo thời gian thực (Giữ nguyên)
            mp.currentTimeProperty().addListener((obs, old, val) -> {
                if (!dangKeoThanhTruot) {
                    progressSlider.setValue(val.toSeconds());
                    currentTimeLbl.setText(doiGiaySangPhut(val.toSeconds()));
                    
                    // Hiệu ứng tô màu thanh trượt
                    toMauThanhTruot(val.toSeconds(), progressSlider.getMax());
                }
            });
        }
    }

    private Node taoDongBaiHat(Song s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SongRow.fxml"));
            Node row = loader.load();
            Label lbTitle = (Label) row.lookup("#rowTitle");
            Label lbArtist = (Label) row.lookup("#rowArtist");
            Label lbDuration = (Label) row.lookup("#rowDuration");
            if (lbTitle != null) lbTitle.setText(s.getTitle());
            if (lbArtist != null) lbArtist.setText(s.getArtist());
            if (lbDuration != null) lbDuration.setText(doiGiaySangPhut(s.getDuration()));
            row.setOnMouseClicked(e -> choiBaiHatMoi(s));
            return row;
        } catch (IOException e) { return null; }
    }

    private void updateQueueView() {
        if (queueContainerVBox == null || player == null) return;
        queueContainerVBox.getChildren().clear();
        Playlist pl = player.getPlaylist();
        Song cur = player.getCurrentSong();
        if (pl != null && cur != null) {
             List<Song> all = pl.getSongs();
             int idx = all.indexOf(cur);
             if (idx >= 0 && idx < all.size()-1) {
                 for (Song s : all.subList(idx+1, all.size())) {
                     Node row = taoDongBaiHat(s);
                     if (row != null) queueContainerVBox.getChildren().add(row);
                 }
             }
        }
    }

    private void loadRelatedSongs() {
        if (relatedContainerVBox == null) return;
        relatedContainerVBox.getChildren().clear();
        List<Song> rnd = new ArrayList<>(library.getAllSongs());
        Collections.shuffle(rnd);
        int c = 0;
        for (Song s : rnd) {
            if (player.getCurrentSong() != null && s.equals(player.getCurrentSong())) continue;
            Node row = taoDongBaiHat(s);
            if (row != null) { relatedContainerVBox.getChildren().add(row); c++; }
            if (c >= 20) break;
        }
    }

    // Utilities
    private void toMauThanhTruot(double cur, double total) {
        if (total > 0) {
            double p = (cur / total) * 100;
            Node track = progressSlider.lookup(".track");
            if (track != null) track.setStyle(String.format("-fx-background-color: linear-gradient(to right, #ffffff %.2f%%, #404040 %.2f%%);", p, p));
        }
    }
    
    private void setupDiscAnimation() {
        discRotation = new RotateTransition(Duration.seconds(20), outerDiscCircle);
        discRotation.setByAngle(360); discRotation.setCycleCount(RotateTransition.INDEFINITE); discRotation.setInterpolator(Interpolator.LINEAR);
    }
    
    private void xyLyHieuUngXoay(boolean run) {
        if (discRotation == null) return;
        if (run && discRotation.getStatus() != javafx.animation.Animation.Status.RUNNING) discRotation.play();
        else if (!run) discRotation.pause();
    }
    
    private String doiGiaySangPhut(double sec) { return String.format("%02d:%02d", (int)sec/60, (int)sec%60); }
    @FXML public void handleVolumeUp(javafx.scene.input.SwipeEvent e) { volumeSlider.setValue(Math.min(100, volumeSlider.getValue()+10)); }
    @FXML public void handleVolumeDown(javafx.scene.input.SwipeEvent e) { volumeSlider.setValue(Math.max(0, volumeSlider.getValue()-10)); }

    private void xuLyTimKiem(String tuKhoa) {
        // 1. Nếu từ khóa rỗng -> Trả về màn hình Home
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            hienThiManHinhHome();
            return;
        }

        String keyword = tuKhoa.toLowerCase().trim();
        List<Song> ketQua = new ArrayList<>();

        // 2. Lọc bài hát từ thư viện
        for (Song s : library.getAllSongs()) {
            // So sánh Tên bài hát HOẶC Tên nghệ sĩ (chứa từ khóa)
            boolean matchTitle = s.getTitle().toLowerCase().contains(keyword);
            boolean matchArtist = s.getArtist().toLowerCase().contains(keyword);
            
            if (matchTitle || matchArtist) {
                ketQua.add(s);
            }
        }

        // 3. Hiển thị kết quả
        // Mẹo: Dùng lại giao diện PlaylistView để hiển thị danh sách tìm được
        Playlist playlistKetQua = new Playlist("Kết quả tìm kiếm: \"" + tuKhoa + "\"");
        playlistKetQua.setCreator("Tìm thấy " + ketQua.size() + " bài hát");
        playlistKetQua.setDescription("Kết quả phù hợp với từ khóa bạn nhập.");
        
        // Add bài hát tìm được vào playlist ảo này
        for (Song s : ketQua) {
            playlistKetQua.addSong(s);
        }

        // Gọi hàm hiển thị playlist chi tiết (tận dụng code cũ, đỡ phải viết màn hình mới)
        hienThiChiTietPlaylist(playlistKetQua);
        
        // (Tùy chọn) Đổi ảnh bìa cho đẹp
        // Vì Playlist ảo không có ảnh, ông có thể set cứng ảnh kính lúp nếu muốn
    }
}