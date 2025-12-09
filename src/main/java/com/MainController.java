package com;

import com.musicPlayer.Playlist;
import com.users.History;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;

public class MainController {

    private MediaPlayer mediaPlayer; // MediaPlayer nhận từ Main
    private History userHistory; // History nhận từ Main
    private Playlist currentPlaylist;
    private Playlist favourite;

    // Setter để Main truyền MediaPlayer vào
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;

        // Khởi tạo volume slider khi có MediaPlayer
        if (volumeSlider != null && mediaPlayer != null) {
            volumeSlider.setMin(0);
            volumeSlider.setMax(100);
            volumeSlider.setValue(mediaPlayer.getVolume() * 100);

            // Khi kéo slider, cập nhật MediaPlayer volume
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (mediaPlayer != null) {
                    double vol = newVal.doubleValue() / 100.0;
                    mediaPlayer.setVolume(vol);
                    updateVolumeLabel(vol);
                }
            });

            updateVolumeLabel(mediaPlayer.getVolume());
        }
    }

    // Setter để Main truyền History vào
    public void setUserHistory(History userHistory) {
        this.userHistory = userHistory;
    }


    // Cập nhật nhãn % âm lượng
    private void updateVolumeLabel(double vol) {
        if (volumeLabel != null) {
            int percent = (int) (vol * 100);
            volumeLabel.setText(percent + "%");
        }
    } // Thêm biến volumeLabel (biến volumaLabel là giá trị âm lượng dùng để trượt trái phải để tăng giảm)


    @FXML
    private Button addToPlaylistBtn;

    @FXML
    private Button backBtn;

    @FXML
    private Label currentArtistLabel;

    @FXML
    private Label currentSongLabel;

    @FXML
    private Label currentTimeLbl;

    @FXML
    private StackPane discContainer;

    @FXML
    private ImageView discIconView;

    @FXML
    private Button favoritesBtn;

    @FXML
    private Button forwardBtn;

    @FXML
    private Button historyBtn;

    @FXML
    private Button homeBtn;

    @FXML
    private Circle innerDiscCircle;

    @FXML
    private Button likeBtn;

    @FXML
    private Button logoBtn;

    @FXML
    private Button lyricsBtn;

    @FXML
    private BorderPane mainRoot;

    @FXML
    private ImageView miniThumbView;

    @FXML
    private Button mutedBtn;

    @FXML
    private Button mvBtn;

    @FXML
    private Button newPlaylistBtn;

    @FXML
    private Button nextButton;

    @FXML
    private ToggleButton nextTabBtn;

    @FXML
    private Circle outerDiscCircle;

    @FXML
    private Button pauseButton;

    @FXML
    private Button playButton;

    @FXML
    private ListView<?> playlistListView;

    @FXML
    private Button prevButton;

    @FXML
    private Slider progressSlider;

    @FXML
    private HBox queueActionsBox;

    @FXML
    private Button queueAddBtn;

    @FXML
    private VBox queueContainerVBox;

    @FXML
    private StackPane queueHeaderStack;

    @FXML
    private HBox queueInfoBox;

    @FXML
    private Button queueLikeBtn;

    @FXML
    private ListView<?> queueListView;

    @FXML
    private Button queueRepeatBtn;

    @FXML
    private Button queueShareBtn;

    @FXML
    private Button queueShuffleBtn;

    @FXML
    private ListView<?> relatedListView;

    @FXML
    private ToggleButton relatedTabBtn;

    @FXML
    private Button repeatButton;

    @FXML
    private Button saveQueueBtn;

    @FXML
    private TextField searchField;

    @FXML
    private Button settingsBtn;

    @FXML
    private Button shareBtn;

    @FXML
    private Button shuffleButton;

    @FXML
    private StackPane sidebarContentStack;

    @FXML
    private Label sourceNameLbl;

    @FXML
    private ToggleGroup tabGroup;

    @FXML
    private Button timerBtn;

    @FXML
    private Button top100Btn;

    @FXML
    private Label totalTimeLbl;

    @FXML
    private Button uploadBtn;

    @FXML
    private Button volumeBtn;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Button volumeLabel; // *****Còn thiếu để bổ sung thêm******

    @FXML // Xử lý tăng âm lượng bằng cách trượt slider
    private void handleVolumeUp() {
        if (mediaPlayer != null) {
            double vol = mediaPlayer.getVolume() + 0.1;
            if (vol > 1) vol = 1;
            mediaPlayer.setVolume(vol);
            volumeSlider.setValue(vol * 100);
            updateVolumeLabel(vol);
        }
    }

    @FXML // Xử lý giảm âm lượng bằng cách trượt slider
    private void handleVolumeDown() {
        if (mediaPlayer != null) {
            double vol = mediaPlayer.getVolume() - 0.1;
            if (vol < 0) vol = 0;
            mediaPlayer.setVolume(vol);
            volumeSlider.setValue(vol * 100);
            updateVolumeLabel(vol);
        }
    }

    /*
    1. Tạo public void setMediaPlayer(MediaPlayer mediaPlayer) để nhận MediaPlayer từ Main.java
    2. Tạo public void setUserHistory(History userHistory) để nhận History
     */
}
