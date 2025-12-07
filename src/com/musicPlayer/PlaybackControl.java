package com.musicPlayer;

// Lớp PlaybackControl quản lý trạng thái và thao tác điều khiển phát nhạc (âm lượng, tốc độ, shuffle, repeat, mute)

public class PlaybackControl {

    // Tham chiếu đến Player
    private Player targetPlayer; // Tham chiếu đến trình phát nhạc thực tế

    private int volume;                  // Từ 0 đến 100
    private boolean isMuted;
    private double playbackSpeed;        // x0.5 - x2.0
    private boolean isShuffle;
    private boolean isRepeat;

    // Constructor không tham số
    public PlaybackControl() {
        this.targetPlayer = null;
        resetAll();
    }

    // Constructor nhận Player
    public PlaybackControl(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
        resetAll();
    }

    // Hàm set Player dự phòng
    public void setTargetPlayer(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
        // Đồng bộ trạng thái hiện tại với Player mới
        syncToPlayer();
    }

    // Đồng bộ toàn bộ trạng thái với Player
    private void syncToPlayer() {
        if (targetPlayer != null) {
            targetPlayer.setVolume(this.volume / 100.0);
            // Các phương thức khác sẽ được gọi trong các setter tương ứng
        }
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
        
        // **GỌI PLAYER:** Chuyển đổi (0-100) -> (0.0-1.0)
        if (targetPlayer != null && !isMuted) {
            targetPlayer.setVolume(this.volume / 100.0);
        }
        
        return this.volume;
    }

    // Hàm tăng hoặc giảm âm lượng theo giá trị amount
    public int changeVolume(int amount) {
        return setVolume(this.volume + amount); // Dùng logic của setVolume
    }

    // Kiểm tra xem hiện tại có đang tắt tiếng hay không
    public boolean isMuted() {
        return isMuted;
    }

    // Cập nhật trạng thái tắt tiếng
    public boolean setMuted(boolean muted) {
        this.isMuted = muted;
        
        // **GỌI PLAYER:** Điều chỉnh volume thực tế
        if (targetPlayer != null) {
            if (this.isMuted) {
                targetPlayer.setVolume(0.0);
            } else {
                targetPlayer.setVolume(this.volume / 100.0);
            }
        }
        
        return this.isMuted;
    }

    // Đảo trạng thái mute
    public boolean toggleMute() {
        return setMuted(!isMuted); // Dùng logic của setMuted
    }

    // Tốc độ phát

    // Lấy tốc độ phát hiện tại
    public double getPlaybackSpeed() {
        return playbackSpeed;
    }

    // Đặt tốc độ phát trong khoảng x0.5 – x2.0, tự động giới hạn nếu vượt ngoài biên
    public double setSpeed(double speed) {
        if (speed < 0.5) {
            this.playbackSpeed = 0.5;
        } else if (speed > 2.0) {
            this.playbackSpeed = 2.0;
        } else {
            this.playbackSpeed = speed;
        }
        
        // **GỌI PLAYER:**
        if (targetPlayer != null) {
            // targetPlayer.setSpeed(this.playbackSpeed);
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

    // Đặt lại tốc độ phát về mặc định (x1.0)
    public double resetSpeed() {
        return setSpeed(1.0);
    }

    // Phát ngẫu nhiên

    // Kiểm tra trạng thái phát ngẫu nhiên
    public boolean isShuffle() {
        return isShuffle;
    }

    // Cập nhật trạng thái phát ngẫu nhiên
    public boolean setShuffle(boolean shuffle) {
        this.isShuffle = shuffle;
        
        // **GỌI PLAYER:**
        if (targetPlayer != null) {
            // targetPlayer.setShuffle(this.isShuffle);
        }
        
        return this.isShuffle;
    }

    // Đảo trạng thái phát ngẫu nhiên
    public boolean toggleShuffle() {
        return setShuffle(!isShuffle); // Dùng logic của setShuffle
    }

    // Phát lại

    // Kiểm tra xem có đang bật chế độ lặp lại hay không
    public boolean isRepeat() {
        return isRepeat;
    }

    // Ép trạng thái repeat
    public boolean setRepeat(boolean repeat) {
        this.isRepeat = repeat;
        
        // **GỌI PLAYER:**
        if (targetPlayer != null) {
            // targetPlayer.setRepeat(this.isRepeat);
        }
        
        return this.isRepeat;
    }

    // Đảo trạng thái Repeat
    public boolean toggleRepeat() {
        return setRepeat(!isRepeat); // Dùng logic của setRepeat
    }

    // Hàm tiện ích

    // Đưa toàn bộ cài đặt (âm lượng, tốc độ, phát ngẫu nhiên, lặp lại, tắt tiếng) về mặc định
    public void resetAll() {
        this.volume = 50;
        this.isMuted = false;
        this.playbackSpeed = 1.0;
        this.isShuffle = false;
        this.isRepeat = false;
        
        // Đồng bộ với Player nếu có
        if (targetPlayer != null) {
            targetPlayer.setVolume(this.volume / 100.0);
            // Các method khác sẽ được uncomment khi Player interface đầy đủ
        }
    }

    // Trả về chuỗi mô tả đầy đủ trạng thái hiện tại của PlaybackControl
    @Override
    public String toString() {
        return String.format(
                "PlaybackControl[volume=%d, speed=%.1f, shuffle=%s, repeat=%s, muted=%s]",
                volume, playbackSpeed, isShuffle, isRepeat, isMuted
        );
    }
}
