package com.users;

import com.musicPlayer.Song;
import java.time.LocalDate;
import java.util.*;

// Lưu lịch sử nghe, giới hạn 100 bài
public class History {

    // Lớp SongRecord để lưu 1 bản ghi
    private class SongRecord {
        private final Song song;
        private final LocalDate datePlayed;

        public SongRecord(Song song, LocalDate datePlayed) {
            this.song = song;
            this.datePlayed = datePlayed;
        }

        public Song getSong() {
            return song;
        }

        public LocalDate getDatePlayed() {
            return datePlayed;
        }
    }

    private final Map<Song, Integer> userPlayCount = new HashMap<>();
    private final LinkedList<SongRecord> userHistory = new LinkedList<>();

    private static final Map<Song, Integer> globalPlayCount = new HashMap<>();
    private static final int MAX_HISTORY_SIZE = 100;

    // Thêm bài hát vào lịch sử
    public void addSong(Song song) {
        if (song == null)
            return;

        SongRecord record = new SongRecord(song, LocalDate.now());

        if (userHistory.size() >= MAX_HISTORY_SIZE) {
            userHistory.removeFirst();
        }
        userHistory.addLast(record);

        if (userPlayCount.containsKey(song)) {
            userPlayCount.put(song, userPlayCount.get(song) + 1);
        } else {
            userPlayCount.put(song, 1);
        }

        if (globalPlayCount.containsKey(song)) {
            globalPlayCount.put(song, globalPlayCount.get(song) + 1);
        } else {
            globalPlayCount.put(song, 1);
        }
    }

    // Lấy lịch sử nghe nhóm theo ngày
    public Map<LocalDate, List<Song>> getUserHistoryByDate() {
        Map<LocalDate, List<Song>> historyByDate = new LinkedHashMap<>();

        for (SongRecord record : userHistory) {
            LocalDate date = record.getDatePlayed();
            Song song = record.getSong();

            if (!historyByDate.containsKey(date)) {
                List<Song> songsForThisDate = new ArrayList<>();
                songsForThisDate.add(song);
                historyByDate.put(date, songsForThisDate);
            } else {
                List<Song> songsForThisDate = historyByDate.get(date);
                songsForThisDate.add(song);
            }
        }

        return historyByDate;
    }

    // Lấy lịch sử nghe không nhóm theo ngày
    public List<Song> getPlayedSongs() {
        List<Song> playedSongs = new ArrayList<>();

        for (SongRecord record : userHistory) {
            Song song = record.getSong();
            playedSongs.add(song);
        }

        return playedSongs;
    }

    // Lấy top n bài hát được nghe nhiều nhất
    public static List<Song> getGlobalTrending(int topN) {
        if (topN <= 0) {
            return new ArrayList<>();
        }

        List<Map.Entry<Song, Integer>> entry = new ArrayList<>(globalPlayCount.entrySet());
        entry.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Song> topTrending = new ArrayList<>();
        int limit = Math.min(topN, entry.size());

        for (int i = 0; i < limit; i++) {
            topTrending.add(entry.get(i).getKey());
        }

        return topTrending;
    }

    // Lấy bài hát được user nghe nhiều nhất
    public Song getMostPlayedByUser() {
        if (userPlayCount.isEmpty()) {
            return null;
        }

        Song mostPlayed = null;
        int maxCount = -1;

        for (Map.Entry<Song, Integer> entry : userPlayCount.entrySet()) {
            int currentCount = entry.getValue();

            if (currentCount > maxCount) {
                maxCount = currentCount;
                mostPlayed = entry.getKey();
            }
        }

        return mostPlayed;
    }

    // Lấy top n bài hát được user nghe nhiều nhất
    public List<Song> suggestMostPlayed(int topN) {
        if (topN <= 0) {
            return new ArrayList<>();
        }
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