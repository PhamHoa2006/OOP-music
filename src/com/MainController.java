package com;

import com.users.SongLibrary;
import com.users.PlaylistLibrary;
import com.musicPlayer.AudioPlayer;
import com.musicPlayer.Playlist;
import com.musicPlayer.Song;
import com.extra.Timer;
import com.extra.TimerListener;
import com.extra.TimerDialogController;
import com.users.History;
import com.musicPlayer.CreatePlaylistController;
import com.musicPlayer.UploadDialogController;
import com.users.User;
import com.users.UserManager; // Import thêm cái này để gọi saveToJSON cho gọn
import com.users.AuthDialogController;

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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.FileChooser;
import javafx.scene.media.Media;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MainController {

    // --- KHAI BÁO FXML ---
    @FXML private BorderPane mainRoot;
    @FXML private VBox rightSidebar; 

    // Player Controls
    @FXML private Button playButton, pauseButton, nextButton, prevButton;
    @FXML private Button shuffleButton, repeatButton, likeBtn;
    @FXML private Slider progressSlider, volumeSlider;
    @FXML private Label currentTimeLbl, totalTimeLbl;
    @FXML private Label currentSongLabel, currentArtistLabel;
    @FXML private ImageView miniThumbView;
    @FXML private Button settingsBtn;
    @FXML private Button addToPlaylistBtn;
    @FXML private Button queueAddBtn;
    @FXML private Button queueRepeatBtn;
    @FXML private Button queueShuffleBtn;
    
    // Disc View
    @FXML private StackPane discContainer; 
    @FXML private Circle outerDiscCircle, innerDiscCircle;
    @FXML private ImageView discIconView;

    // Navigation
    @FXML private Button homeBtn, favoritesBtn, historyBtn, top100Btn;
    @FXML private Button volumeBtn;
    @FXML private Button mutedBtn;
    @FXML private Button logoBtn, backBtn, forwardBtn;
    @FXML private TextField searchField;

    // Right Sidebar
    @FXML private ToggleButton nextTabBtn, relatedTabBtn;
    @FXML private ToggleGroup tabGroup;
    @FXML private ScrollPane queueScrollPane, relatedScrollPane;
    @FXML private VBox queueContainerVBox, relatedContainerVBox, queueTabContent;
    @FXML private ListView<Playlist> playlistListView; 

    // Top Bar Actions
    @FXML private Button timerBtn;
    @FXML private Button uploadBtn;
    @FXML private Button newPlaylistBtn;
    @FXML private ComboBox<String> privacyBox;

    // --- LOGIC VARIABLES ---
    private User currentUser = null;
    private AudioPlayer player;
    private SongLibrary library;
    private PlaylistLibrary playlistLibrary;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private boolean dangKeoThanhTruot = false;
    private boolean isSearchingPlaylist = false; // Biến trạng thái để biết searchField đang lọc cái gì
    private FlowPane currentPlaylistContainer;
    private double lastVolume = 50.0; 
    private boolean isQueueLoop = false; // Biến riêng cho nút Repeat trong Queue
    
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

    public void initialize() {
        System.out.println("🚀 [DEBUG] MainController đang khởi động...");

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
        if (settingsBtn != null) settingsBtn.setOnAction(e -> showSettingsMenu());
        
        // 4. Mặc định vào màn hình Home
        libraryView = null; 
        hienThiManHinhHome();
        
        currentViewAction = this::hienThiManHinhHome;
        updateNavigationButtons();
        capNhatIconVolume(volumeSlider.getValue());
    }

    private void caiDatBackend() {
        library = SongLibrary.getInstance();
        playlistLibrary = PlaylistLibrary.getInstance(); 
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

    // =================================================================================================
    // PHẦN 1: GIAO DIỆN CHÍNH (HOME, LIBRARY, PLAYLIST VIEW)
    // =================================================================================================

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
            try { anhBia.setImage(new Image(getClass().getResourceAsStream("icons/logo.png"))); } catch (Exception e) {}

            nutPlayTrenThe.setOnAction(e -> choiBaiHatCuThe(viTriIndex));
            theGoc.setOnMouseClicked(e -> choiBaiHatCuThe(viTriIndex));
            return theGoc;
        } catch (IOException e) { return null; }
    }

    private void hienThiChiTietPlaylist(Playlist p) {
        if (p == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PlaylistView.fxml"));
            HBox viewRoot = loader.load(); 
            java.util.Map<String, Object> namespace = loader.getNamespace();
            
            Label titleLbl = (Label) namespace.get("detailPlaylistTitle");
            Label creatorLbl = (Label) namespace.get("detailPlaylistCreator");
            Label descLbl = (Label) namespace.get("detailPlaylistDesc");
            ImageView coverImg = (ImageView) namespace.get("detailPlaylistImg");
            Button playAllBtn = (Button) namespace.get("detailPlayAllBtn");
            Button shuffleBtn = (Button) namespace.get("detailShuffleBtn"); 
            Button sortBtn = (Button) namespace.get("sortBtn"); 
            VBox songContainer = (VBox) namespace.get("detailSongContainer");

            // Fill Data
            if (titleLbl != null) titleLbl.setText(p.getTitle());
            if (creatorLbl != null) creatorLbl.setText(p.getCreator() + " • " + p.getSize() + " bài hát");
            
            if (descLbl != null) {
                descLbl.setText(p.getDescription() != null ? p.getDescription() : "");
                descLbl.setVisible(p.getDescription() != null && !p.getDescription().isEmpty());
                descLbl.setManaged(descLbl.isVisible());
            }
            
            if (coverImg != null) {
                String imgPath = "icons/logo.png";
                String title = p.getTitle().toLowerCase();
                if (title.contains("yêu thích") || title.contains("favorite")) imgPath = "icons/heart.png";
                else if (title.contains("gần đây") || title.contains("history")) imgPath = "icons/history.png";
                else if (title.contains("top 100") || title.contains("bxh")) imgPath = "icons/trending.png";
                try { coverImg.setImage(new Image(getClass().getResourceAsStream(imgPath))); } catch (Exception e) {}
            }

            // Logic Play All
            if (playAllBtn != null) {
                playAllBtn.setOnAction(e -> {
                    if (!p.getSongs().isEmpty()) {
                        player.setPlaylist(p);
                        player.play();
                        capNhatGiaoDienDuoiCung();
                        chuyenManHinh(this::hienThiManHinhPlayer);
                    }
                });
            }

            // Logic Shuffle (Trộn bài)
            if (shuffleBtn != null) {
                shuffleBtn.setOnAction(e -> {
                    if (!p.getSongs().isEmpty()) {
                        Playlist shuffledPlaylist = new Playlist(p.getTitle() + " (Shuffle)");
                        List<Song> tempList = new ArrayList<>(p.getSongs());
                        Collections.shuffle(tempList);
                        for (Song s : tempList) shuffledPlaylist.addSong(s);
                        
                        player.setPlaylist(shuffledPlaylist);
                        player.play();
                        capNhatGiaoDienDuoiCung();
                        if (!isShuffle) toggleShuffle(); 
                        chuyenManHinh(this::hienThiManHinhPlayer);
                    }
                });
            }

            // Logic Sort
            if (sortBtn != null) {
                if (p.getTitle().toLowerCase().contains("top 100")) {
                    sortBtn.setVisible(false);
                    sortBtn.setManaged(false);
                } else {
                    ContextMenu sortMenu = new ContextMenu();
                    sortMenu.setStyle("-fx-background-color: #282828; -fx-text-fill: white;");
                    MenuItem sortAZ = new MenuItem("Tên bài hát (A-Z)");
                    MenuItem sortArtist = new MenuItem("Nghệ sĩ (A-Z)");
                    MenuItem sortTime = new MenuItem("Thời lượng (Ngắn -> Dài)");

                    sortAZ.setOnAction(e -> { p.getSongs().sort(Comparator.comparing(Song::getTitle)); refreshSongList(songContainer, p); });
                    sortArtist.setOnAction(e -> { p.getSongs().sort(Comparator.comparing(Song::getArtist)); refreshSongList(songContainer, p); });
                    sortTime.setOnAction(e -> { p.getSongs().sort(Comparator.comparingDouble(Song::getDuration)); refreshSongList(songContainer, p); });

                    sortMenu.getItems().addAll(sortAZ, sortArtist, sortTime);
                    sortBtn.setOnAction(e -> sortMenu.show(sortBtn, Side.BOTTOM, 0, 0));
                }
            }

            refreshSongList(songContainer, p);

            chuyenManHinh(() -> {
                mainRoot.setCenter(viewRoot);
                mainRoot.setRight(null);
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void refreshSongList(VBox container, Playlist p) {
        if (container == null || p == null) return;
        container.getChildren().clear();
        List<Song> songs = p.getSongs();
        if (songs.isEmpty()) {
            Label emptyMsg = new Label("Danh sách trống!");
            emptyMsg.setStyle("-fx-text-fill: #808080; -fx-padding: 20;");
            container.getChildren().add(emptyMsg);
        } else {
            for (Song s : songs) {
                Node row = taoDongBaiHat(s);
                if (row != null) {
                    row.setStyle("-fx-background-color: transparent; -fx-padding: 5 10; -fx-border-color: transparent transparent #1a1a1a transparent;");
                    container.getChildren().add(row);
                }
            }
        }
    }

    // =================================================================================================
    // PHẦN 2: NAVIGATION & SỰ KIỆN NÚT
    // =================================================================================================

    private void ganSuKienChoNut() {
        // Player Control
        playButton.setOnAction(e -> xuLyPlay());
        pauseButton.setOnAction(e -> xuLyPause());
        nextButton.setOnAction(e -> nextSong());
        prevButton.setOnAction(e -> { player.previous(); capNhatGiaoDienDuoiCung(); });
        repeatButton.setOnAction(e -> xulyRepeat());
        if (likeBtn != null) likeBtn.setOnAction(e -> toggleLike());
        shuffleButton.setOnAction(e -> xulyShuffle()); 
        addToPlaylistBtn.setOnAction(e -> hienThiGiaoDienChonPlaylist());

        // Slider
        volumeSlider.valueProperty().addListener((obs, cu, moi) -> {
            double val = moi.doubleValue();
            if (player != null) {
                player.setVolume(val / 100.0);
            }
            // QUAN TRỌNG: Gọi hàm cập nhật icon mỗi khi giá trị thay đổi
            capNhatIconVolume(val);
        });

        // 2. Xử lý logic nút Volume (Đang có tiếng -> Click để Mute)
        volumeBtn.setOnAction(e -> {
            lastVolume = volumeSlider.getValue(); // Lưu lại mức hiện tại
            volumeSlider.setValue(0); // Kéo về 0 (Listener ở trên sẽ tự đổi icon sang Mute)
        });

        // 3. Xử lý logic nút Muted (Đang tắt tiếng -> Click để mở lại)
        mutedBtn.setOnAction(e -> {
            if (lastVolume == 0) lastVolume = 50.0; // Phòng trường hợp lỗi
            volumeSlider.setValue(lastVolume); // Trả lại mức cũ (Listener sẽ tự đổi icon sang Volume)
        });
        
        
        progressSlider.setOnMousePressed(e -> dangKeoThanhTruot = true);
        progressSlider.setOnMouseReleased(e -> { if (player != null) player.seek((int) progressSlider.getValue()); dangKeoThanhTruot = false; });

        // Menu chính
        homeBtn.setOnAction(e -> { chuyenManHinh(this::hienThiManHinhHome); huyChonPlaylist(); });
        logoBtn.setOnAction(e -> { chuyenManHinh(this::hienThiManHinhHome); huyChonPlaylist(); });
        
        favoritesBtn.setOnAction(e -> { hienThiManHinhFavorites(); huyChonPlaylist(); });
        top100Btn.setOnAction(e -> { hienThiManHinhTop100(); huyChonPlaylist(); });
        historyBtn.setOnAction(e -> { hienThiManHinhHistory(); huyChonPlaylist(); });
        
        // Điều hướng
        backBtn.setOnAction(e -> handleBackNav());
        forwardBtn.setOnAction(e -> handleForwardNav());

        // Sidebar tabs
        nextTabBtn.setOnAction(e -> switchSidebarTab(true));
        relatedTabBtn.setOnAction(e -> switchSidebarTab(false));
        
        // Chức năng khác
        if (newPlaylistBtn != null) newPlaylistBtn.setOnAction(e -> showCreatePlaylistDialog());
        if (queueAddBtn != null)  queueAddBtn.setOnAction(e -> luuQueueVaoPlaylistMoi());
        
        if (queueRepeatBtn != null) queueRepeatBtn.setOnAction(e -> xuLyQueueRepeat());
        
        if (queueShuffleBtn != null) queueShuffleBtn.setOnAction(e -> xuLyQueueShuffle());
        
     // Cấu trúc lại SearchField để hỗ trợ cả 2 chế độ
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isSearchingPlaylist) {
                locDanhSachPlaylist(newVal); // Chế độ lọc Playlist
            } else {
                xuLyTimKiem(newVal);		// Chế độ lọc Bài hát
            }
        });
        
        

    }

    private void huyChonPlaylist() {
        if (playlistListView != null) playlistListView.getSelectionModel().clearSelection();
    }

    // --- CÁC MÀN HÌNH "ẢO" ---
    private void hienThiManHinhFavorites() {
        if (currentUser == null) return;
        hienThiChiTietPlaylist(getFavoritesPlaylist());
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

    // =================================================================================================
    // PHẦN 3: LOGIC NGƯỜI DÙNG & DỮ LIỆU (QUAN TRỌNG)
    // =================================================================================================

    // Xử lý tạo Playlist mới
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
                if (newPlaylist.getTitle().equalsIgnoreCase("Favorites")) {
                    // Nếu user cố tình đặt tên này -> Đổi tên khác
                    newPlaylist.setName("My Favorites"); 
                }
                if (currentUser != null) {
                    newPlaylist.setCreator(currentUser.getUsername());
                    playlistLibrary.addPlaylist(newPlaylist);

                    // LƯU NGAY
                    UserManager.getInstance().saveToJSON(); 
                    System.out.println("✅ Đã lưu playlist mới: " + newPlaylist.getTitle());
                } else {
                    newPlaylist.setCreator("Khách");
                }
                if (playlistListView != null) playlistListView.getItems().add(newPlaylist);
            });
            dialogStage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Xử lý Upload nhạc
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nhạc từ máy tính");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a"));
        File selectedFile = fileChooser.showOpenDialog(uploadBtn.getScene().getWindow());

        if (selectedFile != null) {
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
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(root));

            UploadDialogController controller = loader.getController();
            String defaultTitle = file.getName().replaceFirst("[.][^.]+$", "");

            controller.setDialogStage(dialogStage, defaultTitle, (title, artist) -> {
                processFileAndAddLibrary(file, title, artist);
            });
            dialogStage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void processFileAndAddLibrary(File sourceFile, String title, String artist) {
        try {
            File desDir = new File("data/Music");
            if (!desDir.exists()) desDir.mkdirs();

            String originalName = sourceFile.getName();
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) extension = originalName.substring(i);
            
            String newFileName = System.currentTimeMillis() + extension; 
            File destFile = new File(desDir, newFileName);
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Media tempMedia = new Media(destFile.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(tempMedia);
            
            tempPlayer.setOnReady(() -> {
                double realDuration = tempMedia.getDuration().toSeconds();
                Song newSong = new Song(title, artist, "Local Upload", realDuration, "data/Music/" + newFileName);
                
                library.addSong(newSong);
                addToUploadPlaylist(newSong); // GỌI HÀM NÀY ĐỂ ADD VÀO USER VÀ SAVE

                Platform.runLater(() -> {
                    chuanBiGiaoDienLibrary();
                    // Auto chuyển đến playlist upload để xem kết quả
                    if (currentUser != null) {
                         for(Playlist p : currentUser.getPlayLists()) {
                             if(p.getTitle().equals("Nhạc tải lên")) {
                                 hienThiChiTietPlaylist(p);
                                 break;
                             }
                         }
                    }
                });
                System.out.println("✅ Upload thành công: " + title);
                tempPlayer.dispose(); 
            });

            tempPlayer.setOnError(() -> {
                 System.err.println("❌ Lỗi đọc file media!");
                 tempPlayer.dispose();
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    // [FIX QUAN TRỌNG] Logic thêm vào playlist "Nhạc tải lên" của User
    private void addToUploadPlaylist(Song s) {
        String uploadPlaylistName = "Nhạc tải lên";
        
        if (currentUser != null) {
            Playlist targetPlaylist = null;
            // 1. Tìm trong playlist của User
            for (Playlist p : currentUser.getPlayLists()) {
                if (p.getTitle().equals(uploadPlaylistName)) {
                    targetPlaylist = p;
                    break;
                }
            }
            
            // 2. Nếu chưa có -> Tạo mới
            if (targetPlaylist == null) {
                targetPlaylist = new Playlist(uploadPlaylistName);
                targetPlaylist.setCreator(currentUser.getUsername());
                targetPlaylist.setDescription("Các bài hát bạn đã tải lên từ máy tính 💻");
                currentUser.getPlayLists().add(targetPlaylist);
                
                // Add vào UI ListView
                if (playlistListView != null) playlistListView.getItems().add(targetPlaylist);
            }
            
            // 3. Add bài hát vào
            targetPlaylist.addSong(s);
            
            // 4. LƯU NGAY
            UserManager.getInstance().saveToJSON();
        } else {
            // Nếu là khách (Chưa đăng nhập) -> Có thể tạo playlist tạm trong Library hoặc bỏ qua
            System.out.println("⚠️ Khách upload nhạc - sẽ không được lưu vào Playlist cá nhân.");
        }
    }

    // --- AUTH (ĐĂNG NHẬP/ĐĂNG KÝ) ---
    private void showSettingsMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-background-color: #282828; -fx-text-fill: white;");

        if (currentUser == null) {
            MenuItem loginItem = new MenuItem("Đăng nhập");
            loginItem.setStyle("-fx-text-fill: white;");
            loginItem.setOnAction(e -> showAuthDialog());
            menu.getItems().add(loginItem);
        } else {
            MenuItem infoItem = new MenuItem("Xin chào, " + currentUser.getUsername());
            infoItem.setDisable(true);
            infoItem.setStyle("-fx-opacity: 1.0; -fx-font-weight: bold; -fx-text-fill: #1DB954;");

            MenuItem logoutItem = new MenuItem("Đăng xuất");
            logoutItem.setStyle("-fx-text-fill: white;");
            logoutItem.setOnAction(e -> handleLogout());
            
            menu.getItems().addAll(infoItem, logoutItem);
        }
        menu.show(settingsBtn, Side.BOTTOM, 0, 0);
    }

    private void showAuthDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/users/AuthDialog.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainRoot.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(root));

            AuthDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, (user) -> {
                this.currentUser = user;
                onUserLoggedIn();
            });
            dialogStage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Trong MainController.java
    private void onUserLoggedIn() {
        System.out.println("User logged in: " + currentUser.getUsername());
        
        // 1. Cập nhật giao diện Playlist bên trái
        if (playlistListView != null) {
            // Xóa hết playlist cũ
            playlistListView.getItems().removeIf(p -> !p.getCreator().equals("Hệ thống"));
            
            List<Playlist> userPlaylists = currentUser.getPlayLists();
            if (userPlaylists != null) {
                for (Playlist p : userPlaylists) {
                    // [FIX QUAN TRỌNG] Chỉ hiện playlist thường, ẨN playlist Favorites và Nhạc tải lên (nếu muốn ẩn nốt)
                    if (!p.getTitle().equals("Favorites")) { 
                        playlistListView.getItems().add(p);
                    }
                }
            }
        }
        
        // 2. Load History (Giữ nguyên)
        if (currentUser.getHistory() != null) {
            this.historyManager = currentUser.getHistory();
        } else {
            History newHistory = new History();
            currentUser.setHistory(newHistory);
            this.historyManager = newHistory;
        }
    }

    private void handleLogout() {
        currentUser = null;
        System.out.println("Đã đăng xuất");
        if (playlistListView != null) {
            playlistListView.getItems().removeIf(p -> !p.getCreator().equals("Hệ thống"));
        }
        this.historyManager = new History();
        hienThiManHinhHome();
    }

    private Playlist getFavoritesPlaylist() {
        if (currentUser == null) return null;
        for (Playlist p : currentUser.getPlayLists()) {
            if (p.getTitle().equals("Favorites")) return p;
        }
        // Nếu chưa có thì tạo mới
        Playlist fav = new Playlist("Favorites");
        fav.setCreator(currentUser.getUsername());
        fav.setDescription("Bài hát đã thả tim");
        currentUser.getPlayLists().add(fav);
        // Lưu ngay khi vừa tạo mới
        com.users.UserManager.getInstance().saveToJSON();
        return fav;
    }

    // =================================================================================================
    // PHẦN 4: PLAYER LOGIC & HELPER
    // =================================================================================================

    private void xuLyPlay() { if (player != null) { player.play(); daoTrangThaiNutPlay(true); xyLyHieuUngXoay(true); batDauDongBoThoiGian(); } }
    private void xuLyPause() { if (player != null) { player.pause(); daoTrangThaiNutPlay(false); xyLyHieuUngXoay(false); } }
    
    public void nextSong() {
        if (isShuffle) {
            int totalSongs = player.getPlaylist().getSongs().size();
            int randomIndex = (int) (Math.random() * totalSongs);
            choiBaiHatCuThe(randomIndex);
        } else {
            player.next(); // Tăng index lên 1 (đúng thứ tự)
            capNhatGiaoDienDuoiCung();
            player.play();
        }
    }
    
    private void handleSongEnd() {
        Platform.runLater(() -> {
            if (isRepeat) {
                player.seek(0);
                player.play();
                capNhatGiaoDienDuoiCung();
            } 
            else if (isShuffle) {
                // Lấy danh sách đang có của player
                int totalSongs = player.getPlaylist().getSongs().size();
                int randomIndex = (int) (Math.random() * totalSongs);
                
                // Nhảy đến bài đó mà không phá hủy cấu trúc Playlist
                choiBaiHatCuThe(randomIndex); 
            } 
            else {
                nextSong(); // Lúc này nextSong() sẽ chạy theo đúng thứ tự currentIndex + 1
            }
        });
    }

    private void xulyRepeat() {
        isRepeat = !isRepeat;

        if (isRepeat) {
            repeatButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            
            // Khóa nút Shuffle
            isShuffle = false;
            shuffleButton.setStyle(null);
            shuffleButton.setDisable(true);
            
            System.out.println("🔁 Chế độ Lặp lại: BẬT (Đã khóa Shuffle)");
        } else {
            repeatButton.setStyle(null);
            shuffleButton.setDisable(false); // Mở khóa lại Shuffle
            
            System.out.println("➡️ Chế độ Lặp lại: TẮT");
        }
    }
    
    private void xulyShuffle() {
        isShuffle = !isShuffle; // Đảo trạng thái

        if (isShuffle) {
            // 1. Nếu BẬT Shuffle: Tô màu xanh
            shuffleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            
            // 2. VÔ HIỆU HÓA nút Repeat
            isRepeat = false; // Tắt trạng thái repeat trong logic
            repeatButton.setStyle(null); // Xóa màu xanh của repeat (nếu có)
            repeatButton.setDisable(true); // Làm mờ/khóa nút repeat
            
            System.out.println("🔀 Chế độ Ngẫu nhiên: BẬT (Đã khóa Repeat)");
        } else {
            // 3. Nếu TẮT Shuffle: Về mặc định
            shuffleButton.setStyle(null);
            repeatButton.setDisable(false); // Mở khóa lại nút repeat
            
            System.out.println("➡️ Chế độ Ngẫu nhiên: TẮT");
        }
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {
            if (shuffleButton != null) shuffleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            if (player != null && player.getPlaylist() != null) {
                List<Song> currentList = player.getPlaylist().getSongs();
                Song currentSong = player.getCurrentSong();
                if (!currentList.isEmpty()) {
                    List<Song> tempList = new ArrayList<>(currentList);
                    Collections.shuffle(tempList); 
                    if (currentSong != null) {
                        tempList.remove(currentSong);
                        tempList.add(0, currentSong); 
                        player.stop(); 
                        player.getPlaylist().getSongs().clear();
                        player.getPlaylist().getSongs().addAll(tempList);
                        player.play(); 
                    }
                }
            }
        } else {
            if (shuffleButton != null) shuffleButton.setStyle(null); 
            if (player != null && player.getPlaylist() != null) {
                player.getPlaylist().getSongs().sort(Comparator.comparing(Song::getTitle));
            }
        }
        updateQueueView();
    }

    // Trong MainController.java
    private void toggleLike() {
        Song s = player.getCurrentSong();
        if (s == null || currentUser == null) return;
        
        Playlist fav = getFavoritesPlaylist();
        
        boolean isLiked = false;
        Song songToRemove = null;
        
        for (Song existing : fav.getSongs()) {
            if (existing.getSongID().equals(s.getSongID())) {
                isLiked = true;
                songToRemove = existing;
                break;
            }
        }

        if (isLiked) {
            fav.removeSong(songToRemove);
            System.out.println("💔 Đã bỏ like: " + s.getTitle());
        } else {
            fav.addSong(s);
            System.out.println("❤️ Đã like: " + s.getTitle());
        }
        
        updateLikeButtonState();
        
        // [CỰC QUAN TRỌNG] Lưu dữ liệu ngay lập tức!
        com.users.UserManager.getInstance().saveToJSON();
        System.out.println("💾 [Auto-Save] Đã lưu User sau khi Like/Unlike.");
    }
    
    private void updateLikeButtonState() {
        if (likeBtn == null || player == null) return;
        Song s = player.getCurrentSong();
        boolean isLiked = false;
        if (s != null && currentUser != null) {
            Playlist fav = getFavoritesPlaylist();
            for (Song existing : fav.getSongs()) {
                if (existing.getSongID().equals(s.getSongID())) { isLiked = true; break; }
            }
        }
        if (isLiked) likeBtn.setStyle("-fx-opacity: 1.0; -fx-effect: dropshadow(three-pass-box, rgba(29,185,84,0.8), 10, 0, 0, 0);");
        else likeBtn.setStyle("-fx-opacity: 0.5;");
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
            
            // Auto add History & Save
            historyManager.addSong(s);
            if (currentUser != null) UserManager.getInstance().saveToJSON();
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
            mp.totalDurationProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.toSeconds() > 0) {
                    progressSlider.setMax(newVal.toSeconds());
                    totalTimeLbl.setText(doiGiaySangPhut(newVal.toSeconds()));
                    Song currentSong = player.getCurrentSong();
                    if (currentSong != null && currentSong.getDuration() == 0) {
                        currentSong.setDuration(newVal.toSeconds());
                    }
                }
            });
            mp.currentTimeProperty().addListener((obs, old, val) -> {
                if (!dangKeoThanhTruot) {
                    progressSlider.setValue(val.toSeconds());
                    currentTimeLbl.setText(doiGiaySangPhut(val.toSeconds()));
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
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            hienThiManHinhHome(); return;
        }
        String keyword = tuKhoa.toLowerCase().trim();
        List<Song> ketQua = new ArrayList<>();
        for (Song s : library.getAllSongs()) {
            if (s.getTitle().toLowerCase().contains(keyword) || s.getArtist().toLowerCase().contains(keyword)) {
                ketQua.add(s);
            }
        }
        Playlist playlistKetQua = new Playlist("Kết quả tìm kiếm: \"" + tuKhoa + "\"");
        playlistKetQua.setCreator("Tìm thấy " + ketQua.size() + " bài hát");
        playlistKetQua.setDescription("Kết quả phù hợp với từ khóa bạn nhập.");
        for (Song s : ketQua) playlistKetQua.addSong(s);
        hienThiChiTietPlaylist(playlistKetQua);
    }
    
    // --- TIMER SYSTEM ---
    private void setupTimerSystem() {
        sleepTimer = new Timer(); 
        timerBtn.setOnAction(e -> showTimerDialog());
    }

    private void showTimerDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/TimerDialog.fxml"));
            Parent root = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(timerBtn.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(root));
            
            currentTimerDialog = loader.getController();
            currentTimerDialog.setDialogStage(dialogStage, sleepTimer.isActive(), sleepTimer.getTimeRemaining(), (val) -> {
                if (val == -1) { stopCountdownUI(); sleepTimer.cancelTimer(); } 
                else if (val > 0) { startCountdownUI(val); }
                currentTimerDialog = null; 
            });
            dialogStage.showAndWait();
            currentTimerDialog = null;
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startCountdownUI(int totalSeconds) {
        if (sleepTimer != null) sleepTimer.cancelTimer(); 
        sleepTimer = new Timer();
        sleepTimer.setTimer(totalSeconds);
        sleepTimer.addListener(new TimerListener() {
            @Override public void onTimerFinished() {
                Platform.runLater(() -> {
                    if (player != null && player.isPlaying()) xuLyPause();
                    stopCountdownUI();
                    System.out.println("⏰ Hết giờ! Đã tắt nhạc.");
                });
            }
            @Override public void onTimerCancelled() { }
        });

        if (uiUpdateTimeline != null) uiUpdateTimeline.stop();
        uiUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            int remaining = sleepTimer.getTimeRemaining();
            if (remaining <= 0) { stopCountdownUI(); return; }
            String timeText = (remaining >= 3600) ? String.format("%02d:%02d:%02d", remaining/3600, (remaining%3600)/60, remaining%60) : String.format("%02d:%02d", remaining/60, remaining%60);
            timerBtn.setText(timeText);
            timerBtn.setAlignment(javafx.geometry.Pos.CENTER); 
            timerBtn.setContentDisplay(ContentDisplay.RIGHT); 
            timerBtn.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold; -fx-background-color: transparent; -fx-alignment: center;");
            if (currentTimerDialog != null) currentTimerDialog.updateCountdownTime(remaining);
        }));
        uiUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        uiUpdateTimeline.play();
    }

    private void stopCountdownUI() {
        if (uiUpdateTimeline != null) uiUpdateTimeline.stop();
        if (sleepTimer != null) sleepTimer.cancelTimer();
        timerBtn.setText(""); 
        timerBtn.setStyle("-fx-background-color: transparent;"); 
        timerBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        timerBtn.setAlignment(javafx.geometry.Pos.CENTER); 
    }
    
    // Setup List view bên trái
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
            if (selectedPlaylist != null) hienThiChiTietPlaylist(selectedPlaylist); 
        });
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
    	resetSearchState();
        if (libraryView == null) chuanBiGiaoDienLibrary();
        mainRoot.setCenter(libraryView);
        mainRoot.setRight(null);
    }
    
    private void hienThiManHinhPlayer() {
    	resetSearchState();
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
    
 // Hàm cập nhật icon Loa dựa trên giá trị slider
    private void capNhatIconVolume(double value) {
        if (value > 0) {
            // Có âm thanh -> Hiện nút Volume, Ẩn nút Mute
            volumeBtn.setVisible(true);
            volumeBtn.setManaged(true); // setManaged để nút chiếm vị trí trong StackPane
            
            mutedBtn.setVisible(false);
            mutedBtn.setManaged(false);
        } else {
            // Âm thanh = 0 -> Ẩn nút Volume, Hiện nút Mute
            volumeBtn.setVisible(false);
            volumeBtn.setManaged(false);
            
            mutedBtn.setVisible(true);
            mutedBtn.setManaged(true);
        }
    }
   
    @FXML
    private void xuLyNutAddSongToPlaylist() {
        if (player.getCurrentSong() == null) return;

        // 1. Chuyển trạng thái SearchField
        isSearchingPlaylist = true;
        searchField.clear();
        searchField.setPromptText("Nhập tên Playlist để lọc...");

        // 2. Tạo giao diện hiển thị danh sách Playlist ở Center
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #121212;");

        Label header = new Label("Thêm vào Playlist");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        FlowPane playlistContainer = new FlowPane(15, 15);
        playlistContainer.setId("playlistContainer"); // Rất quan trọng để hàm lọc tìm thấy

        // 3. Hiển thị toàn bộ playlist hiện có
        veDanhSachPlaylist(playlistLibrary.getAllPlaylists(), playlistContainer);

        mainLayout.getChildren().addAll(header, playlistContainer);
        
        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #121212; -fx-background-color: transparent;");

        // 4. Đưa vào Center
        mainRoot.setCenter(scroll);
    }
    
 // --- LOGIC THÊM VÀO PLAYLIST (Đã sửa lại) ---

    // Hàm 1: Được gọi khi ấn nút dấu cộng (+)
    @FXML
    private void hienThiGiaoDienChonPlaylist() {
        if (player.getCurrentSong() == null) {
            System.out.println("⚠️ Chưa có bài hát nào đang phát!");
            return;
        }

        // 1. Đặt cờ hiệu để thanh tìm kiếm biết ta đang tìm Playlist
        isSearchingPlaylist = true;
        searchField.clear();
        searchField.setPromptText("🔍 Nhập tên Playlist để tìm...");

        // 2. Tạo Layout chính
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #121212;");

        // Tiêu đề
        Label title = new Label("Thêm \"" + player.getCurrentSong().getTitle() + "\" vào...");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        // Nút quay lại (nếu muốn hủy bỏ)
        Button cancelBtn = new Button("Hủy bỏ");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-border-color: #b3b3b3; -fx-border-radius: 20;");
        cancelBtn.setOnAction(e -> {
            resetSearchState();
            hienThiManHinhPlayer(); // Quay về màn hình đĩa nhạc
        });

        // 3. Container chứa danh sách Playlist (Dạng lưới)
        currentPlaylistContainer = new FlowPane(20, 20); // Lưu vào biến toàn cục để hàm lọc sử dụng
        currentPlaylistContainer.setPadding(new Insets(10, 0, 0, 0));

        // 4. Hiển thị dữ liệu ban đầu (Tất cả playlist)
        // Lấy từ PlaylistLibrary (Data chung)
        veDanhSachPlaylist(playlistLibrary.getAllPlaylists(), currentPlaylistContainer);

        mainLayout.getChildren().addAll(title, cancelBtn, currentPlaylistContainer);

        // 5. Đưa vào ScrollPane để cuộn nếu danh sách dài
        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #121212; -fx-background-color: transparent;");
        scroll.getStyleClass().add("main-scroll-pane");

        // 6. Hiển thị ra màn hình Center
        chuyenManHinh(() -> {
            mainRoot.setCenter(scroll);
            mainRoot.setRight(null); // Ẩn sidebar phải cho tập trung
        });
    }

    // Hàm 2: Vẽ các thẻ Playlist lên màn hình
    private void veDanhSachPlaylist(List<Playlist> dsPlaylist, FlowPane container) {
        container.getChildren().clear();
        
        if (dsPlaylist == null || dsPlaylist.isEmpty()) {
            Label empty = new Label("Không tìm thấy Playlist nào.");
            empty.setStyle("-fx-text-fill: #808080; -fx-font-size: 14px;");
            container.getChildren().add(empty);
            return;
        }

        for (Playlist p : dsPlaylist) {
            // Bỏ qua playlist hệ thống nếu không muốn user add bậy (tuỳ chọn)
            if(p.getTitle().equals("Queue") || p.getTitle().equals("Temp")) continue;

            // Tạo thẻ Card
            VBox card = new VBox(10);
            card.setPrefSize(160, 160);
            card.setStyle("-fx-background-color: #282828; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0); -fx-cursor: hand; -fx-alignment: center;");
            
            // Icon
            ImageView icon = new ImageView();
            try {
                // Logic chọn icon (giống phần PlaylistView)
                String imgPath = "icons/logo.png"; // Mặc định
                if (p.getTitle().toLowerCase().contains("yêu thích")) imgPath = "icons/heart.png";
                icon.setImage(new Image(getClass().getResourceAsStream(imgPath)));
            } catch (Exception e) { }
            icon.setFitWidth(60); icon.setFitHeight(60);

            // Tên Playlist
            Label nameLbl = new Label(p.getTitle());
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-wrap-text: true; -fx-text-alignment: center;");
            nameLbl.setMaxWidth(140);

            // Số lượng bài
            Label countLbl = new Label(p.getSongs().size() + " bài hát");
            countLbl.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12px;");

            card.getChildren().addAll(icon, nameLbl, countLbl);

            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3E3E3E; -fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: center;"));
            card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #282828; -fx-background-radius: 10; -fx-alignment: center;"));

            // SỰ KIỆN CLICK: THÊM BÀI HÁT
            card.setOnMouseClicked(e -> xuLyThemBaiHatVaoPlaylist(p));

            container.getChildren().add(card);
        }
    }

    // Hàm 3: Xử lý logic thêm và lưu file
    private void xuLyThemBaiHatVaoPlaylist(Playlist targetPlaylist) {
        Song currentSong = player.getCurrentSong();
        if (currentSong == null) return;

        // Kiểm tra trùng lặp (Tùy chọn)
        for(Song s : targetPlaylist.getSongs()) {
            if(s.getSongID().equals(currentSong.getSongID())) {
                showAlert("Thông báo", "Bài hát này đã có trong Playlist rồi!");
                return;
            }
        }

        // 1. Thêm vào object Playlist
        targetPlaylist.addSong(currentSong);

        // 2. LƯU XUỐNG FILE JSON (Quan trọng)
        playlistLibrary.saveToJSON(); 
        
        // 3. Nếu User đang đăng nhập, lưu cả User Data để đồng bộ (nếu playlist thuộc về user)
        if(currentUser != null && currentUser.getPlayLists().contains(targetPlaylist)) {
            com.users.UserManager.getInstance().saveToJSON();
        }

        System.out.println("✅ Đã thêm bài " + currentSong.getTitle() + " vào " + targetPlaylist.getTitle());

        // 4. Reset trạng thái và quay về
        resetSearchState();
        hienThiManHinhPlayer();
        
        // 5. Thông báo nhỏ (Alert)
        showAlert("Thành công", "Đã thêm vào " + targetPlaylist.getTitle());
    }

    // Hàm 4: Lọc Playlist theo tên (Được gọi từ SearchListener)
    private void locDanhSachPlaylist(String keyword) {
        if (currentPlaylistContainer == null) return;
        
        String key = keyword.toLowerCase().trim();
        List<Playlist> ketQua = new ArrayList<>();
        
        for (Playlist p : playlistLibrary.getAllPlaylists()) {
            if (p.getTitle().toLowerCase().contains(key)) {
                ketQua.add(p);
            }
        }
        // Vẽ lại danh sách đã lọc
        veDanhSachPlaylist(ketQua, currentPlaylistContainer);
    }

    // Hàm tiện ích: Reset Search về mặc định
    private void resetSearchState() {
        isSearchingPlaylist = false;
        searchField.clear();
        searchField.setPromptText("Tìm kiếm bài hát, nghệ sĩ...");
        currentPlaylistContainer = null;
    }

    // Hàm tiện ích: Hiển thị thông báo
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }
    
 // --- TÍNH NĂNG: LƯU QUEUE RA PLAYLIST MỚI ---
    private void luuQueueVaoPlaylistMoi() {
        // 1. Kiểm tra điều kiện đầu vào
        if (player == null || player.getPlaylist() == null || player.getPlaylist().getSongs().isEmpty()) {
            showAlert("Thông báo", "Danh sách phát đang trống!");
            return;
        }

        // 2. Lấy danh sách thực tế đang chạy trong Player
        // (Lưu ý: Nếu đang Shuffle, danh sách này ĐÃ ĐƯỢC XÁO TRỘN đúng như ý bạn muốn)
        List<Song> currentActiveList = player.getPlaylist().getSongs();
        
        // 3. Lấy vị trí bài đang phát
        int currentIndex = player.getCurrentIndex(); // Cần thêm hàm này ở Bước 1

        // Kiểm tra an toàn chỉ số
        if (currentIndex < 0 || currentIndex >= currentActiveList.size()) {
             showAlert("Lỗi", "Không xác định được vị trí bài hát hiện tại.");
             return;
        }

        // 4. Mở Dialog tạo Playlist
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreatePlaylistDialog.fxml"));
            Parent page = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainRoot.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(page));

            CreatePlaylistController controller = loader.getController();
            
            // Nếu bạn đã thêm hàm setDialogTitle ở bước trước thì bỏ comment dòng dưới
            // controller.setDialogTitle("Lưu danh sách chờ (Queue)"); 

            controller.setDialogStage(dialogStage, (playlistMoi) -> {
                // --- LOGIC QUAN TRỌNG TẠI ĐÂY ---
                
                // Chỉ lấy từ bài hiện tại (currentIndex) đến hết danh sách
                // subList(fromIndex, toIndex) trả về một List view từ vị trí from đến to
                List<Song> remainingSongs = currentActiveList.subList(currentIndex, currentActiveList.size());

                // Copy các bài này vào Playlist mới
                for (Song s : remainingSongs) {
                    playlistMoi.addSong(s);
                }

                // --- PHẦN LƯU TRỮ (GIỮ NGUYÊN) ---
                if (currentUser != null) {
                    playlistMoi.setCreator(currentUser.getUsername());
                    
                    // Lưu vào thư viện chung
                    playlistLibrary.addPlaylist(playlistMoi);
                    
                    // Lưu vào danh sách riêng của User
                    currentUser.getPlayLists().add(playlistMoi);
                    
                    // GỌI HÀM LƯU FILE
                    UserManager.getInstance().saveToJSON(); 
                } else {
                    playlistMoi.setCreator("Khách");
                    playlistLibrary.addPlaylist(playlistMoi);
                }

                // Cập nhật giao diện Sidebar
                if (playlistListView != null) {
                    playlistListView.getItems().add(playlistMoi);
                }
                
                // Thông báo kết quả
                showAlert("Thành công", "Đã lưu " + remainingSongs.size() + " bài hát (từ bài hiện tại) vào playlist: " + playlistMoi.getTitle());
            });

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
 // --- XỬ LÝ NÚT REPEAT TRONG QUEUE (ĐỘC LẬP) ---
    private void xuLyQueueRepeat() {
        // 1. Đảo ngược trạng thái lặp danh sách
        isQueueLoop = !isQueueLoop;

        // 2. Cập nhật giao diện (Chỉ đổi màu chính nút này)
        if (isQueueLoop) {
            if (queueRepeatBtn != null) 
                queueRepeatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            System.out.println("🔁 Queue Loop: BẬT (Hết danh sách sẽ quay lại bài đầu)");
        } else {
            if (queueRepeatBtn != null) 
                queueRepeatBtn.setStyle(null); // Trả về màu mặc định (trong suốt)
            System.out.println("➡️ Queue Loop: TẮT");
        }
    }

    // 2. Hàm xử lý Xáo trộn danh sách chờ (Queue Shuffle)
    private void xuLyQueueShuffle() {
        // Kiểm tra an toàn: Phải đi qua getPlaylist()
        if (player == null || player.getPlaylist() == null) return;
        
        //Truy cập songs thông qua playlist
        Playlist currentPlaylist = player.getPlaylist();
        List<Song> currentList = currentPlaylist.getSongs();
        
        if (currentList.size() <= 1) return;

        // Lấy bài đang hát để giữ nguyên
        Song currentSong = player.getCurrentSong();
        
        // Tạo list tạm để xáo trộn
        List<Song> tempList = new ArrayList<>(currentList);
        
        // Xóa bài đang hát khỏi list tạm (để tránh bị đảo đi chỗ khác)
        tempList.remove(currentSong);
        
        // Thực hiện xáo trộn phần còn lại
        Collections.shuffle(tempList);
        
        // Chèn lại bài đang hát vào đầu danh sách (Vị trí 0)
        tempList.add(0, currentSong);
        
        // [CẬP NHẬT] Gán lại danh sách đã trộn vào Playlist
        currentPlaylist.setSongs(tempList);
        
        // [QUAN TRỌNG] Vì bài đang hát được đưa về vị trí 0, 
        // ta phải báo cho Player biết currentIndex giờ là 0.
        // (Bạn cần thêm hàm setCurrentIndex vào AudioPlayer.java như hướng dẫn ở Bước 1)
        player.setCurrentIndex(0); 
        
        // Cập nhật giao diện bên phải
        updateQueueView();
        capNhatGiaoDienDuoiCung();
        
        System.out.println("🔀 Đã xáo trộn danh sách chờ (Giữ nguyên bài đang phát)!");
    }
    
}

