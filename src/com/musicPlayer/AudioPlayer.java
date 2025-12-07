package com.musicPlayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class AudioPlayer implements Player {
    private Playlist playlist;
    private int currentIndex = 0;
    private volatile int volume = 100;
    private volatile boolean playing = false;
    private volatile boolean paused = false;
    private volatile boolean timerRunning = false;
    private Runnable onSongEnd; // interface co san trong java.lang -> callback
    private MediaPlayer mediaPlayer;

    // volatile giup dong bo giua cac Thread
    // synchronized dam bao 1 va chi 1 thread duoc chay tai 1 thoi diem
    public AudioPlayer() {
        this.playlist = null;
    }

    public AudioPlayer(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public synchronized void play() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return;

        Song s = playlist.getSongs().get(currentIndex);
        if (s == null)
            return;
        if (mediaPlayer == null || !isCurrentMediaForSong(s)) {
            createMediaPlayerForSong(s);
        }
        mediaPlayer.play();
        mediaPlayer.setVolume(volume);
        playing = true;
        paused = false;
        // startTimer();
    }

    @Override
    public synchronized void pause() {
        if (mediaPlayer != null && playing) {
            mediaPlayer.pause();
            paused = true;
            playing = false;
        }
    }

    @Override
    public synchronized void stop() {
        if (mediaPlayer != null && (playing || paused)) {
            mediaPlayer.stop();
            playing = false;
            paused = false;
            mediaPlayer.seek(Duration.ZERO);
        }
    }

    @Override
    public synchronized String getStatus() {
        if (isPlaying())
            return "Playing";
        else if (isPaused())
            return "Paused";
        else
            return "Stopped";
    }

    @Override
    public synchronized void next() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return;
        else {
            currentIndex = (currentIndex + 1) % playlist.getSongs().size();
            switchToCurrentSongAndPlay();
        }
    }

    @Override
    public synchronized void previous() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return;
        else {
            currentIndex = (currentIndex - 1 + playlist.getSongs().size()) % playlist.getSongs().size();
            switchToCurrentSongAndPlay();
        }
    }

    @Override
    public synchronized void seekForward(int seconds) {
        if (mediaPlayer == null)
            return;
        Duration current = mediaPlayer.getCurrentTime();
        Duration total = mediaPlayer.getTotalDuration();
        Duration target = current.add(Duration.seconds(seconds));
        if (total != null && !total.isUnknown() && target.greaterThan(total)) {
            target = total;
        }
        mediaPlayer.seek(target);
    }

    @Override
    public synchronized void seekBackward(int seconds) {
        if (mediaPlayer == null)
            return;
        Duration current = mediaPlayer.getCurrentTime();
        Duration target = current.subtract(Duration.seconds(seconds));
        if (target.lessThan(Duration.ZERO)) {
            target = Duration.ZERO;
        }
        mediaPlayer.seek(target);
    }

    @Override
    public synchronized void seek(int seconds) {
        if (mediaPlayer == null)
            return;
        Duration total = mediaPlayer.getTotalDuration();
        Duration target = Duration.seconds(seconds);
        if (total != null && !total.isUnknown() && target.greaterThan(total)) {
            target = total;
        } else if (target.lessThan(Duration.ZERO)) {
            target = Duration.ZERO;
        }
        mediaPlayer.seek(target);

    }

    @Override
    public synchronized double getVolume() {
        return this.volume;
    }

    @Override
    public synchronized void setVolume(int volume) {
        if (volume < 0) {
            this.volume = 0;
        } else if (volume > 100) {
            this.volume = 100;
        } else {
            this.volume = volume;
        }
    }

    @Override
    public boolean isPlaying() {
        return this.playing;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public boolean isStopped() {
        return !this.paused && !this.playing;
    }

    @Override
    public Song getCurrentSong() {
        if (playlist == null || playlist.getSongs().isEmpty()) {
            return null;
        }
        return playlist.getSongs().get(currentIndex);
    }

    @Override
    public double getCurrentTime() {
        if (mediaPlayer == null)
            return 0;
        Duration t = mediaPlayer.getCurrentTime();
        if (t == null || t.isUnknown())
            return 0;
        return t.toSeconds();
    }

    @Override
    public void setOnSongEnd(Runnable callback) {
        this.onSongEnd = callback;
    }

    private void createMediaPlayerForSong(Song s) {
        disposeMediaPlayer(); // giai phong mediaPlayer cu

        if (s == null || s.getUrl() == null)
            return;
        try {
            File file = new File(s.getUrl());
            if (!file.exists()) {
                System.out.println("File not found: " + s.getUrl());
                return;
            }
            String uriString = file.toURI().toString();
            Media media = new Media(uriString);
            mediaPlayer = new MediaPlayer(media);

            // set callback
            mediaPlayer.setOnEndOfMedia(() -> {
                playing = false; // trang thai kh con phat
                paused = false; // trang thai kh tam dung nua
                if (onSongEnd != null) {
                    try {
                        onSongEnd.run();
                    } catch (Exception ex) { // neu callback loi -> van chay
                        ex.printStackTrace(); // stack trace, in ra vi tri loi
                    }
                }
                next();// tu dong chuyen bai
            });
            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer error: " + mediaPlayer.getError());
            });
            mediaPlayer.setVolume(volume);
        } catch (Exception ex) {
            System.err.println("Failed to create MediaPlayer for: " + s.getUrl());
            ex.printStackTrace();
        }
    }

    private synchronized void switchToCurrentSongAndPlay() {
        Song s = getCurrentSong();
        if (s == null)
            return;
        createMediaPlayerForSong(s);
        play();
    }

    // kiem tra xem mediaPlayer co dang phat Song s khong
    private boolean isCurrentMediaForSong(Song s) {
        if (mediaPlayer == null)
            return false;
        Media m = mediaPlayer.getMedia(); // lay doi tuong hien tai ma mediaPlayer dang phat
        if (m == null)
            return false;
        String mediaSource = m.getSource();
        String songUrl = new File(s.getUrl()).toURI().toString();
        return songUrl != null && songUrl.equals(mediaSource);
    }

    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.currentIndex = 0;
        if (playlist != null && !playlist.getSongs().isEmpty()) {
            createMediaPlayerForSong(playlist.getSongs().get(0));
        }
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }
}
