package com.musicPlayer;

public class PlaybackControl {

    private int volume = 50;                  // 0-100
    private boolean isMuted = false;
    private double playbackSpeed = 1.0;       // 0.5x - 2.0x
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    public PlaybackControl() {
        this.volume = 50;
        this.isMuted = false;
        this.playbackSpeed = 1.0;
        this.isShuffle = false;
        this.isRepeat = false;
    }

    // ==================== VOLUME ====================
    
    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        if (volume < 0) {
            this.volume = 0;
        } else if (volume > 100) {
            this.volume = 100;
        } else {
            this.volume = volume;
        }
        System.out.println("Volume: " + this.volume);
    }

    public void increaseVolume() {
        if (volume < 100) {
            volume += 5;
        }
        System.out.println("Volume: " + volume);
    }

    public void increaseVolume(int amount) {
        volume += amount;
        if (volume > 100) {
            volume = 100;
        }
        System.out.println("Volume: " + volume);
    }

    public void decreaseVolume() {
        if (volume > 0) {
            volume -= 5;
        }
        System.out.println("Volume: " + volume);
    }

    public void decreaseVolume(int amount) {
        volume -= amount;
        if (volume < 0) {
            volume = 0;
        }
        System.out.println("Volume: " + volume);
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
        System.out.println(isMuted ? "Muted" : "Unmuted");
    }

    public void toggleMute() {
        isMuted = !isMuted;
        System.out.println(isMuted ? "Muted" : "Unmuted");
    }

    // ==================== SPEED ====================
    
    public double getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void changeSpeed(double speed) {
        if (speed < 0.5) {
            this.playbackSpeed = 0.5;
        } else if (speed > 2.0) {
            this.playbackSpeed = 2.0;
        } else {
            this.playbackSpeed = speed;
        }
        System.out.println("Speed: " + this.playbackSpeed + "x");
    }

    public void setSpeedPreset(String preset) {
        switch (preset) {
            case "0.5x":
                changeSpeed(0.5);
                break;
            case "1x":
                changeSpeed(1.0);
                break;
            case "1.25x":
                changeSpeed(1.25);
                break;
            case "1.5x":
                changeSpeed(1.5);
                break;
            case "2x":
                changeSpeed(2.0);
                break;
            default:
                changeSpeed(1.0);
        }
    }

    public void resetSpeed() {
        changeSpeed(1.0);
    }

    // ==================== SHUFFLE ====================
    
    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.isShuffle = shuffle;
        System.out.println("Shuffle: " + isShuffle);
    }

    public void toggleShuffle() {
        isShuffle = !isShuffle;
        System.out.println("Shuffle: " + isShuffle);
    }

    // ==================== REPEAT ====================
    
    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        this.isRepeat = repeat;
        System.out.println("Repeat: " + isRepeat);
    }

    public void toggleRepeat() {
        isRepeat = !isRepeat;
        System.out.println("Repeat: " + isRepeat);
    }

    // ==================== UTILITY ====================
    
    public void resetAll() {
        this.volume = 50;
        this.isMuted = false;
        this.playbackSpeed = 1.0;
        this.isShuffle = false;
        this.isRepeat = false;
        System.out.println("All settings reset to default");
    }

    public void printSettings() {
        System.out.println("========== THIẾT LẬP PHÁT NHẠC ==========");
        System.out.println("Volume: " + volume + (isMuted ? " (MUTED)" : ""));
        System.out.println("Speed: " + playbackSpeed + "x");
        System.out.println("Shuffle: " + isShuffle);
        System.out.println("Repeat: " + isRepeat);
        System.out.println("=========================================");
    }

    @Override
    public String toString() {
        return String.format("PlaybackControl[volume=%d, speed=%.1fx, shuffle=%s, repeat=%s, muted=%s]",
            volume, playbackSpeed, isShuffle, isRepeat, isMuted);
    }
}
