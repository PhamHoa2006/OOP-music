package com.musicPlayer;

public interface Player {
    void play(); // bat dau hoac tiep tuc

    void pause(); // tam dung

    void stop(); // dung hoan toan

    String getStatus();

    void next(); // bai tiep theo

    void previous(); // bai truoc

    void seekForward(int seconds); // tua toi

    void seekBackward(int seconds); // tua lui

    void seek(int seconds); // tua cu the

    Song getCurrentSong();

    double getCurrentTime();

    double getVolume(); // lay am thanh

    void setVolume(int volume);

    boolean isPlaying(); // check tt

    boolean isStopped(); // check tt

    boolean isPaused(); // check trang thai

    // void setMute();

    // void setShuffle();

    // void setRepeat();

    void setOnSongEnd(Runnable callback); // de sau tim hieu them
}
