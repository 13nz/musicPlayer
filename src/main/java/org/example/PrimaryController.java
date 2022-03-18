package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.*;

public class PrimaryController implements Initializable {

    @FXML
    private AnchorPane pane;
    @FXML
    private Label songLabel;
    @FXML
    private Button playBtn, pauseBtn, resetBtn, previousBtn, nextBtn, btn;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;

    @FXML
    ToggleButton toggleShuffle;

    private Media media;
    private MediaPlayer player;

    private ArrayList<File> songs = new ArrayList<>();

    private int songNumber;
    private int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};

    private Timer timer;
    private TimerTask task;
    private boolean running;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        for (int speed : speeds) {
            speedBox.getItems().add(speed + "%");
        }

        speedBox.setOnAction(this::changeSpeed);

        volumeSlider.valueProperty().addListener((observableValue, number, t1) -> player.setVolume(volumeSlider.getValue() * 0.01));

        songProgressBar.setStyle("-fx-accent: #9756e1;");

        songProgressBar.setOnMouseClicked(e -> {
            if (player != null) {
                double position = e.getSceneX() - songProgressBar.getLayoutX();
                double size = songProgressBar.getWidth();

                double progress = position / size;

                System.out.println(progress);

                songProgressBar.setProgress(progress);

                Media file = new Media("file://" + songs.get(songNumber).getAbsolutePath().replaceAll(" ","%20"));
                MediaPlayer newPlayer = new MediaPlayer(file);

                newPlayer.setOnReady(() -> {
                    double duration = file.getDuration().toSeconds();

                    System.out.println(duration);

                    double songPosition = duration * progress;

                    System.out.println(songPosition);

                    player.seek(Duration.seconds(songPosition));
                });


            }
        });
    }

    public void playMedia() {
        beginTimer();
        changeSpeed(null);
        player.setVolume(volumeSlider.getValue() * 0.01);
        player.play();
    }

    public void pauseMedia() {
        cancelTimer();
        player.pause();
    }

    public void resetMedia() {
        songProgressBar.setProgress(0);
        player.seek(Duration.seconds(0.0));
    }

    public void previousMedia() {
        if(songNumber > 0) {
            songNumber--;
        } else {
            songNumber = songs.size() - 1;
        }
        player.stop();

        if(running) {
            cancelTimer();
        }

        initPlayer();
    }

    public void nextMedia() {
        if(songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        player.stop();

        if(running) {
            cancelTimer();
        }

        initPlayer();
    }

    public void changeSpeed(ActionEvent event) {
        if (speedBox.getValue() == null) {
            player.setRate(1);
        } else {
            player.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
        }
    }

    public void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                running = true;
                double current = player.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                songProgressBar.setProgress(current / end);

                if(current / end == 1) {
                    cancelTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 100);
    }

    public void cancelTimer() {
        running = false;
        timer.cancel();
    }

    public void initPlayer() {
        media = new Media(songs.get(songNumber).toURI().toString());
        player = new MediaPlayer(media);

        songLabel.setText(songs.get(songNumber).getName());
        playMedia();
    }

    public void selectFiles() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Music Files", "*.mp3", "*.m4a"));

        List<File> selected = chooser.showOpenMultipleDialog(null);

        songs.clear();
        songs.addAll(selected);

        System.out.println(songs.size());
        initSongs();
    }

    public void initSongs() {
        if (player != null) {
            player.stop();
        }
        System.out.println(songs.size());
        if (songs.size() > 0) {
            initPlayer();
        }
    }

    public void shuffle() {
        Collections.shuffle(songs);
        initSongs();
    }


}
