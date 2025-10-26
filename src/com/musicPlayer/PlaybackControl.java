package musicplayer;

public class PlaybackControl {

    private double volume;      // 0.0 - 1.0
    private double speed;       // 0.5x - 2.0x
    private boolean shuffle;
    private String repeatMode;  // "OFF", "ALL", "ONE"

    public PlaybackControl() {
        this.volume = 1.0;
        this.speed = 1.0;
        this.shuffle = false;
        this.repeatMode = "OFF";
    }

    // ==================== VOLUME ====================
    
    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        if (volume < 0.0) {
            this.volume = 0.0;
        } else if (volume > 1.0) {
            this.volume = 1.0;
        } else {
            this.volume = volume;
        }
    }

    public void volumeUp(double amount) {
        setVolume(this.volume + amount);
    }

    public void volumeUp() {
        volumeUp(0.1);
    }

    public void volumeDown(double amount) {
        setVolume(this.volume - amount);
    }

    public void volumeDown() {
        volumeDown(0.1);
    }

    public int getVolumePercent() {
        return (int) Math.round(volume * 100);
    }

    public void mute() {
        this.volume = 0.0;
    }

    // ==================== SPEED ====================
    
    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0.5) {
            this.speed = 0.5;
        } else if (speed > 2.0) {
            this.speed = 2.0;
        } else {
            this.speed = speed;
        }
    }

    public void resetSpeed() {
        this.speed = 1.0;
    }

    // ==================== SHUFFLE ====================
    
    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void toggleShuffle() {
        this.shuffle = !this.shuffle;
    }

    // ==================== REPEAT ====================
    
    public String getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(String mode) {
        if (mode.equals("OFF") || mode.equals("ALL") || mode.equals("ONE")) {
            this.repeatMode = mode;
        }
    }

    public void toggleRepeatMode() {
        switch (repeatMode) {
            case "OFF":
                repeatMode = "ALL";
                break;
            case "ALL":
                repeatMode = "ONE";
                break;
            case "ONE":
                repeatMode = "OFF";
                break;
        }
    }

    public String getRepeatModeText() {
        switch (repeatMode) {
            case "OFF":
                return "Không lặp";
            case "ALL":
                return "Lặp tất cả";
            case "ONE":
                return "Lặp 1 bài";
            default:
                return "Không xác định";
        }
    }

    // ==================== UTILITY ====================
    
    public void resetAll() {
        this.volume = 1.0;
        this.speed = 1.0;
        this.shuffle = false;
        this.repeatMode = "OFF";
    }

    public void printSettings() {
        System.out.println("========== THIẾT LẬP PHÁT NHẠC ==========");
        System.out.println("Âm lượng: " + getVolumePercent() + "%");
        System.out.println("Tốc độ: " + speed + "x");
        System.out.println("Shuffle: " + (shuffle ? "BẬT" : "TẮT"));
        System.out.println("Repeat: " + getRepeatModeText());
        System.out.println("=========================================");
    }

    @Override
    public String toString() {
        return String.format("PlaybackControl[volume=%.0f%%, speed=%.1fx, shuffle=%s, repeat=%s]",
            volume * 100, speed, shuffle ? "ON" : "OFF", repeatMode);
    }
}
