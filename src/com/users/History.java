package com.users;

import com.musicPlayer.Song;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class History {

    // Class con để lưu bản ghi
    @JsonIgnoreProperties(ignoreUnknown = true) 
    public static class SongRecord {
        private Song song;

        @JsonProperty("datePlayed")
        private String datePlayed; 

        public SongRecord() {}

        public SongRecord(Song song, LocalDate date) {
            this.song = song;
            this.datePlayed = date.toString();
        }

        public Song getSong() { return song; }
        public void setSong(Song song) { this.song = song; }

        @JsonIgnore
        public LocalDate getDatePlayed() { 
            return datePlayed != null ? LocalDate.parse(datePlayed) : LocalDate.now(); 
        }

        public void setDatePlayed(String datePlayed) { this.datePlayed = datePlayed; }
        
    }

    // --- SỬA ĐỔI QUAN TRỌNG TẠI ĐÂY ---
    // Key là String (songID) thay vì object Song để tránh lỗi JSON
    private Map<String, Integer> userPlayCount; 
    
    private LinkedList<SongRecord> userHistory;

    // Biến static toàn cục (Giữ nguyên Song làm key vì biến này không lưu vào file JSON)
    @JsonIgnore
    private static final Map<Song, Integer> globalPlayCount = new HashMap<>();
    
    @JsonIgnore
    private static final int MAX_HISTORY_SIZE = 100;

    public History() {
        this.userPlayCount = new HashMap<>();
        this.userHistory = new LinkedList<>();
    }

    public void addSong(Song song) {
        if (song == null) return;

        // 1. Thêm vào lịch sử (List)
        SongRecord record = new SongRecord(song, LocalDate.now());
        if (userHistory.size() >= MAX_HISTORY_SIZE) {
            userHistory.removeFirst();
        }
        userHistory.addLast(record);

        // 2. Update count cá nhân (Lưu theo ID)
        String id = song.getSongID();
        userPlayCount.put(id, userPlayCount.getOrDefault(id, 0) + 1);

        // 3. Update count toàn cục
        globalPlayCount.put(song, globalPlayCount.getOrDefault(song, 0) + 1);
    }

    // --- Các hàm Get History ---

    @JsonIgnore
    public Map<LocalDate, List<Song>> getUserHistoryByDate() {
        Map<LocalDate, List<Song>> historyByDate = new LinkedHashMap<>();
        Iterator<SongRecord> it = userHistory.descendingIterator(); 
        
        while(it.hasNext()){
            SongRecord record = it.next();
            historyByDate.computeIfAbsent(record.getDatePlayed(), k -> new ArrayList<>()).add(record.getSong());
        }
        return historyByDate;
    }

    @JsonIgnore
    public List<Song> getPlayedSongs() {
        List<Song> playedSongs = new ArrayList<>();
        Iterator<SongRecord> it = userHistory.descendingIterator();
        while(it.hasNext()){
             playedSongs.add(it.next().getSong());
        }
        return playedSongs;
    }

    // Getter/Setter chuẩn cho Jackson
    public LinkedList<SongRecord> getUserHistory() { return userHistory; }
    public void setUserHistory(LinkedList<SongRecord> userHistory) { this.userHistory = userHistory; }

    public Map<String, Integer> getUserPlayCount() { return userPlayCount; }
    public void setUserPlayCount(Map<String, Integer> userPlayCount) { this.userPlayCount = userPlayCount; }

    // --- Logic Trending & Suggestion ---
    
    // Hàm phụ trợ: Tìm object Song từ ID (Lấy từ lịch sử có sẵn)
    private Song findSongByIdInHistory(String songId) {
        if (songId == null) return null;
        // Duyệt ngược từ mới nhất để lấy thông tin cập nhật nhất
        Iterator<SongRecord> it = userHistory.descendingIterator();
        while(it.hasNext()) {
            Song s = it.next().getSong();
            if (s.getSongID().equals(songId)) {
                return s;
            }
        }
        return null;
    }

    @JsonIgnore
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
        // Tìm Entry có value (lượt nghe) lớn nhất
        Map.Entry<String, Integer> maxEntry = Collections.max(userPlayCount.entrySet(), Map.Entry.comparingByValue());
        // Map ngược từ ID ra Song
        return findSongByIdInHistory(maxEntry.getKey());
    }

    @JsonIgnore
    public List<Song> suggestMostPlayed(int topN) {
        if (topN <= 0) return new ArrayList<>();
        
        // Sắp xếp Map theo Value giảm dần
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(userPlayCount.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Song> suggestions = new ArrayList<>();
        int limit = Math.min(topN, entries.size());
        
        for (int i = 0; i < limit; i++) {
            // Lấy ID từ Map -> Tìm Song object tương ứng
            String songId = entries.get(i).getKey();
            Song s = findSongByIdInHistory(songId);
            if (s != null) {
                suggestions.add(s);
            }
        }
        return suggestions;
    }
}