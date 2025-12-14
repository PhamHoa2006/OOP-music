package com.musicPlayer;

public class AudioPlayer implements Player {
    private Playlist playlist;
    private int currentIndex = 0;
    private volatile double volume = 1.0;
    private volatile boolean playing = false;
    private volatile boolean paused = false;
    private volatile boolean timerRunning = false;
    private Runnable onSongEnd; // interface co san trong java.lang -> callback
    private double currentTime = 0.0;
    private Thread timerThread;

    // volatile giup dong bo giua cac Thread

    public AudioPlayer() {
        this.playlist = null;
    }

    public AudioPlayer(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public void play() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return;
        else {
            Song s = playlist.getSongs().get(currentIndex);
            playing = true;
            paused = false;
            // System.out.println("▶ Playing: " + s);
            startTimer();
        }
    }

    @Override
    public void pause() {
        if (playing) {
            paused = true;
            playing = false;
            // System.out.println("⏸Paused.");
        }
    }

    @Override
    public void stop() {
        if (playing || paused) {
            playing = false;
            paused = false;
            // System.out.println("⏹ Stopped.");
            currentTime = 0;
        }
    }

    @Override
    public void next() {
        if (playlist == null || playlist.getSongs().isEmpty())
            return;
        // if (!playlist.getSongs().isEmpty() && playlist != null) {
        else {
            currentIndex = (currentIndex + 1) % playlist.getSongs().size();
            // System.out.println("⏭ Next song.");
            currentTime = 0;
            play();
        }
    }

    @Override
    public void previous() {
        if (playlist.getSongs().isEmpty() || playlist == null)
            return;
        // if (!playlist.getSongs().isEmpty()) {
        else {
            currentIndex = (currentIndex - 1 + playlist.getSongs().size()) % playlist.getSongs().size();
            currentTime = 0;
            // System.out.println("⏮ Previous song.");
            play();
        }
    }

    @Override
    public void seekForward(int seconds) {
        currentTime += seconds;
        Song s = playlist.getSongs().get(currentIndex);
        if (s == null)
            return;
        double duration = s.getDuration();
        if (currentTime > duration) {
            currentTime = duration;
            if (onSongEnd != null)
                onSongEnd.run(); // callback khi hết bài
        }
        // System.out.println("⏩ Seeked forward " + seconds + "s. Current: " +
        // currentTime + " / " + duration);
    }

    @Override
    public void seekBackward(int seconds) {
        Song s = playlist.getSongs().get(currentIndex);
        if (s == null)
            return;
        double duration = s.getDuration();
        currentTime -= seconds;
        if (currentTime < 0)
            currentTime = 0;
        // System.out.println("⏪ Seeked backward " + seconds + "s. Current: " +
        // currentTime + " / " + duration);
    }

    @Override
    public void seek(int seconds) {
        currentTime = seconds;
        Song s = playlist.getSongs().get(currentIndex);
        if (s == null)
            return;
        double duration = s.getDuration();
        if (currentTime > duration)
            currentTime = duration;
        if (currentTime < 0)
            currentTime = 0;
        // System.out.println("Current: " + currentTime + " / " + duration);
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

    @Override
    public void setOnSongEnd(Runnable callback) {
        this.onSongEnd = callback;
    }

    private void startTimer() {
        if (timerRunning)
            return;
        timerRunning = true;
        timerThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();
            while (timerRunning) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                } // sleep de tranh luong chay lien tuc -> dung 200 mls
                if (playing && !paused) {
                    long now = System.currentTimeMillis();
                    double deltaSec = (now - lastTime) / 1000.0;
                    lastTime = now;
                    currentTime += deltaSec;

                    Song s = getCurrentSong();
                    if (s != null && currentTime >= s.getDuration()) {
                        currentTime = s.getDuration();
                        finishSong();
                    }
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    private void finishSong() {
        playing = false;
        if (onSongEnd != null)
            onSongEnd.run();
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.currentIndex = 0;
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }
}
