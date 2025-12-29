package com.musicPlayer;

public interface Player {
    void play(); // bat dau hoac tiep tuc

    void pause(); // tam dung

    void stop(); // dung hoan toan

    void next(); // bai tiep theo

    void previous(); // bai truoc

    void seekForward(int seconds); // tua toi

    void seekBackward(int seconds); // tua lui

    void seek(int seconds); // tua cu the

    Song getCurrentSong(); // Lấy bài hát hiện tại

    double getCurrentTime(); // Lấy thời lượng hiện tại

    double getVolume(); // lay am luong

    boolean isPlaying(); // check trang thai

    boolean isStopped(); // heck trang thai

    boolean isPaused(); // check trang thai

    void setOnSongEnd(Runnable callback); // Hàm callback

    void setVolume(double volume); // Thiết lập âm lượng

    double getTotalDuration(); // Lấy duration thực tế từ file (chính xác hơn Song object lưu)
}
