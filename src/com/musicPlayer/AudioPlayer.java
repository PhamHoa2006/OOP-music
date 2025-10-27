package com.musicPlayer;

public class AudioPlayer {
    package com.musicplayer;

public class AudioPlayer implements Player {
    private Playlist playlist;
    private int currentIndex = 0;
    private double volume = 1.0;
    private boolean playing = false;
    private boolean paused = false;
    private Runnable onSongEnd; // interface co san trong java.lang -> callback
    private double currentTime = 0.0;
    private double duration = 363.6; // mac dinh la 6 phut 36s;

    public AudioPlayer() {
        this.playlist = null;
    }

    public AudioPlayer(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public void play() {
        if (!playlist.getSongs().isEmpty()) {
            Song s = playlist.getSongs().get(currentIndex);
            playing = true;
            paused = false;
            System.out.println("▶ Playing: " + s);
        }
    }

    @Override
    public void pause() {
        if (playing) {
            paused = true;
            playing = false;
            System.out.println("⏸Paused.");
        }
    }

    @Override
    public void stop() {
        if (playing || paused) {
            playing = false;
            paused = false;
            System.out.println("⏹ Stopped.");
        }
    }

    @Override
    public void next() {
        if (playlist == null)
            return;
        if (!playlist.getSongs().isEmpty() && playlist != null) {
            currentIndex = (currentIndex + 1) % playlist.getSongs().size();
            System.out.println("⏭ Next song.");
            play();
        }
    }

    @Override
    public void previous() {
        if (!playlist.getSongs().isEmpty()) {
            currentIndex = (currentIndex - 1 + playlist.getSongs().size()) % playlist.getSongs().size();
            System.out.println("⏮ Previous song.");
            play();
        }
    }

    @Override
    public void seekForward(int seconds) {
        currentTime += seconds;
        if (currentTime > duration) {
            currentTime = duration;
            if (onSongEnd != null)
                onSongEnd.run(); // callback khi hết bài
        }
        System.out.println("⏩ Seeked forward " + seconds + "s. Current: " + currentTime + " / " + duration);
    }

    @Override
    public void seekBackward(int seconds) {
        currentTime -= seconds;
        if (currentTime < 0)
            currentTime = 0;
        System.out.println("⏪ Seeked backward " + seconds + "s. Current: " + currentTime + " / " + duration);
    }

    @Override
    public void seek(int seconds) {
        currentTime = seconds;
        if (currentTime > duration)
            currentTime = duration;
        if (currentTime < 0)
            currentTime = 0;
        System.out.println("Current: " + currentTime + " / " + duration);
    }

    @Override
    public void setVolume(double volume) {
        if (volume < 0.0)
            volume = 0.0;
        if (volume > 1.0)
            volume = 1.0;
        this.volume = volume;
        System.out.println("🔊 Volume set to: " + (int) (volume * 100) + "%");
    }

    @Override
    public double getVolume() {
        return this.volume;
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
        return this.currentTime;
    }

    // @Override
    // public double getDuration() {
    // return this.duration;
    // }

    @Override
    public void setOnSongEnd(Runnable callback) {
        this.onSongEnd = callback;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.currentIndex = 0;
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }
}
}
