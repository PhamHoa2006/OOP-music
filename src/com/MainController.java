package com;

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
import javafx.scene.shape.Circle;

public class MainController {

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

}
