package com.musicPlayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioPlayer implements Player {
    private Playlist playlist;
    private int currentIndex = 0;

    // MediaPlayer thực tế của JavaFX
    private MediaPlayer mediaPlayer;

    private double volume = 1.0;
    private boolean playing = false;
    private boolean paused = false;

    // Callback để giao diện biết khi nào bài hát kết thúc hoặc thay đổi
    private Runnable onSongEnd;

    public AudioPlayer() {
        this.playlist = null;
    }

    public AudioPlayer(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public void play() {
        // 1. Kiểm tra danh sách : rỗng -> không làm gì
        if (playlist == null || playlist.getSongs().isEmpty())
            return;

        // 2. Nếu đang pause và player vẫn còn đó -> Resume lại
        if (paused && mediaPlayer != null) {
            mediaPlayer.play();
            playing = true;
            paused = false;
            return;
        }

        // 3. Nếu là play mới hoàn toàn (hoặc chuyển bài)
        stop(); // Dọn dẹp player cũ trước

        Song currentSong = playlist.getSongs().get(currentIndex);

        // Lấy đường dẫn URI chuẩn từ hàm ông đã viết trong Song.java
        String source = currentSong.getPlayableUrl();

        if (source == null) {
            System.err.println("Không tìm thấy file nhạc: " + currentSong.getTitle());
            return;
        }

        try {
            Media media = new Media(source);
            mediaPlayer = new MediaPlayer(media);

            // Set volume hiện tại
            mediaPlayer.setVolume(volume);

            // Xử lý sự kiện: Khi bài hát chạy xong
            mediaPlayer.setOnEndOfMedia(() -> {
                // Tự động next bài
                // next(); // Không nên để dòng này để chạy tính năng Repeat.

                // Gọi callback nếu bên ngoài cần biết
                if (onSongEnd != null) {
                    onSongEnd.run();
                }
            });

            mediaPlayer.play();
            playing = true;
            paused = false;

            System.out.println("▶ Playing: " + currentSong.getTitle());

        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo Media: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null && playing) {
            mediaPlayer.pause();
            playing = false;
            paused = true;
            System.out.println("⏸ Paused.");
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose(); // Quan trọng: Giải phóng tài nguyên hệ thống
            mediaPlayer = null;
        }
        playing = false;
        paused = false;
        // Reset về đầu bài hiện tại (nếu muốn) nhưng không reset currentIndex
    }

    @Override
    public void next() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return;

        // Logic xoay vòng index
        currentIndex = (currentIndex + 1) % playlist.getSongs().size();

        // Stop bài cũ và Play bài mới
        // Lưu ý: play() đã có logic gọi stop() ở đầu nên gọi thẳng play() cũng được,
        // nhưng gọi stop() ở đây cho rõ ràng.
        stop();
        play();
    }

    @Override
    public void previous() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return;

        // Logic xoay vòng lùi
        currentIndex = (currentIndex - 1 + playlist.getSongs().size()) % playlist.getSongs().size();
    }

    @Override
    public void seekForward(int seconds) {
        if (mediaPlayer != null) {
            seek((int) mediaPlayer.getCurrentTime().toSeconds() + seconds);
        }
    }

    @Override
    public void seekBackward(int seconds) {
        if (mediaPlayer != null) {
            seek((int) mediaPlayer.getCurrentTime().toSeconds() - seconds);
        }
    }

    @Override
    public void seek(int seconds) {
        if (mediaPlayer != null) {
            // MediaPlayer dùng Duration, không dùng int giây thuần
            mediaPlayer.seek(Duration.seconds(seconds));
        }
    }

    @Override
    public Song getCurrentSong() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return null;
        return playlist.getSongs().get(currentIndex);
    }

    @Override
    public double getCurrentTime() {
        // Lấy thời gian thực từ MediaPlayer
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime().toSeconds();
        }
        return 0.0;
    }

    // Hàm mới bổ sung: Lấy duration thực tế từ file (chính xác hơn Song object lưu)
    public double getTotalDuration() {
        if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
            return mediaPlayer.getTotalDuration().toSeconds();
        }
        return 0.0;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    // Setter volume cập nhật trực tiếp vào player đang chạy
    public void setVolume(double vol) {
        this.volume = vol;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(vol);
        }
    }

    @Override
    public boolean isPlaying() {
        // Có thể check thêm trạng thái của mediaPlayer status
        return playing;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean isStopped() {
        return !playing && !paused;
    }

    @Override
    public void setOnSongEnd(Runnable callback) {
        this.onSongEnd = callback;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.currentIndex = 0;
        stop(); // Reset player khi đổi playlist
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }

    // Getter cho MediaPlayer để Controller có thể bind slider (Thanh chạy)
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }
}