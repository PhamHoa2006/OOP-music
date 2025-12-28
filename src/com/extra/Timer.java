package com.extra;

import java.util.ArrayList;
import java.util.List;

// Lớp Timers
public class Timer implements Runnable {
    private int timeRemaining;
    private boolean active = false;
    private boolean paused = false;

    private final List<TimerListener> listeners = new ArrayList<>(); // danh sách đối tượng
    private Thread timerThread; // Luồng chạy timer

    // Thêm 1 đối tượng nhận thông báo khi bộ hẹn giờ kết thúc
    public void addListener(TimerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    // Xoá đối tượng không muốn nhận thông báo
    public void removeListener(TimerListener listener) {
        listeners.remove(listener);
    }

    // Đặt hẹn giờ
    public synchronized void setTimer(int seconds) {
        // Kiểm tra hợp lệ
        if (seconds <= 0) {
            return;
        }

        // Hủy Timer cũ nếu đang chạy
        if (active) {
            cancelTimer();
        }

        // Cập nhập trạng thái mới
        this.timeRemaining = seconds;
        this.active = true;
        this.paused = false;

        // Tạo và chạy luồng mới
        timerThread = new Thread(this);
        timerThread.start();
    }

    // Huỷ hẹn giờ đang chạy
    public synchronized void cancelTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            // Cập nhập trạng thái
            active = false;
            paused = false;
            timerThread.interrupt(); // Ngắt luồng

            for (TimerListener listener : listeners) {
                listener.onTimerCancelled(); // Thông báo -> listener
            }
        } else {
            return;
        }
    }

    // Trạng thái của bộ hẹn giờ
    public boolean isActive() {
        return active;
    }

    // Dừng bộ hẹn giờ
    public synchronized void pause() {
        if (active && !paused) {
            paused = true;
        }
    }

    // Tiếp tục bộ hẹn giờ
    public synchronized void resume() {
        if (active && paused) {
            paused = false;
            notify(); // Đánh thức luồng -> Timer thoát wait và chạy
        }
    }

    // Lấy số giây còn lại của bộ hẹn giờ
    public int getTimeRemaining() {
        return timeRemaining;
    }

    // Ghi đè phương thức run của Runnable
    @Override
    public void run() {
        try {
            // Luồng đang chạy và còn thời gian
            while (active && timeRemaining > 0) {
                synchronized (this) {
                    // Dừng luồng khi gặp lệnh pause
                    while (paused) {
                        wait();
                    }
                }

                // Đếm ngược giây ( hàm sleep theo ms )
                Thread.sleep(1000);
                timeRemaining--;
            }

            // Thông báo khi khi timer kết thúc
            if (active) {
                for (TimerListener listener : listeners) {
                    listener.onTimerFinished();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Gặp lỗi thì hủy luồng
        } finally {
            // Cập nhập trạng thái
            active = false;
            paused = false;
        }
    }
}
