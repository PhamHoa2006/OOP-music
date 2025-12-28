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
import com.users.UserManager;
import com.users.UserProfileController;
import com.users.RecommendationEngine; // [MỚI]
import java.util.stream.Collectors;    // [MỚI]

import javafx.geometry.Side;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MainController implements Initializable {

    // --- KHAI BÁO FXML ---
    @FXML
    private BorderPane mainRoot;
    @FXML
    private VBox rightSidebar;
    @FXML
    private HBox bottomPlayerBar;

    // Player Controls
    @FXML
    private Button playButton, pauseButton, nextButton, prevButton;
    @FXML
    private Button shuffleButton, repeatButton, likeBtn;
    @FXML
    private Slider progressSlider, volumeSlider;
    @FXML
    private Label currentTimeLbl, totalTimeLbl;
    @FXML
    private Label currentSongLabel, currentArtistLabel;
    @FXML
    private ImageView miniThumbView;
    @FXML
    private Button settingsBtn;
    @FXML
    private Button addToPlaylistBtn;

    // Nút trạng thái ON (Mới thêm)
    @FXML
    private Button onLikeBtn; // Tim bài hát (Bật)
    @FXML
    private Button onShuffleBtn; // Trộn bài (Bật)
    @FXML
    private Button onRepeatBtn; // Lặp lại (Bật)
    @FXML
    private Button onQueueLikeBtn; // Tim Playlist (Bật)
    @FXML
    private Button onQueueRepeatBtn; // Lặp danh sách (Bật)

    // Các nút Queue & Sidebar
    @FXML
    private Button queueAddBtn;
    @FXML
    private Button queueRepeatBtn;
    @FXML
    private Button queueShuffleBtn;
    @FXML
    private Button queueLikeBtn;
    @FXML
    private Button shareBtn;
    @FXML
    private Button queueShareBtn;

    // Disc View
    @FXML
    private StackPane discContainer;
    @FXML
    private Circle outerDiscCircle, innerDiscCircle;
    @FXML
    private ImageView discIconView;

    // Navigation
    @FXML
    private Button homeBtn, favoritesBtn, historyBtn, top100Btn;
    @FXML
    private Button volumeBtn;
    @FXML
    private Button mutedBtn;
    @FXML
    private Button logoBtn, backBtn, forwardBtn;
    @FXML
    private TextField searchField;

    // Right Sidebar
    @FXML
    private ToggleButton nextTabBtn, relatedTabBtn;
    @FXML
    private ToggleGroup tabGroup;
    @FXML
    private ScrollPane queueScrollPane, relatedScrollPane;
    @FXML
    private VBox queueContainerVBox, relatedContainerVBox, queueTabContent;
    @FXML
    private ListView<Playlist> playlistListView;

    // Top Bar Actions
    @FXML
    private Button timerBtn;
    @FXML
    private Button uploadBtn;
    @FXML
    private Button newPlaylistBtn;
    @FXML
    private ComboBox<String> privacyBox;

    // --- LOGIC VARIABLES ---
    private User currentUser = null;
    private AudioPlayer player;
    private SongLibrary library;
    private PlaylistLibrary playlistLibrary;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private boolean dangKeoThanhTruot = false;
    private boolean isSearchingPlaylist = false;
    private FlowPane currentPlaylistContainer;

    private double lastVolume = 50.0;
    private boolean isQueueLoop = false;

    private ScrollPane libraryView;
    private Node savedRightSidebar;
    private RotateTransition discRotation;

    private Stack<Runnable> backStack = new Stack<>();
    private Stack<Runnable> forwardStack = new Stack<>();
    private Runnable currentViewAction;

    private Timer sleepTimer;
    private Timeline uiUpdateTimeline;
    private History historyManager;
    private TimerDialogController currentTimerDialog;

    private static MainController instance;
    // Biến kiểm tra xem có đang ở màn hình Player không
    private boolean isPlayerMode = false;
    private javafx.scene.Node manHinhTruocKhiVaoPlayer = null;

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        System.out.println("🚀 [DEBUG] MainController đang khởi động...");

        savedRightSidebar = rightSidebar;
        caiDatBackend();
        setupDiscAnimation();

        setupTimerSystem();
        setupPlaylistListView();

        ganSuKienChoNut(); // Gán toàn bộ sự kiện nút ở đây

        if (uploadBtn != null)
            uploadBtn.setOnAction(e -> handleUpload());
        if (settingsBtn != null)
            settingsBtn.setOnAction(e -> showUserScreen());

        libraryView = null;
        hienThiManHinhHome();

        currentViewAction = this::hienThiManHinhHome;
        updateNavigationButtons();

        if (bottomPlayerBar != null) {
            bottomPlayerBar.setVisible(false);
            bottomPlayerBar.setManaged(false);
        }

        // --- SETUP VOLUME BAN ĐẦU ---
        double defaultVolume = 50.0;
        volumeSlider.setValue(defaultVolume);
        lastVolume = defaultVolume;

        if (player != null) {
            player.setVolume(defaultVolume / 100.0);
        }
        capNhatIconVolume(defaultVolume);

        Platform.runLater(() -> capNhatMauVolume());

        // --- SETUP TRẠNG THÁI MẶC ĐỊNH ---
        isShuffle = false;
        isRepeat = false;
        isQueueLoop = false;

        // Ẩn hiện các nút On/Off ban đầu
        setToggleState(likeBtn, onLikeBtn, false);
        setToggleState(shuffleButton, onShuffleBtn, false);
        setToggleState(repeatButton, onRepeatBtn, false);
        setToggleState(queueLikeBtn, onQueueLikeBtn, false);
        setToggleState(queueRepeatBtn, onQueueRepeatBtn, false);

        // Disable nút like playlist khi mới vào
        if (queueLikeBtn != null) {
            queueLikeBtn.setDisable(false); // Cho phép bấm
            queueLikeBtn.setOpacity(1.0);   // Rõ nét 100% (vì đã có ảnh)
            queueLikeBtn.setVisible(true);
        }
        // Ẩn nút ON đi
        if (onQueueLikeBtn != null) {
            onQueueLikeBtn.setVisible(false);
            onQueueLikeBtn.setManaged(false);
        }

        if (bottomPlayerBar != null) {
            bottomPlayerBar.setOnMouseClicked(event -> {
                // Trừ trường hợp bấm vào nút hoặc slider thì không tính
                if (!(event.getTarget() instanceof javafx.scene.control.Button) && 
                    !(event.getTarget() instanceof javafx.scene.control.Slider)) {
                    
                    if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                        handleBottomBarClick();
                    }
                }
            });
            // Thêm hiệu ứng tay chỉ
            bottomPlayerBar.setStyle("-fx-cursor: hand; -fx-background-color: #282828; -fx-border-color: #121212; -fx-border-width: 1 0 0 0;");
        }

        try {
            // mainRoot là cái BorderPane to nhất bao quanh app của ông
            if (mainRoot != null) {
                // Lưu ý: Đảm bảo file style.css nằm cùng thư mục với MainController
                String css = getClass().getResource("Style.css").toExternalForm();
                mainRoot.getStylesheets().add(css);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Không tìm thấy file style.css! Hãy kiểm tra lại đường dẫn.");
            e.printStackTrace();
        }
    }

    public void setLoggedInUser(User user) {
        this.currentUser = user;
        UserManager.getInstance().setCurrentUser(user);

        double currentVolumeVal = volumeSlider.getValue();

        if (player != null) {
            player.stop(); // Tắt nhạc
        }

        if (bottomPlayerBar != null) {
            bottomPlayerBar.setVisible(false);
            bottomPlayerBar.setManaged(false); // Ẩn và không chiếm chỗ layout
        }

        // Reset giao diện Player về mặc định
        if (currentSongLabel != null)
            currentSongLabel.setText("Chọn bài hát để nghe...");
        if (currentArtistLabel != null)
            currentArtistLabel.setText("");
        if (totalTimeLbl != null)
            totalTimeLbl.setText("00:00");
        if (currentTimeLbl != null)
            currentTimeLbl.setText("00:00");
        if (progressSlider != null)
            progressSlider.setValue(0);
        if (outerDiscCircle != null) {
            outerDiscCircle.setStyle(null); // Xóa style cũ đi
            outerDiscCircle.setFill(javafx.scene.paint.Color.web("#e2e6e9")); // Set màu bằng Code
        }

        // Khởi tạo lại Backend (Tạo Player mới tinh)
        caiDatBackend();

        if (player != null) {
            player.setVolume(currentVolumeVal / 100.0);
        }

        onUserLoggedIn();
        showUserScreen();
        System.out.println("✅ MainController: Đã nhận user " + user.getUsername() + " và load data.");
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

    private void chuanBiGiaoDienLibrary() {
        VBox mainContent = new VBox();
        mainContent.setSpacing(40); // Tăng khoảng cách giữa các mục cho thoáng
        mainContent.setPadding(new Insets(20, 20, 50, 20));
        mainContent.setStyle("-fx-background-color: #121212;");

        // --- CHUẨN BỊ DỮ LIỆU CHUNG ---
        List<Song> allSongs = library.getAllSongs();
        List<Playlist> allPlaylists = playlistLibrary.getAllPlaylists();

        // Lấy Top 10 Trending (Chung cho cả 2 chế độ)
        List<Song> trendingSongs = new ArrayList<>(allSongs);
        trendingSongs.sort((s1, s2) -> Integer.compare(s2.getPlayCount(), s1.getPlayCount())); // Sắp xếp view giảm dần
        List<Song> top10Trending = trendingSongs.subList(0, Math.min(trendingSongs.size(), 10));

        // =========================================================
        // TRƯỜNG HỢP 1: CHƯA ĐĂNG NHẬP (KHÁCH)
        // =========================================================
        if (currentUser == null) {
            // 1. Những bài hát thịnh hành
            if (!top10Trending.isEmpty()) {
                mainContent.getChildren().add(taoMotHangNgang("🔥 Những bài hát thịnh hành", top10Trending));
            }

            // 2. Những Playlist nổi bật (10 Playlist công khai bất kỳ)
            List<Playlist> publicPlaylists = allPlaylists.stream()
                    .filter(this::isPublicPlaylist)
                    .collect(Collectors.toList());
            Collections.shuffle(publicPlaylists); // Xáo trộn
            List<Playlist> featuredPlaylists = publicPlaylists.subList(0, Math.min(publicPlaylists.size(), 10));

            if (!featuredPlaylists.isEmpty()) {
                mainContent.getChildren().add(taoHangNgangPlaylist("🌟 Những Playlist nổi bật", featuredPlaylists));
            }
        } 
        
        // =========================================================
        // TRƯỜNG HỢP 2: ĐÃ ĐĂNG NHẬP (USER)
        // =========================================================
        else {
            RecommendationEngine recommender = new RecommendationEngine();

            // 1. Dành cho bạn (Backend Suggestion)
            List<Song> forYouSongs = recommender.getListOfSuggestedSongs(currentUser.getHistory(), 10);
            if (!forYouSongs.isEmpty()) {
                mainContent.getChildren().add(taoMotHangNgang("💖 Dành cho bạn", forYouSongs));
            }

            // 2. Những bài hát thịnh hành (Top 100)
            if (!top10Trending.isEmpty()) {
                mainContent.getChildren().add(taoMotHangNgang("🔥 Xu hướng thịnh hành", top10Trending));
            }

            // 3. Những Playlist nổi bật (Public, Ưu tiên người khác tạo, Shuffle)
            List<Playlist> otherPublicPlaylists = allPlaylists.stream()
                    .filter(this::isPublicPlaylist)
                    .filter(p -> !p.getCreator().equals(currentUser.getUsername())) // Trừ của mình ra
                    .collect(Collectors.toList());
            Collections.shuffle(otherPublicPlaylists);
            List<Playlist> featuredPlaylists = otherPublicPlaylists.subList(0, Math.min(otherPublicPlaylists.size(), 10));

            if (!featuredPlaylists.isEmpty()) {
                mainContent.getChildren().add(taoHangNgangPlaylist("🌏 Khám phá Playlist", featuredPlaylists));
            }

            // 4. Nghe lại (10 bài gần đây nhất)
            if (currentUser.getHistory() != null) {
                List<Song> historySongs = currentUser.getHistory().getPlayedSongs();
                if (!historySongs.isEmpty()) {
                    List<Song> listenAgain = historySongs.subList(0, Math.min(historySongs.size(), 10));
                    mainContent.getChildren().add(taoMotHangNgang("↺ Nghe lại", listenAgain));
                }
            }

            // 5. Bài hát yêu thích (10 bài ngẫu nhiên)
            Playlist favPlaylist = getFavoritesPlaylist();
            if (favPlaylist != null && !favPlaylist.getSongs().isEmpty()) {
                List<Song> favSongs = new ArrayList<>(favPlaylist.getSongs());
                Collections.shuffle(favSongs);
                List<Song> randomFavs = favSongs.subList(0, Math.min(favSongs.size(), 10));
                mainContent.getChildren().add(taoMotHangNgang("❤️ Bài hát yêu thích", randomFavs));
            }

            // 6. Playlist của bạn (Tự tạo + Đã thích, Shuffle lấy 10)
            List<Playlist> myPlaylists = new ArrayList<>(currentUser.getPlayLists());
            if (currentUser.getLikedPlaylists() != null) {
                myPlaylists.addAll(currentUser.getLikedPlaylists());
            }

            // [UPDATE] Lọc bỏ tất cả Playlist hệ thống
            myPlaylists = myPlaylists.stream()
                    .distinct()
                    .filter(p -> {
                        String title = p.getTitle();
                        // Danh sách đen các tên cần loại bỏ
                        return !title.equals("Temp") && 
                               !title.equals("Queue") &&
                               !title.equals("Favorites") &&
                               !title.equals("History") &&
                               !title.equals("Nhạc tải lên") &&
                               !title.equals("Bài hát yêu thích") &&
                               !title.equals("Nghe gần đây") &&
                               !title.contains("Top 100"); // Nếu không muốn hiện Top 100 đã like
                    })
                    .collect(Collectors.toList());

            if (!myPlaylists.isEmpty()) {
                Collections.shuffle(myPlaylists);
                List<Playlist> randomMyPlaylists = myPlaylists.subList(0, Math.min(myPlaylists.size(), 10));
                mainContent.getChildren().add(taoHangNgangPlaylist("🎧 Thư viện của bạn", randomMyPlaylists));
            }
        }

        // --- ĐÓNG GÓI VÀO SCROLLPANE ---
        libraryView = new ScrollPane(mainContent);
        libraryView.setFitToWidth(true);
        libraryView.setPannable(true);
        libraryView.setStyle("-fx-background: #121212; -fx-background-color: transparent;");
        libraryView.getStyleClass().add("main-scroll-pane"); // CSS thanh cuộn đẹp
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
            if (card != null)
                cardRow.getChildren().add(card);
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
            if (lbTenBai != null)
                lbTenBai.setText(baiHat.getTitle());
            if (lbCaSi != null)
                lbCaSi.setText(baiHat.getArtist());
            try {
                anhBia.setImage(new Image(getClass().getResourceAsStream("icons/logo.png")));
            } catch (Exception e) {
            }
            nutPlayTrenThe.setOnAction(e -> choiBaiHatCuThe(viTriIndex));
            theGoc.setOnMouseClicked(e -> choiBaiHatCuThe(viTriIndex));
            return theGoc;
        } catch (IOException e) {
            return null;
        }
    }

    public void hienThiChiTietPlaylist(Playlist p) {
        isPlayerMode = false;
        if (p == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PlaylistView.fxml"));
            HBox viewRoot = loader.load(); 
            java.util.Map<String, Object> namespace = loader.getNamespace();
            
            Label titleLbl = (Label) namespace.get("detailPlaylistTitle");
            Label creatorLbl = (Label) namespace.get("detailPlaylistCreator");
            Label descLbl = (Label) namespace.get("detailPlaylistDesc"); // Vị trí mấu chốt
            ImageView coverImg = (ImageView) namespace.get("detailPlaylistImg");
            Button playAllBtn = (Button) namespace.get("detailPlayAllBtn");
            Button shuffleBtn = (Button) namespace.get("detailShuffleBtn"); 
            Button sortBtn = (Button) namespace.get("sortBtn"); 
            VBox songContainer = (VBox) namespace.get("detailSongContainer");

            // 1. Set Title
            if (titleLbl != null) {
                if (p.getTitle().equalsIgnoreCase("Favorites")) titleLbl.setText("Bài hát yêu thích");
                else titleLbl.setText(p.getTitle());
            }
            
            // 2. Set Creator
            if (creatorLbl != null) creatorLbl.setText(p.getCreator() + " • " + p.getSize() + " bài hát");
            
            // 3. [MỚI] CHÈN DÒNG TRẠNG THÁI & NÚT SỬA (Ngay trên mô tả)
            // Chỉ chèn nếu tìm thấy label mô tả để làm mốc vị trí
            if (descLbl != null) {
                addStatusAndEditRow(descLbl, p);
            }

            // 4. Set Description
            if (descLbl != null) {
                descLbl.setText(p.getDescription() != null ? p.getDescription() : "");
                // Luôn hiện mô tả (kể cả trống) để giữ khoảng cách, hoặc ẩn tùy ông
                descLbl.setVisible(true); 
                descLbl.setManaged(true);
            }
            
            // 5. Set Image (Giữ nguyên code cũ)
            if (coverImg != null) {
                String imgPath = "icons/logo.png";
                String title = p.getTitle().toLowerCase();
                if (title.contains("yêu thích") || title.contains("favorite")) imgPath = "icons/heart.png";
                else if (title.contains("gần đây") || title.contains("history")) imgPath = "icons/history.png";
                else if (title.contains("top 100") || title.contains("bxh")) imgPath = "icons/trending.png";
                try { coverImg.setImage(new Image(getClass().getResourceAsStream(imgPath))); } catch (Exception e) {}
            }

            // ... (Các nút PlayAll, Shuffle, Sort giữ nguyên logic cũ) ...
            if (playAllBtn != null) {
                playAllBtn.setOnAction(e -> {
                    if (!p.getSongs().isEmpty()) {
                        player.setPlaylist(p); player.play();
                        capNhatGiaoDienDuoiCung(); chuyenManHinh(this::hienThiManHinhPlayer);
                    }
                });
            }
            if (shuffleBtn != null) {
                shuffleBtn.setOnAction(e -> {
                    if (!p.getSongs().isEmpty()) {
                        Playlist shuffledPlaylist = new Playlist(p.getTitle() + " (Shuffle)");
                        List<Song> tempList = new ArrayList<>(p.getSongs());
                        Collections.shuffle(tempList);
                        for (Song s : tempList) shuffledPlaylist.addSong(s);
                        player.setPlaylist(shuffledPlaylist); player.play();
                        capNhatGiaoDienDuoiCung();
                        if (!isShuffle) toggleShuffle(); 
                        chuyenManHinh(this::hienThiManHinhPlayer);
                    }
                });
            }
            if (sortBtn != null) {
                // ... (Giữ nguyên logic sort) ...
                if (p.getTitle().toLowerCase().contains("top 100")) {
                    sortBtn.setVisible(false); sortBtn.setManaged(false);
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
        if (container == null || p == null)
            return;
        container.getChildren().clear();
        List<Song> songs = p.getSongs();
        if (songs.isEmpty()) {
            Label emptyMsg = new Label("Danh sách trống!");
            emptyMsg.setStyle("-fx-text-fill: #808080; -fx-padding: 20; -fx-font-size: 18px;");
            container.getChildren().add(emptyMsg);
        } else {
            for (Song s : songs) {
                Node row = taoDongBaiHat(s);
                if (row != null) {
                    row.setStyle(
                            "-fx-background-color: transparent; -fx-padding: 5 10; -fx-border-color: transparent transparent #1a1a1a transparent;");
                    row.setOnMouseClicked(e -> choiPlaylistTuBaiHat(p, s));
                    container.getChildren().add(row);
                }
            }
        }
    }

    public void choiPlaylistTuBaiHat(Playlist p, Song s) {
        if (p == null || s == null)
            return;
        player.setPlaylist(p);
        if (player.getMediaPlayer() != null)
            player.stop();
        int index = p.getSongs().indexOf(s);
        if (index != -1) {
            for (int i = 0; i < index; i++) {
                player.next();
            }
        }
        xuLyPlay();
        capNhatGiaoDienDuoiCung();
        chuyenManHinh(this::hienThiManHinhPlayer);
        updateQueueView();
    }

    // =================================================================================================
    // PHẦN 2: NAVIGATION & SỰ KIỆN NÚT (Đã chuẩn hóa)
    // =================================================================================================

    private void ganSuKienChoNut() {
        // --- 1. PLAYER CONTROL ---
        playButton.setOnAction(e -> xuLyPlay());
        pauseButton.setOnAction(e -> xuLyPause());

        nextButton.setOnAction(e -> nextSong());

        prevButton.setOnAction(e -> {
            if (player.getCurrentTime() > 5.0) {
                // TRƯỜNG HỢP 1: Đang nghe dở (>5s) -> Tua lại từ đầu
                player.seek(0);
                player.play();
                
                // Cập nhật lại giao diện ngay lập tức
                daoTrangThaiNutPlay(true); // Đổi nút Play thành Pause
                xyLyHieuUngXoay(true);     // Đĩa quay
                batDauDongBoThoiGian();    // [QUAN TRỌNG] Gọi slider chạy lại
            } else {
                // TRƯỜNG HỢP 2: Mới nghe (<5s) -> Lùi về bài trước
                player.previous();
                
                // Gọi chuỗi hàm này để đảm bảo mọi thứ được reset
                xuLyPlay();                 // Play + Kích hoạt slider
                capNhatGiaoDienDuoiCung();  // Cập nhật tên bài, ảnh...
                updateQueueView();          // Cập nhật highlight danh sách
            }
        });

        // --- 2. CÁC NÚT CHỨC NĂNG (ON/OFF) ---
        // Like Bài hát
        if (likeBtn != null)
            likeBtn.setOnAction(e -> handleLike());
        if (onLikeBtn != null)
            onLikeBtn.setOnAction(e -> handleLike());

        // Shuffle (Dưới)
        if (shuffleButton != null)
            shuffleButton.setOnAction(e -> toggleShuffle());
        if (onShuffleBtn != null)
            onShuffleBtn.setOnAction(e -> toggleShuffle());

        // Repeat (Dưới)
        if (repeatButton != null)
            repeatButton.setOnAction(e -> xulyRepeat());
        if (onRepeatBtn != null)
            onRepeatBtn.setOnAction(e -> xulyRepeat());

        // Like Playlist (Trên)
        if (queueLikeBtn != null)
            queueLikeBtn.setOnAction(e -> toggleLikePlaylist());
        if (onQueueLikeBtn != null)
            onQueueLikeBtn.setOnAction(e -> toggleLikePlaylist());

        // Loop Playlist (Trên)
        if (queueRepeatBtn != null)
            queueRepeatBtn.setOnAction(e -> xuLyQueueRepeat());
        if (onQueueRepeatBtn != null)
            onQueueRepeatBtn.setOnAction(e -> xuLyQueueRepeat());

        // Queue Shuffle (Trên) - Chỉ dùng nút thường
        if (queueShuffleBtn != null)
            queueShuffleBtn.setOnAction(e -> xuLyQueueShuffle());

        // --- 3. VOLUME CONTROL ---
        volumeSlider.valueProperty().addListener((obs, cu, moi) -> {
            double val = moi.doubleValue();
            if (player != null)
                player.setVolume(val / 100.0);
            capNhatIconVolume(val);
            capNhatMauVolume();
        });

        volumeBtn.setOnAction(e -> {
            lastVolume = volumeSlider.getValue();
            volumeSlider.setValue(0);
        });

        mutedBtn.setOnAction(e -> {
            if (lastVolume <= 0)
                lastVolume = 50.0;
            volumeSlider.setValue(lastVolume);
        });

        // --- 4. PROGRESS BAR ---
        progressSlider.setOnMousePressed(e -> dangKeoThanhTruot = true);
        progressSlider.setOnMouseReleased(e -> {
            if (player != null)
                player.seek((int) progressSlider.getValue());
            dangKeoThanhTruot = false;
        });

        // --- 5. NAVIGATION ---
        homeBtn.setOnAction(e -> {
            chuyenManHinh(this::hienThiManHinhHome);
            huyChonPlaylist();
        });
        logoBtn.setOnAction(e -> {
            chuyenManHinh(this::hienThiManHinhHome);
            huyChonPlaylist();
        });
        favoritesBtn.setOnAction(e -> {
            hienThiManHinhFavorites();
            huyChonPlaylist();
        });
        top100Btn.setOnAction(e -> {
            hienThiManHinhTop100();
            huyChonPlaylist();
        });
        historyBtn.setOnAction(e -> {
            hienThiManHinhHistory();
            huyChonPlaylist();
        });

        backBtn.setOnAction(e -> handleBackNav());
        forwardBtn.setOnAction(e -> handleForwardNav());

        // --- 6. SIDEBAR & SHARE ---
        nextTabBtn.setOnAction(e -> switchSidebarTab(true));
        relatedTabBtn.setOnAction(e -> switchSidebarTab(false));

        if (shareBtn != null)
            shareBtn.setOnAction(e -> handleShare());
        if (queueShareBtn != null)
            queueShareBtn.setOnAction(e -> handleShare());

        // --- 7. OTHER ACTIONS ---
        if (newPlaylistBtn != null)
            newPlaylistBtn.setOnAction(e -> showCreatePlaylistDialog());
        if (queueAddBtn != null)
            queueAddBtn.setOnAction(e -> luuQueueVaoPlaylistMoi());
        if (addToPlaylistBtn != null)
            addToPlaylistBtn.setOnAction(e -> handleAddToPlaylist());

        // --- 8. SEARCH ---
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            isPlayerMode = false;
            if (isSearchingPlaylist)
                locDanhSachPlaylist(newVal);
            else
                xuLyTimKiem(newVal);
        });
    }

    private void huyChonPlaylist() {
        if (playlistListView != null)
            playlistListView.getSelectionModel().clearSelection();
    }

    private void resetSidebarStyles() {
        homeBtn.getStyleClass().remove("nav-btn-selected");
        favoritesBtn.getStyleClass().remove("nav-btn-selected");
        historyBtn.getStyleClass().remove("nav-btn-selected");
        top100Btn.getStyleClass().remove("nav-btn-selected");
    }

    public void hienThiManHinhFavorites() {
        isPlayerMode = false;
        if (currentUser == null) {
            showUserScreen();
            return;
        }
        hienThiChiTietPlaylist(getFavoritesPlaylist());
    }

    public void hienThiManHinhHistory() {
        if (currentUser == null) {
            showUserScreen();
            return;
        }
        List<Song> historyList = historyManager.getPlayedSongs();
        Playlist historyPlaylist = new Playlist("Nghe gần đây");
        historyPlaylist.setCreator("Lịch sử phát");
        if (historyList != null) {
            int limit = Math.min(historyList.size(), 50);
            for (int i = 0; i < limit; i++)
                historyPlaylist.addSong(historyList.get(i));
        }
        hienThiChiTietPlaylist(historyPlaylist);
    }

    private void hienThiManHinhTop100() {
        isPlayerMode = false;
        List<Song> allSongs = new ArrayList<>(library.getAllSongs());
        allSongs.sort((s1, s2) -> Integer.compare(s2.getPlayCount(), s1.getPlayCount()));
        Playlist topPlaylist = new Playlist("Top 100 - BXH");
        topPlaylist.setCreator("MUSEEK Charts");
        topPlaylist.setDescription("Danh sách 100 bài hát được nghe nhiều nhất 🏆");
        int limit = Math.min(allSongs.size(), 100);
        for (int i = 0; i < limit; i++)
            topPlaylist.addSong(allSongs.get(i));
        hienThiChiTietPlaylist(topPlaylist);
    }

    private void showCreatePlaylistDialog() {
        if (currentUser == null) {
            showUserScreen();
            return;
        }
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
                    newPlaylist.setName("My Favorites");
                }
                if (currentUser != null) {
                    newPlaylist.setCreator(currentUser.getUsername());
                    playlistLibrary.addPlaylist(newPlaylist);
                    currentUser.getPlayLists().add(newPlaylist);
                    UserManager.getInstance().saveToJSON();
                    System.out.println("✅ Đã lưu playlist mới: " + newPlaylist.getTitle());
                } else {
                    newPlaylist.setCreator("Khách");
                    playlistLibrary.addPlaylist(newPlaylist);
                }
                if (playlistListView != null)
                    refreshPlaylistSidebar();
            });
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUpload() {
        if (currentUser == null) {
            showUserScreen();
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nhạc từ máy tính");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a"));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFileAndAddLibrary(File sourceFile, String title, String artist) {
        try {
            File desDir = new File("data/Music");
            if (!desDir.exists())
                desDir.mkdirs();
            String originalName = sourceFile.getName();
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0)
                extension = originalName.substring(i);
            String newFileName = System.currentTimeMillis() + extension;
            File destFile = new File(desDir, newFileName);
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Media tempMedia = new Media(destFile.toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(tempMedia);
            tempPlayer.setOnReady(() -> {
                double realDuration = tempMedia.getDuration().toSeconds();
                Song newSong = new Song(title, artist, "Local Upload", realDuration, "data/Music/" + newFileName);
                library.addSong(newSong);
                addToUploadPlaylist(newSong);
                Platform.runLater(() -> {
                    chuanBiGiaoDienLibrary();
                    if (currentUser != null) {
                        for (Playlist p : currentUser.getPlayLists()) {
                            if (p.getTitle().equals("Nhạc tải lên")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToUploadPlaylist(Song s) {
        String uploadPlaylistName = "Nhạc tải lên";
        if (currentUser != null) {
            Playlist targetPlaylist = null;
            for (Playlist p : currentUser.getPlayLists()) {
                if (p.getTitle().equals(uploadPlaylistName)) {
                    targetPlaylist = p;
                    break;
                }
            }
            if (targetPlaylist == null) {
                targetPlaylist = new Playlist(uploadPlaylistName);
                targetPlaylist.setCreator(currentUser.getUsername());
                targetPlaylist.setDescription("Các bài hát bạn đã tải lên từ máy tính 💻");
                currentUser.getPlayLists().add(targetPlaylist);
                
                // --- [SỬA] Gọi refresh thay vì add thủ công ---
                refreshPlaylistSidebar(); 
            }
            targetPlaylist.addSong(s);
            UserManager.getInstance().saveToJSON();
        } else {
            System.out.println("⚠️ Khách upload nhạc - sẽ không được lưu vào Playlist cá nhân.");
        }
    }

    private void onUserLoggedIn() {
        System.out.println("User logged in: " + currentUser.getUsername());

        libraryView = null;
        refreshPlaylistSidebar();
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

    // =================================================================================================
    // PHẦN 4: PLAYER LOGIC & HELPER
    // =================================================================================================

    private void xuLyPlay() {
        if (player != null) {
            player.play();
            daoTrangThaiNutPlay(true);
            xyLyHieuUngXoay(true);
            batDauDongBoThoiGian();
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
        if (player.getPlaylist() == null || player.getPlaylist().getSongs().isEmpty())
            return;

        // ƯU TIÊN 1: Nếu nút dưới đang bật -> Random lung tung
        if (isShuffle) {
            choiBaiNgauNhien();
        }
        // ƯU TIÊN 2: Nếu nút dưới tắt -> Chạy theo danh sách bên phải
        else {
            player.next();
            xuLyPlay();
            capNhatGiaoDienDuoiCung();
            updateQueueView();
        }
    }

    private void handleSongEnd() {
        Platform.runLater(() -> {
            progressSlider.setValue(0);
            toMauThanhTruot(0, progressSlider.getMax());
            currentTimeLbl.setText("00:00");

            if (isRepeat) {
                player.stop();
                player.seek(0);
                player.play();

                daoTrangThaiNutPlay(true);
                xyLyHieuUngXoay(true);
                batDauDongBoThoiGian();

                System.out.println("🔁 Repeat One: Đã reset và phát lại.");
            } else if (isShuffle) {
                nextSong();
            } else {
                int total = player.getPlaylist().getSongs().size();
                int currentIdx = player.getPlaylist().getSongs().indexOf(player.getCurrentSong());

                if (currentIdx >= total - 1) {
                    if (isQueueLoop) {
                        choiBaiTaiIndex(0);
                    } else {
                        player.stop();
                        daoTrangThaiNutPlay(false);
                        xyLyHieuUngXoay(false);
                    }
                } else {
                    nextSong();
                }
            }
        });
    }

    private void choiBaiTaiIndex(int targetIndex) {
        Playlist currentPl = player.getPlaylist();
        double currentVol = player.getVolume();

        player.stop();

        player = new AudioPlayer(currentPl);
        player.setVolume(currentVol);
        player.setOnSongEnd(this::handleSongEnd);

        for (int i = 0; i < targetIndex; i++)
            player.next();

        xuLyPlay();
        capNhatGiaoDienDuoiCung();
        updateQueueView();
    }

    private void xulyRepeat() {
        isRepeat = !isRepeat;
        setToggleState(repeatButton, onRepeatBtn, isRepeat);
        System.out.println("🔁 Repeat One: " + (isRepeat ? "ON" : "OFF"));
    }

    private void toggleShuffle() {
        isShuffle = !isShuffle;
        setToggleState(shuffleButton, onShuffleBtn, isShuffle);
        System.out.println("🔀 Random Mode: " + (isShuffle ? "ON" : "OFF"));
    }

    private void toggleLike() {
        Song s = player.getCurrentSong();
        if (s == null || currentUser == null)
            return;
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
        com.users.UserManager.getInstance().saveToJSON();
    }

    private void updateLikeButtonState() {
        if (likeBtn == null || player == null)
            return;
        Song s = player.getCurrentSong();
        boolean isLiked = false;
        if (s != null && currentUser != null) {
            Playlist fav = getFavoritesPlaylist();
            for (Song existing : fav.getSongs()) {
                if (existing.getSongID().equals(s.getSongID())) {
                    isLiked = true;
                    break;
                }
            }
        }
        if (isLiked)
            likeBtn.setStyle(
                    "-fx-opacity: 1.0; -fx-effect: dropshadow(three-pass-box, rgba(29,185,84,0.8), 10, 0, 0, 0);");
        else
            likeBtn.setStyle("-fx-opacity: 0.5;");
    }

    private void choiBaiHatCuThe(int index) {
        chuyenManHinh(this::hienThiManHinhPlayer);
        if (player.getMediaPlayer() != null)
            player.stop();
        Playlist q = new Playlist("Queue");
        for (Song s : library.getAllSongs())
            q.addSong(s);
        player = new AudioPlayer(q);
        player.setOnSongEnd(this::handleSongEnd);
        for (int i = 0; i < index; i++)
            player.next();
        xuLyPlay();
        capNhatGiaoDienDuoiCung();
        setupSliderEvents();
        updateQueueView();
    }

    public void choiBaiHatMoi(Song s) {
        Playlist p = new Playlist("Temp");
        p.addSong(s);
        player.setPlaylist(p);
        player.play();
        capNhatGiaoDienDuoiCung();
        chuyenManHinh(this::hienThiManHinhPlayer);
        updateQueueView();
    }

    private void setupSliderEvents() {
        MediaPlayer mp = player.getMediaPlayer();
        if (mp != null) {
            progressSlider.setValue(0);

            mp.setOnReady(() -> {
                progressSlider.setMax(mp.getTotalDuration().toSeconds());
                totalTimeLbl.setText(doiGiaySangPhut(mp.getTotalDuration().toSeconds()));
            });

            mp.currentTimeProperty().addListener((o, old, val) -> {
                if (!dangKeoThanhTruot) {
                    progressSlider.setValue(val.toSeconds());
                    currentTimeLbl.setText(doiGiaySangPhut(val.toSeconds()));
                    toMauThanhTruot(val.toSeconds(), progressSlider.getMax());
                }
            });
        }
    }

    private void daoTrangThaiNutPlay(boolean playing) {
        playButton.setVisible(!playing);
        pauseButton.setVisible(playing);
    }

    private void capNhatGiaoDienDuoiCung() {
        if (bottomPlayerBar != null && !bottomPlayerBar.isVisible()) {
            bottomPlayerBar.setVisible(true);
            bottomPlayerBar.setManaged(true);
        }
        Song s = player.getCurrentSong();
        if (s != null) {
            currentSongLabel.setText(s.getTitle());
            currentArtistLabel.setText(s.getArtist());
            totalTimeLbl.setText(doiGiaySangPhut(s.getDuration()));
            progressSlider.setMax(s.getDuration());
            capNhatAnhDiaNhac(s);

            s.setPlayCount(s.getPlayCount() + 1);
            SongLibrary.getInstance().saveToJSON();

            System.out.println("🎧 Đang phát: " + s.getTitle() + " | Views: " + s.getPlayCount());

            historyManager.addSong(s);
            if (currentUser != null)
                UserManager.getInstance().saveToJSON();
        }
        daoTrangThaiNutPlay(player.isPlaying());
        xyLyHieuUngXoay(player.isPlaying());

        updateLikeButtonColor(); // Tim bài hát
        updateQueueLikeBtnState(); // Tim Playlist

        if (nextTabBtn.isSelected())
            updateQueueView();
        batDauDongBoThoiGian();
    }

    private void capNhatAnhDiaNhac(Song s) {
        if (outerDiscCircle == null) return;
        
        // [THÊM DÒNG NÀY] Gỡ bỏ mọi style màu cứng đầu trước đó
        outerDiscCircle.setStyle(null); 

        try {
            outerDiscCircle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("icons/logo.png"))));
        } catch (Exception e) {
            // Nếu lỗi ảnh thì mới fallback về màu xám
            outerDiscCircle.setFill(javafx.scene.paint.Color.web("#e2e6e9"));
        }
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
            if (lbTitle != null)
                lbTitle.setText(s.getTitle());
            if (lbArtist != null)
                lbArtist.setText(s.getArtist());
            if (lbDuration != null)
                lbDuration.setText(doiGiaySangPhut(s.getDuration()));
            row.setOnMouseClicked(e -> choiBaiHatMoi(s));
            return row;
        } catch (IOException e) {
            return null;
        }
    }

    private void updateQueueView() {
        if (queueContainerVBox == null || player == null)
            return;
        queueContainerVBox.getChildren().clear();

        Playlist pl = player.getPlaylist();
        Song cur = player.getCurrentSong();

        if (pl != null && cur != null) {
            List<Song> all = pl.getSongs();
            int currentIndex = all.indexOf(cur);

            for (int i = 0; i < all.size(); i++) {
                Song s = all.get(i);
                Node row = taoDongBaiHat(s);

                if (row != null) {
                    if (i == currentIndex) {
                        row.setStyle(
                                "-fx-background-color: #2a2a2a; -fx-border-color: transparent transparent transparent #1DB954; -fx-border-width: 0 0 0 4;");
                        Label titleLbl = (Label) row.lookup("#rowTitle");
                        if (titleLbl != null)
                            titleLbl.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold;");
                        row.setOpacity(1.0);
                    } else {
                        row.setStyle("-fx-background-color: transparent;");
                        if (isShuffle) {
                            row.setOpacity(1.0);
                        } else {
                            if (i < currentIndex)
                                row.setOpacity(0.5);
                            else
                                row.setOpacity(1.0);
                        }
                    }

                    int targetIndex = i;
                    row.setOnMouseClicked(e -> choiBaiTaiIndex(targetIndex));

                    queueContainerVBox.getChildren().add(row);
                }
            }

            if (all.size() > 0) {
                double scrollPos = (double) currentIndex / (all.size() - 1);
                Platform.runLater(() -> queueScrollPane.setVvalue(scrollPos));
            }
        }
    }

    private void loadRelatedSongs() {
        if (relatedContainerVBox == null)
            return;
        relatedContainerVBox.getChildren().clear();
        List<Song> rnd = new ArrayList<>(library.getAllSongs());
        Collections.shuffle(rnd);
        int c = 0;
        for (Song s : rnd) {
            if (player.getCurrentSong() != null && s.equals(player.getCurrentSong()))
                continue;
            Node row = taoDongBaiHat(s);
            if (row != null) {
                relatedContainerVBox.getChildren().add(row);
                c++;
            }
            if (c >= 20)
                break;
        }
    }

    private void toMauThanhTruot(double cur, double total) {
        if (total > 0) {
            double p = (cur / total) * 100;
            Node track = progressSlider.lookup(".track");
            if (track != null)
                track.setStyle(String.format(
                        "-fx-background-color: linear-gradient(to right, #ffffff %.2f%%, #404040 %.2f%%);", p, p));
        }
    }

    private void setupDiscAnimation() {
        discRotation = new RotateTransition(Duration.seconds(20), outerDiscCircle);
        discRotation.setByAngle(360);
        discRotation.setCycleCount(RotateTransition.INDEFINITE);
        discRotation.setInterpolator(Interpolator.LINEAR);
    }

    private void xyLyHieuUngXoay(boolean run) {
        if (discRotation == null)
            return;
        if (run && discRotation.getStatus() != javafx.animation.Animation.Status.RUNNING)
            discRotation.play();
        else if (!run)
            discRotation.pause();
    }

    private String doiGiaySangPhut(double sec) {
        return String.format("%02d:%02d", (int) sec / 60, (int) sec % 60);
    }

    @FXML
    public void handleVolumeUp(javafx.scene.input.SwipeEvent e) {
        volumeSlider.setValue(Math.min(100, volumeSlider.getValue() + 10));
    }

    @FXML
    public void handleVolumeDown(javafx.scene.input.SwipeEvent e) {
        volumeSlider.setValue(Math.max(0, volumeSlider.getValue() - 10));
    }

    private void xuLyTimKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            hienThiManHinhHome();
            return;
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
        for (Song s : ketQua)
            playlistKetQua.addSong(s);
        hienThiChiTietPlaylist(playlistKetQua);
    }

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
            currentTimerDialog.setDialogStage(dialogStage, sleepTimer.isActive(), sleepTimer.getTimeRemaining(),
                    (val) -> {
                        if (val == -1) {
                            stopCountdownUI();
                            sleepTimer.cancelTimer();
                        } else if (val > 0) {
                            startCountdownUI(val);
                        }
                        currentTimerDialog = null;
                    });
            dialogStage.showAndWait();
            currentTimerDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountdownUI(int totalSeconds) {
        if (sleepTimer != null)
            sleepTimer.cancelTimer();
        sleepTimer = new Timer();
        sleepTimer.setTimer(totalSeconds);
        sleepTimer.addListener(new TimerListener() {
            @Override
            public void onTimerFinished() {
                Platform.runLater(() -> {
                    if (player != null && player.isPlaying())
                        xuLyPause();
                    stopCountdownUI();
                    System.out.println("⏰ Hết giờ! Đã tắt nhạc.");
                });
            }

            @Override
            public void onTimerCancelled() {
            }
        });
        if (uiUpdateTimeline != null)
            uiUpdateTimeline.stop();
        uiUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            int remaining = sleepTimer.getTimeRemaining();
            if (remaining <= 0) {
                stopCountdownUI();
                return;
            }
            String timeText = (remaining >= 3600)
                    ? String.format("%02d:%02d:%02d", remaining / 3600, (remaining % 3600) / 60, remaining % 60)
                    : String.format("%02d:%02d", remaining / 60, remaining % 60);
            timerBtn.setText(timeText);
            timerBtn.setAlignment(javafx.geometry.Pos.CENTER);
            timerBtn.setContentDisplay(ContentDisplay.RIGHT);
            timerBtn.setStyle(
                    "-fx-text-fill: #1DB954; -fx-font-weight: bold; -fx-background-color: transparent; -fx-alignment: center;");
            if (currentTimerDialog != null)
                currentTimerDialog.updateCountdownTime(remaining);
        }));
        uiUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        uiUpdateTimeline.play();
    }

    private void stopCountdownUI() {
        if (uiUpdateTimeline != null)
            uiUpdateTimeline.stop();
        if (sleepTimer != null)
            sleepTimer.cancelTimer();
        timerBtn.setText("");
        timerBtn.setStyle("-fx-background-color: transparent;");
        timerBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        timerBtn.setAlignment(javafx.geometry.Pos.CENTER);
    }

    private void setupPlaylistListView() {
        playlistListView.getStyleClass().add("playlist-list");
        playlistListView.setCellFactory(param -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("PlaylistRow.fxml"));
                        VBox root = loader.load();
                        Label nameLbl = (Label) root.lookup("#playlistName");
                        Label creatorLbl = (Label) root.lookup("#playlistCreator");
                        nameLbl.setText(item.getTitle());
                        creatorLbl.setText(item.getCreator());
                        setGraphic(root);
                        setText(null);
                    } catch (IOException e) {
                        setText(item.getTitle());
                    }
                }
            }
        });
        playlistListView.setOnMouseClicked(event -> {
            Playlist selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null)
                hienThiChiTietPlaylist(selectedPlaylist);
        });
    }

    private void chuyenManHinh(Runnable viewMethod) {
        if (currentViewAction == viewMethod)
            return;
        if (currentViewAction != null)
            backStack.push(currentViewAction);
        forwardStack.clear();
        currentViewAction = viewMethod;
        viewMethod.run();
        updateNavigationButtons();
    }

    private void handleBackNav() {
        isPlayerMode = false;
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
        isPlayerMode = false;
        resetSearchState();
        if (libraryView == null)
            chuanBiGiaoDienLibrary();
        mainRoot.setCenter(libraryView);
        mainRoot.setRight(null);
    }

    private void hienThiManHinhPlayer() {
        resetSearchState();
        if (discContainer != null) {
            mainRoot.setCenter(discContainer);
            mainRoot.setRight(savedRightSidebar);
            isPlayerMode = true;
        }
    }

    private void switchSidebarTab(boolean isNextTab) {
        if (queueTabContent == null || relatedScrollPane == null)
            return;
        Node toShow = isNextTab ? queueTabContent : relatedScrollPane;
        Node toHide = isNextTab ? relatedScrollPane : queueTabContent;
        toHide.setVisible(false);
        toHide.setManaged(false);
        if (!isNextTab)
            loadRelatedSongs();
        else
            updateQueueView();
        toShow.setVisible(true);
        toShow.setManaged(true);
    }

    private void capNhatIconVolume(double value) {
        if (value > 0) {
            if (volumeBtn != null) {
                volumeBtn.setVisible(true);
                volumeBtn.setManaged(true);
            }
            if (mutedBtn != null) {
                mutedBtn.setVisible(false);
                mutedBtn.setManaged(false);
            }
        } else {
            if (volumeBtn != null) {
                volumeBtn.setVisible(false);
                volumeBtn.setManaged(false);
            }
            if (mutedBtn != null) {
                mutedBtn.setVisible(true);
                mutedBtn.setManaged(true);
            }
        }
    }

    @FXML
    private void xuLyNutAddSongToPlaylist() {
        if (player.getCurrentSong() == null) {
            System.out.println("⚠️ Chưa có bài hát nào đang phát!");
            return;
        }
        isSearchingPlaylist = true;
        searchField.clear();
        searchField.setPromptText("🔍 Nhập tên Playlist để tìm...");
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #121212;");
        Label title = new Label("Thêm \"" + player.getCurrentSong().getTitle() + "\" vào...");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        Button cancelBtn = new Button("Hủy bỏ");
        cancelBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #b3b3b3; -fx-border-color: #b3b3b3; -fx-border-radius: 20;");
        cancelBtn.setOnAction(e -> {
            resetSearchState();
            hienThiManHinhPlayer();
        });
        currentPlaylistContainer = new FlowPane(20, 20);
        currentPlaylistContainer.setPadding(new Insets(10, 0, 0, 0));
        List<Playlist> allLists = new ArrayList<>(playlistLibrary.getAllPlaylists());
        if (currentUser != null) {
            for (Playlist p : currentUser.getPlayLists()) {
                if (!allLists.contains(p))
                    allLists.add(p);
            }
        }
        veDanhSachPlaylist(allLists, currentPlaylistContainer);
        mainLayout.getChildren().addAll(title, cancelBtn, currentPlaylistContainer);
        ScrollPane scroll = new ScrollPane(mainLayout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #121212; -fx-background-color: transparent;");
        scroll.getStyleClass().add("main-scroll-pane");
        chuyenManHinh(() -> {
            mainRoot.setCenter(scroll);
            mainRoot.setRight(null);
        });
    }

    private void veDanhSachPlaylist(List<Playlist> dsPlaylist, FlowPane container) {
        container.getChildren().clear();
        if (dsPlaylist == null || dsPlaylist.isEmpty()) {
            Label empty = new Label("Không tìm thấy Playlist nào.");
            empty.setStyle("-fx-text-fill: #808080; -fx-font-size: 14px;");
            container.getChildren().add(empty);
            return;
        }
        for (Playlist p : dsPlaylist) {
            if (p.getTitle().equals("Queue") || p.getTitle().equals("Temp"))
                continue;
            VBox card = new VBox(10);
            card.setPrefSize(160, 160);
            card.setStyle(
                    "-fx-background-color: #282828; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0); -fx-cursor: hand; -fx-alignment: center;");
            ImageView icon = new ImageView();
            try {
                String imgPath = "icons/logo.png";
                if (p.getTitle().toLowerCase().contains("yêu thích"))
                    imgPath = "icons/heart.png";
                icon.setImage(new Image(getClass().getResourceAsStream(imgPath)));
            } catch (Exception e) {
            }
            icon.setFitWidth(60);
            icon.setFitHeight(60);
            Label nameLbl = new Label(p.getTitle());
            nameLbl.setStyle(
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-wrap-text: true; -fx-text-alignment: center;");
            nameLbl.setMaxWidth(140);
            Label countLbl = new Label(p.getSongs().size() + " bài hát");
            countLbl.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12px;");
            card.getChildren().addAll(icon, nameLbl, countLbl);
            card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color: #3E3E3E; -fx-background-radius: 10; -fx-cursor: hand; -fx-alignment: center;"));
            card.setOnMouseExited(e -> card
                    .setStyle("-fx-background-color: #282828; -fx-background-radius: 10; -fx-alignment: center;"));
            card.setOnMouseClicked(e -> xuLyThemBaiHatVaoPlaylist(p));
            container.getChildren().add(card);
        }
    }

    private void xuLyThemBaiHatVaoPlaylist(Playlist targetPlaylist) {
        Song currentSong = player.getCurrentSong();
        if (currentSong == null)
            return;
        for (Song s : targetPlaylist.getSongs()) {
            if (s.getSongID().equals(currentSong.getSongID())) {
                showAlert("Thông báo", "Bài hát này đã có trong Playlist rồi!");
                return;
            }
        }
        targetPlaylist.addSong(currentSong);
        playlistLibrary.saveToJSON();
        if (currentUser != null && currentUser.getPlayLists().contains(targetPlaylist)) {
            com.users.UserManager.getInstance().saveToJSON();
        }
        System.out.println("✅ Đã thêm bài " + currentSong.getTitle() + " vào " + targetPlaylist.getTitle());
        resetSearchState();
        hienThiManHinhPlayer();
        showAlert("Thành công", "Đã thêm vào " + targetPlaylist.getTitle());
    }

    private void locDanhSachPlaylist(String keyword) {
        if (currentPlaylistContainer == null)
            return;
        String key = keyword.toLowerCase().trim();
        List<Playlist> ketQua = new ArrayList<>();
        List<Playlist> allLists = new ArrayList<>(playlistLibrary.getAllPlaylists());
        if (currentUser != null) {
            for (Playlist p : currentUser.getPlayLists()) {
                if (!allLists.contains(p))
                    allLists.add(p);
            }
        }
        for (Playlist p : allLists) {
            if (p.getTitle().toLowerCase().contains(key)) {
                ketQua.add(p);
            }
        }
        veDanhSachPlaylist(ketQua, currentPlaylistContainer);
    }

    private void resetSearchState() {
        isSearchingPlaylist = false;
        searchField.clear();
        searchField.setPromptText("Tìm kiếm bài hát, nghệ sĩ...");
        currentPlaylistContainer = null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }

    private void luuQueueVaoPlaylistMoi() {
        if (!checkLogin())
            return;
        if (player == null || player.getPlaylist() == null || player.getPlaylist().getSongs().isEmpty()) {
            showAlert("Thông báo", "Danh sách phát đang trống!");
            return;
        }
        List<Song> currentActiveList = player.getPlaylist().getSongs();
        int currentIndex = 0;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreatePlaylistDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainRoot.getScene().getWindow());
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(page));
            CreatePlaylistController controller = loader.getController();
            controller.setDialogStage(dialogStage, (playlistMoi) -> {
                List<Song> remainingSongs = currentActiveList.subList(currentIndex, currentActiveList.size());
                for (Song s : remainingSongs) {
                    playlistMoi.addSong(s);
                }
                if (currentUser != null) {
                    playlistMoi.setCreator(currentUser.getUsername());
                    playlistLibrary.addPlaylist(playlistMoi);
                    playlistLibrary.saveToJSON();
                    currentUser.getPlayLists().add(playlistMoi);
                    UserManager.getInstance().saveToJSON();
                } else {
                    playlistMoi.setCreator("Khách");
                    playlistLibrary.addPlaylist(playlistMoi);
                }
                if (playlistListView != null) {
                    refreshPlaylistSidebar();
                }
                showAlert("Thành công",
                        "Đã lưu " + remainingSongs.size() + " bài hát vào playlist: " + playlistMoi.getTitle());
            });
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void xuLyQueueRepeat() {
        isQueueLoop = !isQueueLoop;
        updateQueueRepeatBtnState();
        System.out.println("🔁 Loop Playlist: " + (isQueueLoop ? "ON" : "OFF"));
    }

    private void xuLyQueueShuffle() {
        if (player == null || player.getPlaylist() == null)
            return;

        Playlist currentPl = player.getPlaylist();
        List<Song> originalList = currentPl.getSongs();
        if (originalList.isEmpty())
            return;

        Song currentSong = player.getCurrentSong();

        List<Song> newList = new ArrayList<>(originalList);
        newList.remove(currentSong);
        Collections.shuffle(newList);

        newList.add(0, currentSong);

        currentPl.setSongs(newList);

        player.setCurrentIndex(0);

        updateQueueView();

        System.out.println("🔀 Queue Shuffle: Đã trộn danh sách (Nhạc vẫn chạy mượt)");
    }

    public void showUserScreen() {
        chuyenManHinh(() -> {
            try {
                resetSidebarStyles();
                User currentUser = UserManager.getInstance().getCurrentUser();
                String fxmlPath = "";
                if (currentUser == null) {
                    fxmlPath = "/com/users/GuestView.fxml";
                } else {
                    fxmlPath = "/com/users/UserProfileView.fxml";
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent view = loader.load();
                if (currentUser != null && loader.getController() instanceof UserProfileController) {
                    UserProfileController profileCtrl = loader.getController();
                    profileCtrl.setUserData(currentUser);
                }
                mainRoot.setCenter(view);
                mainRoot.setRight(null);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Lỗi load màn hình User: " + e.getMessage());
            }
        });
    }

    private Playlist getFavoritesPlaylist() {
        if (currentUser == null)
            return null;
        for (Playlist p : currentUser.getPlayLists()) {
            if (p.getTitle().equals("Favorites"))
                return p;
        }
        Playlist fav = new Playlist("Favorites");
        fav.setCreator(currentUser.getUsername());
        fav.setDescription("Bài hát đã thả tim");
        currentUser.getPlayLists().add(fav);
        com.users.UserManager.getInstance().saveToJSON();
        return fav;
    }

    public void userLogout() {
        UserManager.getInstance().logout();
        this.currentUser = null;
        if (playlistListView != null) {
            playlistListView.getItems().removeIf(p -> !p.getCreator().equals("Hệ thống"));
        }
        this.historyManager = new History();
        showUserScreen();
        System.out.println("👋 Đã đăng xuất và dọn dẹp sạch sẽ!");
    }

    private void toggleLikePlaylist() {
        if (!checkLogin())
            return;
        if (currentUser == null) {
            showUserScreen();
            return;
        }
        Playlist currentPl = player.getPlaylist();
        if (currentPl == null || currentPl.getTitle().equals("Queue") || currentPl.getTitle().equals("Temp")) {
            showAlert("Thông báo", "Không thể thích danh sách chờ tạm thời.");
            return;
        }
        List<Playlist> likedList = currentUser.getLikedPlaylists();
        boolean isLiked = false;
        for (Playlist p : likedList) {
            if (p.getTitle().equals(currentPl.getTitle()) && p.getCreator().equals(currentPl.getCreator())) {
                isLiked = true;
                likedList.remove(p);
                break;
            }
        }
        if (!isLiked) {
            likedList.add(currentPl);
            System.out.println("❤️ Đã thích playlist: " + currentPl.getTitle());
        } else {
            System.out.println("💔 Đã bỏ thích playlist: " + currentPl.getTitle());
        }
        UserManager.getInstance().saveToJSON();
        updateQueueLikeBtnState();
    }

    private void updateQueueLikeBtnState() {
        // 1. Kiểm tra null an toàn
        if (queueLikeBtn == null || currentUser == null || player == null || player.getPlaylist() == null) {
            setToggleState(queueLikeBtn, onQueueLikeBtn, false);
            return;
        }
        
        Playlist currentPl = player.getPlaylist();

        // 2. Mở khóa nút bấm (trừ khi là Queue/Temp thì chặn)
        if(currentPl.getTitle().equals("Queue") || currentPl.getTitle().equals("Temp")) {
             // Nếu là playlist tạm thì ẩn luôn cho đỡ rối (hoặc disable tùy ông)
             // Ở đây tôi cho ẩn nút ON đi, hiện nút OFF
             setToggleState(queueLikeBtn, onQueueLikeBtn, false);
             queueLikeBtn.setDisable(true); // Disable nút off
             return;
        } else {
             queueLikeBtn.setDisable(false); // Các playlist khác thì bấm thoải mái
        }

        // 3. Check xem đã Like chưa
        boolean isLiked = false;
        if (currentUser.getLikedPlaylists() != null) {
            for (Playlist p : currentUser.getLikedPlaylists()) {
                if (p.getTitle().equals(currentPl.getTitle()) && 
                    p.getCreator().equals(currentPl.getCreator())) {
                    isLiked = true;
                    break;
                }
            }
        }

        // 4. Tráo nút (Đã có ảnh nên không cần setStyle màu mè nữa)
        setToggleState(queueLikeBtn, onQueueLikeBtn, isLiked);
    }

    @FXML
    private void handleLike() {
        if (!checkLogin())
            return;
        User user = UserManager.getInstance().getCurrentUser();
        if (user == null) {
            System.out.println("⚠️ Chưa đăng nhập, không thể like!");
            return;
        }
        Song currentSong = player.getCurrentSong();
        if (currentSong == null)
            return;
        Playlist favPlaylist = null;
        for (Playlist p : user.getPlayLists()) {
            if (p.getTitle().equalsIgnoreCase("Favorites")) {
                favPlaylist = p;
                break;
            }
        }
        if (favPlaylist == null) {
            favPlaylist = new Playlist("Favorites");
            favPlaylist.setCreator(user.getUsername());
            favPlaylist.setDescription("Bài hát đã thả tim");
            user.getPlayLists().add(favPlaylist);
        }
        boolean isLiked = false;
        Song songToRemove = null;
        for (Song s : favPlaylist.getSongs()) {
            if (s.getSongID().equals(currentSong.getSongID())) {
                isLiked = true;
                songToRemove = s;
                break;
            }
        }
        if (isLiked) {
            favPlaylist.removeSong(songToRemove);
            System.out.println("💔 Đã bỏ thích: " + currentSong.getTitle());
        } else {
            favPlaylist.addSong(currentSong);
            System.out.println("❤️ Đã thích: " + currentSong.getTitle());
        }
        UserManager.getInstance().saveToJSON();
        updateLikeButtonColor();
    }

    // [UPDATE GIAO DIỆN] Tên bài hát sáng rõ + Nút Footer đóng khung xịn
    @FXML
    private void handleAddToPlaylist() {
        if (!checkLogin()) return;
        Song currentSong = player.getCurrentSong();
        if (currentSong == null) {
            showAlert("Lỗi", "Chưa có bài hát nào đang phát!");
            return;
        }

        Stage popupStage = new Stage();
        popupStage.initOwner(mainRoot.getScene().getWindow());
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #282828; -fx-background-radius: 12; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0); " +
                      "-fx-border-color: #404040; -fx-border-radius: 12; -fx-border-width: 1;");
        root.setPrefWidth(480);

        // --- HEADER ---
        Label header = new Label("Quản lý danh sách phát");
        header.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 14px; -fx-font-weight: bold;"); // Tiêu đề nhỏ lại chút cho tinh tế
        
        // [SỬA] Tên bài hát: To, Trắng, Đậm
        Label songName = new Label(currentSong.getTitle() + " - " + currentSong.getArtist());
        songName.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 5 0;");

        List<Playlist> canAddList = new ArrayList<>();
        List<Playlist> canRemoveList = new ArrayList<>();

        for (Playlist p : currentUser.getPlayLists()) {
            boolean isOwner = p.getCreator().equals(currentUser.getUsername());
            boolean isSystem = p.getTitle().equalsIgnoreCase("Favorites") || 
                               p.getTitle().equalsIgnoreCase("History") ||
                               p.getTitle().equalsIgnoreCase("Nhạc tải lên") ||
                               p.getTitle().equalsIgnoreCase("Bài hát yêu thích") ||
                               p.getTitle().equalsIgnoreCase("Nghe gần đây");
            
            if (isOwner && !isSystem) {
                boolean hasSong = false;
                for (Song s : p.getSongs()) {
                    if (s.getSongID().equals(currentSong.getSongID())) {
                        hasSong = true; break;
                    }
                }
                if (hasSong) canRemoveList.add(p); else canAddList.add(p);
            }
        }

        // --- SECTION ADD ---
        Label lblAdd = new Label("➕ Thêm vào:");
        lblAdd.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        ComboBox<Playlist> cbAdd = new ComboBox<>();
        cbAdd.getItems().addAll(canAddList);
        cbAdd.setPromptText(canAddList.isEmpty() ? "Không có playlist khả dụng" : "Chọn playlist...");
        cbAdd.setMaxWidth(Double.MAX_VALUE);
        stylePlaylistComboBox(cbAdd); 

        Button btnConfirmAdd = new Button("Thêm");
        styleActionButton(btnConfirmAdd, true);
        btnConfirmAdd.setOnAction(e -> {
            Playlist target = cbAdd.getValue();
            if (target != null) {
                target.addSong(currentSong);
                saveAllChanges();
                showAlert("Thành công", "Đã thêm vào: " + target.getTitle());
                popupStage.close();
            }
        });
        HBox boxAdd = new HBox(10, cbAdd, btnConfirmAdd);
        HBox.setHgrow(cbAdd, Priority.ALWAYS);

        // --- SECTION REMOVE ---
        Label lblRemove = new Label("Xóa khỏi:");
        lblRemove.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        ComboBox<Playlist> cbRemove = new ComboBox<>();
        cbRemove.getItems().addAll(canRemoveList);
        cbRemove.setPromptText(canRemoveList.isEmpty() ? "Chưa có trong playlist nào" : "Chọn playlist...");
        cbRemove.setMaxWidth(Double.MAX_VALUE);
        stylePlaylistComboBox(cbRemove);

        Button btnConfirmRemove = new Button("Xóa");
        styleActionButton(btnConfirmRemove, false);
        btnConfirmRemove.setOnMouseEntered(e -> btnConfirmRemove.setStyle("-fx-background-color: #ff5555; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnConfirmRemove.setOnMouseExited(e -> btnConfirmRemove.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));

        btnConfirmRemove.setOnAction(e -> {
            Playlist target = cbRemove.getValue();
            if (target != null) {
                target.getSongs().removeIf(s -> s.getSongID().equals(currentSong.getSongID()));
                saveAllChanges();
                showAlert("Thành công", "Đã xóa khỏi: " + target.getTitle());
                popupStage.close();
            }
        });
        HBox boxRemove = new HBox(10, cbRemove, btnConfirmRemove);
        HBox.setHgrow(cbRemove, Priority.ALWAYS);

        // --- FOOTER (ĐÃ SỬA GIAO DIỆN) ---
        Separator sep = new Separator(); sep.setOpacity(0.3);

        // Nút Tạo mới: Đóng khung, nền xám, chữ trắng
        Button btnCreate = new Button("Tạo Playlist mới");
        btnCreate.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 15;");
        // Hover thì sáng lên tí
        btnCreate.setOnMouseEntered(e -> btnCreate.setStyle("-fx-background-color: #4D4D4D; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 15;"));
        btnCreate.setOnMouseExited(e -> btnCreate.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 15;"));
        
        btnCreate.setOnAction(e -> {
            popupStage.close();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CreatePlaylistDialog.fxml"));
                Parent page = loader.load();
                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(mainRoot.getScene().getWindow());
                dialogStage.initStyle(StageStyle.UNDECORATED);
                dialogStage.setScene(new Scene(page));
                CreatePlaylistController controller = loader.getController();
                controller.setDialogTitle("Tạo playlist mới");
                controller.setDialogStage(dialogStage, (newPl) -> {
                    if (currentUser != null) {
                        newPl.setCreator(currentUser.getUsername());
                        playlistLibrary.addPlaylist(newPl);
                        currentUser.getPlayLists().add(newPl);
                        newPl.addSong(currentSong); // Auto add
                        saveAllChanges();
                    }
                    if (playlistListView != null) playlistListView.getItems().add(newPl);
                    showAlert("Thành công", "Đã tạo \"" + newPl.getTitle() + "\" và thêm bài hát vào!");
                });
                dialogStage.showAndWait();
            } catch (IOException ex) { ex.printStackTrace(); }
        });

        // Nút Đóng: Đóng khung giống nút Tạo mới
        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 15;");
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: #4D4D4D; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 15;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: #3E3E3E; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 15;"));
        
        btnClose.setOnAction(e -> popupStage.close());

        HBox footer = new HBox(10, btnCreate, new Region(), btnClose);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS); // Spacer ở giữa đẩy 2 nút ra 2 bên
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        root.getChildren().addAll(header, songName, new Separator(), lblAdd, boxAdd, lblRemove, boxRemove, sep, footer);
        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popupStage.setScene(scene);
        popupStage.show();
    }

    // --- HÀM BỔ TRỢ STYLE NÚT HÀNH ĐỘNG ---
    private void styleActionButton(Button btn, boolean isPrimary) {
        if (isPrimary) {
            btn.setStyle("-fx-background-color: #1DB954; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #1ed760; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #1DB954; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
        } else {
            btn.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #4d4d4d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
        }
        btn.setPrefWidth(70);
    }

    // Hàm lưu chung cho gọn
    private void saveAllChanges() {
        UserManager.getInstance().saveToJSON();
        PlaylistLibrary.getInstance().saveToJSON();
    }

    private void updateLikeButtonColor() {
        User user = UserManager.getInstance().getCurrentUser();
        Song currentSong = player.getCurrentSong();
        if (user == null || currentSong == null) {
            setToggleState(likeBtn, onLikeBtn, false);
            return;
        }
        boolean isLiked = false;
        for (Playlist p : user.getPlayLists()) {
            if (p.getTitle().equalsIgnoreCase("Favorites")) {
                for (Song s : p.getSongs()) {
                    if (s.getSongID().equals(currentSong.getSongID())) {
                        isLiked = true;
                        break;
                    }
                }
                break;
            }
        }
        setToggleState(likeBtn, onLikeBtn, isLiked);
    }

    private void capNhatMauVolume() {
        Node track = volumeSlider.lookup(".track");
        if (track != null) {
            double val = volumeSlider.getValue();
            double max = volumeSlider.getMax();
            double p = (val / max) * 100;
            String style = String
                    .format("-fx-background-color: linear-gradient(to right, #FFFFFF %.2f%%, #404040 %.2f%%);", p, p);
            track.setStyle(style);
        }
    }

    private void updateQueueRepeatBtnState() {
        setToggleState(queueRepeatBtn, onQueueRepeatBtn, isQueueLoop);
    }

    private void choiBaiNgauNhien() {
        int size = player.getPlaylist().getSongs().size();
        if (size <= 1)
            return;

        int currentIdx = player.getPlaylist().getSongs().indexOf(player.getCurrentSong());
        int newIdx = currentIdx;

        while (newIdx == currentIdx) {
            newIdx = (int) (Math.random() * size);
        }

        Playlist currentPl = player.getPlaylist();
        double vol = player.getVolume();
        player.stop();

        player = new AudioPlayer(currentPl);
        player.setVolume(vol);
        player.setOnSongEnd(this::handleSongEnd);

        for (int i = 0; i < newIdx; i++)
            player.next();

        xuLyPlay();
        capNhatGiaoDienDuoiCung();
        updateQueueView();
    }

    private boolean checkLogin() {
        if (currentUser != null) {
            return true;
        }
        System.out.println("⚠️ Chưa đăng nhập -> Hiện form Login (Nhạc vẫn chạy)");
        showUserScreen();
        return false;
    }

    @FXML
    private void handleShare() {
        if (!checkLogin())
            return;

        Song s = player.getCurrentSong();
        if (s != null) {
            showAlert("Chia sẻ", "Đã sao chép liên kết bài hát: " + s.getTitle());
        } else {
            showAlert("Thông báo", "Chưa có bài hát nào đang phát.");
        }
    }

    private void setToggleState(Button offBtn, Button onBtn, boolean isOn) {
        if (offBtn == null || onBtn == null)
            return;

        if (isOn) {
            offBtn.setVisible(false);
            offBtn.setManaged(false);
            onBtn.setVisible(true);
            onBtn.setManaged(true);
        } else {
            onBtn.setVisible(false);
            onBtn.setManaged(false);
            offBtn.setVisible(true);
            offBtn.setManaged(true);
        }
    }

    // [NEW] Hàm làm mới Sidebar có sắp xếp
    private void refreshPlaylistSidebar() {
        if (playlistListView == null || currentUser == null) return;

        // 1. Xóa danh sách cũ trên giao diện
        playlistListView.getItems().clear();

        // 2. Lấy danh sách playlist của User
        List<Playlist> userPlaylists = new ArrayList<>(currentUser.getPlayLists());

        // 3. [LOGIC SẮP XẾP]
        userPlaylists.sort((p1, p2) -> {
            String title1 = p1.getTitle();
            String title2 = p2.getTitle();

            // Ưu tiên 1: "Nhạc tải lên" luôn lên đầu
            if (title1.equals("Nhạc tải lên")) return -1; // p1 lên trước
            if (title2.equals("Nhạc tải lên")) return 1;  // p2 lên trước

            // Ưu tiên 2: Sắp xếp Alphabet (A-Z) không phân biệt hoa thường
            return title1.compareToIgnoreCase(title2);
        });

        // 4. Đẩy lại vào giao diện (Trừ Favorites ra vì nó có nút riêng rồi)
        for (Playlist p : userPlaylists) {
            if (!p.getTitle().equals("Favorites")) {
                playlistListView.getItems().add(p);
            }
        }
    }

    // [HÀM MỚI] Chèn dòng trạng thái và nút sửa vào giao diện Playlist
    private void addStatusAndEditRow(Label descLbl, Playlist p) {
        if (descLbl.getParent() instanceof VBox) {
            VBox parentContainer = (VBox) descLbl.getParent();
            
            // Xóa dòng cũ
            parentContainer.getChildren().removeIf(node -> "statusRow".equals(node.getId()));

            HBox statusRow = new HBox(15);
            statusRow.setId("statusRow");
            statusRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            statusRow.setPadding(new Insets(0, 0, 10, 0));

            // --- 1. XỬ LÝ TRẠNG THÁI (CÔNG KHAI / RIÊNG TƯ) ---
            String privacyText = "Công khai";
            String icon = "🌐";
            String title = p.getTitle();

            // A. Check playlist hệ thống (Mặc định là Riêng tư)
            boolean isSystemPlaylist = title.equalsIgnoreCase("Favorites") || 
                                     title.equalsIgnoreCase("Bài hát yêu thích") ||
                                     title.equalsIgnoreCase("History") || 
                                     title.equalsIgnoreCase("Nghe gần đây") ||
                                     title.equalsIgnoreCase("Nhạc tải lên"); // Nhạc tải lên là hệ thống

            if (isSystemPlaylist) {
                privacyText = "Riêng tư";
                icon = "🔒";
            } 
            // B. Check playlist người dùng tạo (omega, test...)
            else {
                // [QUAN TRỌNG] Lấy dữ liệu Privacy thực tế đã lưu
                // Giả sử trong class Playlist ông có hàm getPrivacy() trả về String "Private" hoặc "Riêng tư"
                String pStatus = "";
                try {
                     pStatus = p.getPrivacy(); // Nếu chưa có hàm này thì thêm vào class Playlist nhé
                } catch (Exception e) { 
                     pStatus = "Public"; 
                }

                if (pStatus != null && (pStatus.equalsIgnoreCase("Private") || pStatus.equalsIgnoreCase("Riêng tư"))) {
                    privacyText = "Riêng tư";
                    icon = "🔒";
                }
            }

            Label statusLbl = new Label(icon + " " + privacyText);
            statusLbl.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px; -fx-font-weight: bold;");

            // --- 2. XỬ LÝ NÚT CHỈNH SỬA ---
            boolean showEditBtn = currentUser != null && p.getCreator().equals(currentUser.getUsername());

            // [FIX] Ẩn nút sửa cho Playlist hệ thống (Bao gồm cả Nhạc tải lên) & Top 100
            if (isSystemPlaylist || title.toLowerCase().contains("top 100")) {
                showEditBtn = false;
            }

            Button editBtn = new Button("Chỉnh sửa");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #b3b3b3; -fx-border-radius: 4; -fx-font-size: 11px; -fx-cursor: hand;");
            
            editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 4; -fx-font-size: 11px; -fx-cursor: hand;"));
            editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #b3b3b3; -fx-border-radius: 4; -fx-font-size: 11px; -fx-cursor: hand;"));
            
            editBtn.setOnAction(e -> handleEditPlaylist(p));

            statusRow.getChildren().add(statusLbl);
            if (showEditBtn) {
                statusRow.getChildren().add(editBtn);
            }

            int index = parentContainer.getChildren().indexOf(descLbl);
            if (index >= 0) {
                parentContainer.getChildren().add(index, statusRow);
            }
        }
    }

    // [HÀM MỚI] Xử lý khi bấm nút "Chỉnh sửa"
    private void handleEditPlaylist(Playlist p) {
        // 1. Tạo Stage (Cửa sổ) mới với nền trong suốt
        Stage editStage = new Stage();
        editStage.initOwner(mainRoot.getScene().getWindow());
        editStage.initModality(Modality.WINDOW_MODAL); // Chặn cửa sổ chính
        editStage.initStyle(StageStyle.TRANSPARENT);   // Không viền window

        // 2. Tạo Layout chính (VBox)
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        // Style Dark Mode xịn sò
        root.setStyle("-fx-background-color: #282828; -fx-background-radius: 15; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 0); " +
                      "-fx-border-color: #404040; -fx-border-radius: 15; -fx-border-width: 1;");
        root.setPrefWidth(500); // Rộng 500px

        // --- HEADER ---
        Label header = new Label("Chỉnh sửa thông tin");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        // --- FORM NHẬP LIỆU ---
        
        // Tên
        Label nameLbl = new Label("Tên Playlist");
        nameLbl.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 12px; -fx-font-weight: bold;");
        TextField nameField = new TextField(p.getTitle());
        styleInput(nameField); // Hàm style ở dưới

        // Quyền riêng tư
        Label privacyLbl = new Label("Quyền riêng tư");
        privacyLbl.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 12px; -fx-font-weight: bold;");
        ComboBox<String> privacyBox = new ComboBox<>();
        privacyBox.getItems().addAll("Công khai", "Riêng tư");
        styleComboBox(privacyBox);
        
        // Set giá trị mặc định cho Privacy
        String currentPriv = "Công khai";
        try {
            if (p.getPrivacy() != null && (p.getPrivacy().equalsIgnoreCase("Riêng tư") || p.getPrivacy().equalsIgnoreCase("Private"))) {
                currentPriv = "Riêng tư";
            }
        } catch (Exception e) {}
        privacyBox.setValue(currentPriv);

        // Mô tả
        Label descLbl = new Label("Mô tả");
        descLbl.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 12px; -fx-font-weight: bold;");
        TextArea descArea = new TextArea(p.getDescription() != null ? p.getDescription() : "");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);
        styleTextArea(descArea); // Hàm style ở dưới

        // Gom nhóm Form
        VBox formLayout = new VBox(10);
        formLayout.getChildren().addAll(nameLbl, nameField, privacyLbl, privacyBox, descLbl, descArea);

        // --- BUTTONS ---
        
        // Nút XÓA (Màu đỏ, nằm bên trái)
        Button deleteBtn = new Button("Xóa Playlist");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #ff5555; -fx-border-radius: 20;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: rgba(255, 85, 85, 0.1); -fx-text-fill: #ff5555; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #ff5555; -fx-border-radius: 20;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #ff5555; -fx-border-radius: 20;"));
        
        // Logic Xóa
        deleteBtn.setOnAction(e -> {
            // Xác nhận xóa
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xóa Playlist");
            alert.setHeaderText("Bạn có chắc muốn xóa playlist này?");
            alert.setContentText("Hành động này không thể hoàn tác.");
            alert.initOwner(editStage);
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                // 1. Xóa khỏi User hiện tại (Giữ nguyên)
                if (currentUser != null) {
                    currentUser.getPlayLists().remove(p);
                    UserManager.getInstance().saveToJSON();
                }

                // 2. [FIX QUAN TRỌNG] Xóa khỏi Thư viện tổng bằng cách so sánh Tên & Creator
                // Thay vì remove(p) bình thường, ta dùng removeIf để tìm đúng kẻ cần xóa
                PlaylistLibrary.getInstance().getAllPlaylists().removeIf(globalPl -> 
                    globalPl.getTitle().equals(p.getTitle()) && 
                    globalPl.getCreator().equals(p.getCreator())
                );
                PlaylistLibrary.getInstance().saveToJSON();
                
                editStage.close();
                hienThiManHinhHome(); // Về trang chủ để cập nhật lại list
                refreshPlaylistSidebar(); // Load lại sidebar
                showAlert("Thông báo", "Đã xóa playlist thành công.");
            }
        });

        // Nút HỦY
        Button cancelBtn = new Button("Hủy");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> editStage.close());

        // Nút LƯU (Màu trắng, nổi bật)
        Button saveBtn = new Button("Lưu thay đổi");
        saveBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color: #e6e6e6; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;"));

        // Logic Lưu
        saveBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                nameField.setStyle("-fx-border-color: #ff5555; -fx-border-radius: 5; -fx-text-fill: white; -fx-background-color: #3e3e3e;");
                return;
            }
            
            p.setName(newName);
            p.setPrivacy(privacyBox.getValue());
            p.setDescription(descArea.getText().trim());
            
            UserManager.getInstance().saveToJSON();
            PlaylistLibrary.getInstance().saveToJSON();
            
            editStage.close();
            hienThiChiTietPlaylist(p); // Refresh màn hình chi tiết
            refreshPlaylistSidebar();  // Refresh sidebar
            showAlert("Thành công", "Đã cập nhật thông tin playlist!");
        });

        // Layout Hàng Nút
        HBox btnBox = new HBox(15);
        btnBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        // Spacer để đẩy nút Xóa sang trái, các nút khác sang phải
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        btnBox.getChildren().addAll(deleteBtn, spacer, cancelBtn, saveBtn);

        // 3. Hoàn thiện Scene
        root.getChildren().addAll(header, formLayout, btnBox);
        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Nền trong suốt
        editStage.setScene(scene);
        editStage.show();
    }

    // [HÀM MỚI] Tạo thanh trượt ngang (Giống Music Card)
    private ScrollPane createHorizontalSlider(HBox contentBox) {
        ScrollPane scroller = new ScrollPane(contentBox);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Tắt thanh cuộn dọc
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Thanh ngang hiện khi cần
        scroller.setFitToHeight(true); // Co giãn theo chiều cao
        scroller.setPannable(true);    // Cho phép dùng chuột kéo
        
        // Style cho trong suốt để không bị viền trắng xấu
        scroller.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroller.getStyleClass().add("horizontal-scroll"); // Nếu có CSS custom
        
        return scroller;
    }

    private void styleInput(TextField tf) {
        // Tăng font size lên 14px, Chữ trắng tinh
        tf.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5; -fx-padding: 10;");
        tf.setOnMouseEntered(e -> tf.setStyle("-fx-background-color: #4d4d4d; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5; -fx-padding: 10;"));
        tf.setOnMouseExited(e -> tf.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5; -fx-padding: 10;"));
    }
    
    private void styleTextArea(TextArea ta) {
        // Chữ trắng, font to hơn chút
        ta.setStyle("-fx-control-inner-background: #3e3e3e; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-color: #3e3e3e; -fx-background-radius: 5;");
    }
    
    private void styleComboBox(ComboBox<String> cb) {
        // 1. Style khung ngoài + Mũi tên màu trắng (-fx-mark-color)
        cb.setStyle("-fx-background-color: #3e3e3e; -fx-font-size: 14px; -fx-background-radius: 5; -fx-mark-color: white;");
        
        // 2. [FIX QUAN TRỌNG] Ép dòng chữ hiển thị bên trong thành MÀU TRẮNG
        cb.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(javafx.scene.paint.Color.WHITE); // Chữ trắng
                    setStyle("-fx-background-color: transparent;"); // Nền trong suốt để ăn theo nền cha
                }
            }
        });

        // 3. (Tùy chọn) Style cho danh sách xổ xuống để đồng bộ Dark mode
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #3e3e3e;");
                } else {
                    setText(item);
                    setTextFill(javafx.scene.paint.Color.WHITE);
                    // Hack nhỏ: Set style tĩnh, hover có thể không mượt bằng CSS file nhưng đủ dùng
                    if (isSelected()) {
                        setStyle("-fx-background-color: #555555; -fx-text-fill: #1DB954;"); // Màu xanh Spotify khi chọn
                    } else {
                        setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white;");
                    }
                }
            }
        });
    }

    // [HÀM MỚI] Style dành riêng cho ComboBox chứa Playlist (Tránh trùng tên)
    private void stylePlaylistComboBox(ComboBox<Playlist> cb) {
        cb.setStyle("-fx-background-color: #3e3e3e; -fx-font-size: 14px; -fx-background-radius: 5; -fx-mark-color: white;");
        
        // Hiển thị tên Playlist (Button)
        cb.setButtonCell(new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(cb.getPromptText());
                    setTextFill(javafx.scene.paint.Color.WHITE);
                } else {
                    setText(item.getTitle());
                    setTextFill(javafx.scene.paint.Color.WHITE);
                }
                setStyle("-fx-background-color: transparent;");
            }
        });

        // Hiển thị danh sách Playlist (Dropdown)
        cb.setCellFactory(lv -> new ListCell<Playlist>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #3e3e3e;");
                } else {
                    setText(item.getTitle());
                    setTextFill(javafx.scene.paint.Color.WHITE);
                    if (isSelected()) {
                        setStyle("-fx-background-color: #555555; -fx-text-fill: #1DB954;"); 
                    } else {
                        setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white;");
                    }
                }
            }
        });
    }

    // [HÀM MỚI] Xử lý khi bấm vào thanh Bottom Bar
    private void handleBottomBarClick() {
        if (isPlayerMode) {
            // TRƯỜNG HỢP 1: Đang ở Player -> Thu xuống (Khôi phục màn hình cũ)
            if (manHinhTruocKhiVaoPlayer != null) {
                mainRoot.setCenter(manHinhTruocKhiVaoPlayer);
                isPlayerMode = false;
                
                // Mẹo: Nếu muốn chắc ăn thì update lại giao diện sidebar (VD highlight nút Home)
                // nhưng cơ bản setCenter là đủ.
            } else {
                // Phòng hờ nếu null thì về Home
                hienThiManHinhHome(); 
            }
        } else {
            // TRƯỜNG HỢP 2: Đang ở màn hình khác -> Mở Player
            
            // 1. [QUAN TRỌNG] Lưu màn hình hiện tại lại trước khi bị đè
            manHinhTruocKhiVaoPlayer = mainRoot.getCenter();
            
            // 2. Mở Player
            hienThiManHinhPlayer();
            // (Trong hàm hienThiManHinhPlayer ông nhớ vẫn giữ dòng isPlayerMode = true nhé)
        }
    }

    // --- [HÀM MỚI] Tạo hàng ngang chứa Playlist (Giống hàng bài hát nhưng cho Playlist) ---
    private VBox taoHangNgangPlaylist(String tieuDe, List<Playlist> playlists) {
        VBox sectionBox = new VBox();
        sectionBox.setSpacing(15);
        Label lblTitle = new Label(tieuDe);
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        HBox cardRow = new HBox();
        cardRow.setSpacing(20);
        cardRow.setPadding(new Insets(5));
        
        for (Playlist p : playlists) {
            Node card = taoThePlaylistHome(p); 
            if (card != null) cardRow.getChildren().add(card);
        }
        
        ScrollPane rowScroller = new ScrollPane(cardRow);
        rowScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
        rowScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 
        rowScroller.setFitToHeight(true); 
        rowScroller.setPannable(true);    
        rowScroller.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        rowScroller.getStyleClass().add("horizontal-scroll"); // Ăn theo CSS cũ
        
        sectionBox.getChildren().addAll(lblTitle, rowScroller);
        return sectionBox;
    }

    // [HÀM MỚI] Tạo Card Playlist nhỏ cho màn hình Home
    private Node taoThePlaylistHome(Playlist p) {
        VBox card = new VBox(8);
        card.setPrefSize(150, 200);
        card.setStyle("-fx-background-color: #282828; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
        
        ImageView img = new ImageView();
        try {
            String imgPath = "icons/logo.png"; // Mặc định
            String title = p.getTitle().toLowerCase();
            // Logic chọn ảnh y hệt PlaylistView
            if (title.contains("yêu thích") || title.contains("favorite")) imgPath = "icons/heart.png";
            else if (title.contains("gần đây") || title.contains("history")) imgPath = "icons/history.png";
            else if (title.contains("top 100") || title.contains("bxh")) imgPath = "icons/trending.png";
            
            img.setImage(new Image(getClass().getResourceAsStream(imgPath)));
        } catch (Exception e) {}
        img.setFitWidth(130); img.setFitHeight(130);
        
        Label name = new Label(p.getTitle());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        name.setWrapText(true);
        name.setMaxWidth(130);
        
        Label creator = new Label(p.getCreator());
        creator.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12px;");
        
        card.getChildren().addAll(img, name, creator);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #3E3E3E; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #282828; -fx-background-radius: 8; -fx-padding: 10;"));
        
        card.setOnMouseClicked(e -> hienThiChiTietPlaylist(p));
        
        return card;
    }

    // Hàm check công khai/riêng tư tiện lợi
    private boolean isPublicPlaylist(Playlist p) {
        String pr = p.getPrivacy();
        return pr == null || pr.equalsIgnoreCase("Công khai") || pr.equalsIgnoreCase("Public");
    }
}