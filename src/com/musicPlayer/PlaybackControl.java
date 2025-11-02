    package com.musicPlayer;

    // Lớp PlaybackControl quản lý trạng thái và thao tác điều khiển phát nhạc (âm lượng, tốc độ, shuffle, repeat, mute)

    public class PlaybackControl {

    private int volume;                  // Từ 0 đến 100
    private boolean isMuted;
    private double playbackSpeed;        // x0.5 - x2.0
    private boolean isShuffle;
    private boolean isRepeat;

    public PlaybackControl() {
        resetAll();
    }

    // Cho phép các hàm khác và UI đọc giá trị volume hiện tại
    public int getVolume() {
        return volume;
    }

    // Đặt âm lượng mới từ 0 đến 100, tự động giới hạn
    public int setVolume(int volume) {
        if (volume < 0) {
            this.volume = 0;
        } else if (volume > 100) {
            this.volume = 100;
        } else {
            this.volume = volume;
        }
        return this.volume;
    }

    // Hàm tăng hoặc giảm âm lượng theo giá trị amount, amount có thể âm hoặc dương
    public int changeVolume(int amount) {
        this.volume += amount;
        if (this.volume > 100) this.volume = 100;
        if (this.volume < 0) this.volume = 0;
        return this.volume;
    }

    // Kiểm tra xem hiện tại có đang tắt tiếng hay không
    public boolean isMuted() {
        return isMuted;
    }

    // Cập nhật trạng thái tắt tiếng theo giá trị truyền vào (true = mute, false = unmute)
    public boolean setMuted(boolean muted) {
        this.isMuted = muted;
        return this.isMuted;
    }

    // Đảo trạng thái mute
    public boolean toggleMute() {
        isMuted = !isMuted;
        return isMuted;
    }

    // Tốc độ phát

    // Lấy tốc độ phát hiện tại
    public double getPlaybackSpeed() {
        return playbackSpeed;
    }

    // Đặt tốc độ phát trong khoảng 0.5x – 2.0x, tự động giới hạn nếu vượt ngoài biên
    public double setSpeed(double speed) {
        if (speed < 0.5) {
            this.playbackSpeed = 0.5;
        } else if (speed > 2.0) {
            this.playbackSpeed = 2.0;
        } else {
            this.playbackSpeed = speed;
        }
        return this.playbackSpeed;
    }

    // Chọn tốc độ phát từ danh sách preset (ví dụ: x0.5, x1.25, x2), thay vì nhập số trực tiếp
    public double setSpeedPreset(String preset) {
        switch (preset) {
            case "x0.5": return setSpeed(0.5);
            case "x0.75": return setSpeed(0.75);
            case "x1": return setSpeed(1.0);
            case "x1.25": return setSpeed(1.25);
            case "x1.5": return setSpeed(1.5);
            case "x1.75": return setSpeed(1.75);
            case "x2": return setSpeed(2.0);
            default: return setSpeed(1.0);
        }
    }

    // Đặt lại tốc độ phát về mặc định (1.0x)
    public double resetSpeed() {
        return setSpeed(1.0);
    }

    // Phát ngẫu nhiên

    // Kiểm tra trạng thái phát ngẫu nhiên
    public boolean isShuffle() {
        return isShuffle;
    }

    // Cập nhật trạng thái phát ngẫu nhiên theo giá trị truyền vào
    public boolean setShuffle(boolean shuffle) {
        this.isShuffle = shuffle;
        return this.isShuffle;
    }

    // Đảo trạng thái phát ngẫu nhiên
    public boolean toggleShuffle() {
        isShuffle = !isShuffle;
        return isShuffle;
    }

    // Phát lại

    // Kiểm tra xem có đang bật chế độ lặp lại hay không
    public boolean isRepeat() {
        return isRepeat;
    }

    // Ép trạng thái repeat theo giá trị truyền vào
    public boolean setRepeat(boolean repeat) {
        this.isRepeat = repeat;
        return this.isRepeat;
    }

    // Đảo trạng thái Repeat
    public boolean toggleRepeat() {
        isRepeat = !isRepeat;
        return isRepeat;
    }

    // Hàm tiện ích

    // Đưa toàn bộ cài đặt (âm lượng, tốc độ, phát ngẫu nhiên, lặp lại, tắt tiếng) về mặc định
    public void resetAll() {
        this.volume = 50;
        this.isMuted = false;
        this.playbackSpeed = 1.0;
        this.isShuffle = false;
        this.isRepeat = false;
    }

    // Trả về chuỗi mô tả đầy đủ trạng thái hiện tại của PlaybackControl
    @Override
    public String toString() {
        return String.format(
                "PlaybackControl[volume=%d, speed=%.1fx, shuffle=%s, repeat=%s, muted=%s]",
                volume, playbackSpeed, isShuffle, isRepeat, isMuted
        );
    }
}
