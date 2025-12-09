package com.users;

import com.musicPlayer.Song;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class History {

    // 1. SỬA: Đổi thành public static để Jackson có thể truy cập và khởi tạo
    public static class SongRecord {
        private Song song;
        private LocalDate datePlayed; // Jackson xử lý LocalDate cần module JSR310, nhưng tạm thời cứ để đây
        
        // Constructor rỗng (Bắt buộc cho Jackson)
        public SongRecord() {}

        public SongRecord(Song song, LocalDate datePlayed) {
            this.song = song;
            this.datePlayed = datePlayed;
        }

        public Song getSong() { return song; }
        public void setSong(Song song) { this.song = song; } // Cần Setter

        public LocalDate getDatePlayed() { return datePlayed; }
        public void setDatePlayed(LocalDate datePlayed) { this.datePlayed = datePlayed; } // Cần Setter
    }

    private Map<Song, Integer> userPlayCount;
    private LinkedList<SongRecord> userHistory;

    // Biến static này không lưu vào file JSON của từng user -> @JsonIgnore
    @JsonIgnore
    private static final Map<Song, Integer> globalPlayCount = new HashMap<>();
    
    @JsonIgnore
    private static final int MAX_HISTORY_SIZE = 100;

    // 2. SỬA: Thêm Constructor mặc định cho Jackson
    public History() {
        this.userPlayCount = new HashMap<>();
        this.userHistory = new LinkedList<>();
    }

    // Logic thêm bài hát (Giữ nguyên logic của bạn, chỉ chỉnh format code)
    public void addSong(Song song) {
        if (song == null) return;

        SongRecord record = new SongRecord(song, LocalDate.now());

        if (userHistory.size() >= MAX_HISTORY_SIZE) {
            userHistory.removeFirst();
        }
        userHistory.addLast(record);

        // Update count cá nhân
        userPlayCount.put(song, userPlayCount.getOrDefault(song, 0) + 1);

        // Update count toàn cục
        globalPlayCount.put(song, globalPlayCount.getOrDefault(song, 0) + 1);
    }

    // --- Các hàm Get logic giữ nguyên ---

    public Map<LocalDate, List<Song>> getUserHistoryByDate() {
        Map<LocalDate, List<Song>> historyByDate = new LinkedHashMap<>();
        // Đảo ngược để xem mới nhất trước
        Iterator<SongRecord> it = userHistory.descendingIterator(); 
        
        while(it.hasNext()){
            SongRecord record = it.next();
            historyByDate.computeIfAbsent(record.getDatePlayed(), k -> new ArrayList<>()).add(record.getSong());
        }
        return historyByDate;
    }

    public List<Song> getPlayedSongs() {
        List<Song> playedSongs = new ArrayList<>();
        // Lấy từ mới nhất đến cũ nhất
        Iterator<SongRecord> it = userHistory.descendingIterator();
        while(it.hasNext()){
             playedSongs.add(it.next().getSong());
        }

        return playedSongs;
    }

    // Getter cho Jackson
    public LinkedList<SongRecord> getUserHistory() { return userHistory; }
    public void setUserHistory(LinkedList<SongRecord> userHistory) { this.userHistory = userHistory; }

    public Map<Song, Integer> getUserPlayCount() { return userPlayCount; }
    public void setUserPlayCount(Map<Song, Integer> userPlayCount) { this.userPlayCount = userPlayCount; }

    // --- Logic Trending & Suggestion (Giữ nguyên logic tốt của bạn) ---
    
    @JsonIgnore // Không lưu kết quả tính toán vào file
    public static List<Song> getGlobalTrending(int topN) {
        if (topN <= 0) return new ArrayList<>();
        List<Map.Entry<Song, Integer>> entry = new ArrayList<>(globalPlayCount.entrySet());
        entry.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Song> topTrending = new ArrayList<>();
        int limit = Math.min(topN, entry.size());

        for (int i = 0; i < limit; i++) {
            topTrending.add(entry.get(i).getKey());
        }

        return topTrending;
    }

    @JsonIgnore
    public Song getMostPlayedByUser() {
        if (userPlayCount.isEmpty()) return null;
        return Collections.max(userPlayCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    @JsonIgnore
    public List<Song> suggestMostPlayed(int topN) {
        if (topN <= 0) return new ArrayList<>();
        List<Map.Entry<Song, Integer>> entries = new ArrayList<>(userPlayCount.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Song> suggestions = new ArrayList<>();
        int limit = Math.min(topN, entries.size());
        for (int i = 0; i < limit; i++) {
            suggestions.add(entries.get(i).getKey());
        }

        return suggestions;
    }
}
