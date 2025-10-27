package com.extra;

public interface TimerListener {
    // Gọi khi bộ đếm về 0
    void onTimerFinished();

    // Gọi khi bị huỷ thủ công
    void onTimerCancelled();
}
