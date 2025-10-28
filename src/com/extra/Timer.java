package com.extra;

import java.util.ArrayList;
import java.util.List;

// Lớp Timers
public class Timer implements Runnable {
    private int timeRemaining;
    private boolean active = false;
    private boolean paused = false;

    private final List<TimerListener> listeners = new ArrayList<>();
    private Thread timerThread;

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
        if (seconds <= 0) {
            return;
        }

        if (active) {
            cancelTimer();
        }

        this.timeRemaining = seconds;
        this.active = true;
        this.paused = false;

        timerThread = new Thread(this);
        timerThread.start();
    }

    // Huỷ hẹn giờ đang chạy
    public synchronized void cancelTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            active = false;
            paused = false;
            timerThread.interrupt();

            for (TimerListener listener : listeners) {
                listener.onTimerCancelled();
            }
        }
        else {
            return;
        }
    }

    // Trạng thái của bộ hẹn giờ
    public boolean isActive() {
        return active;
    }

    // Dừng bộ hẹn giờ
    public synchronized void pause() {
        if(active && !paused) {
            paused = true;
        }
    }

    // Tiếp tục bộ hẹn giờ
    public synchronized void resume() {
        if (active && paused) {
            paused = false;
            notify();
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
            while (active && timeRemaining > 0) {
                synchronized (this) {
                    while (paused) {
                        wait();
                    }
                }

                Thread.sleep(1000);
                timeRemaining--;
            }

            if (active) {
                for (TimerListener listener : listeners) {
                    listener.onTimerFinished();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            active = false;
            paused = false;
        }
    }
}
