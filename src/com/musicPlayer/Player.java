package com.musicplayer;

public interface Player {
    void play(); // bat dau hoac tiep tuc

    void pause(); // tam dung

    void stop(); // dung hoan toan

    void next(); // bai tiep theo

    void previous(); // bai truoc

    void seekForward(int seconds); // tua toi

    void seekBackward(int seconds); // tua lui

    void seek(int seconds); // tua cu the

    Song getCurrentSong();

    double getCurrentTime();

    // double getDuration();

    void setVolume(double volume);

    double getVolume();

    boolean isPlaying();

    boolean isStopped();

    boolean isPaused();

    void setOnSongEnd(Runnable callback);
}
