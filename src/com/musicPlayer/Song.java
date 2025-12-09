package com.musicPlayer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty; // Import cái này để map tên JSON

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Song implements Comparable<Song>, Serializable {
    private static final long serialVersionUID = 1L;

    private String songID;
    private String title;
    
    // 1. SỬA: Đổi sang List để hứng được mảng ["Artist1", "Artist2"] từ JSON
    private List<String> artist; 
    
    private String album;
    private double duration;
    
    // 2. SỬA: Map key "filePath" trong JSON vào biến "url" của Java
    @JsonProperty("filePath") 
    private String url; 
    
    private List<String> genres;
    private int playCount;
    
    private List<Integer> ratingList;
    private List<String> lyricLines;
    private List<Integer> timeStamps;
    private Long totalLike;

    public Song() {
        this.songID = UUID.randomUUID().toString();
        this.artist = new ArrayList<>(); // Khởi tạo list
        this.genres = new ArrayList<>();
        this.ratingList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        this.lyricLines = new ArrayList<>();
        this.timeStamps = new ArrayList<>();
        this.totalLike = 0L;
    }

    // Constructor dùng khi tạo tay (nếu cần)
    public Song(String title, String artist, String album, double duration, String url) {
        this.songID = UUID.randomUUID().toString();
        this.title = title;
        this.artist = new ArrayList<>();
        this.artist.add(artist); // Add artist đơn lẻ vào list
        this.album = album;
        this.duration = duration;
        this.url = url;
        
        this.playCount = 0;
        this.genres = new ArrayList<>();
        this.ratingList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        this.lyricLines = new ArrayList<>();
        this.timeStamps = new ArrayList<>();
        this.totalLike = 0L;
    }

    @JsonIgnore
    public String getPlayableUrl() {
        try {
            if (url != null && (url.startsWith("http") || url.startsWith("file:"))) {
                return url;
            }
            
            String projectDir = System.getProperty("user.dir");
            // Fix: Đảm bảo đường dẫn dùng dấu / xuôi
            String normalizedPath = url.replace("\\", "/");
            
            File file = new File(projectDir, normalizedPath);
            
            // --- ĐOẠN DEBUG (In ra terminal) ---
            System.out.println("------------------------------------------------");
            System.out.println("Đang tìm file nhạc tại: " + file.getAbsolutePath());
            if (!file.exists()) {
                System.err.println("❌ LỖI: File không tồn tại! Hãy kiểm tra lại thư mục 'data'");
                return null;
            } else {
                System.out.println("✅ File TỒN TẠI. Đang nạp vào player...");
            }
            // ------------------------------------

            return file.toURI().toString();
        } catch (Exception e) {
            System.err.println("Lỗi tạo đường dẫn: " + e.getMessage());
            return null;
        }
    }

    // --- Getters & Setters ---

    public String getSongID() { return songID; }
    public void setSongID(String songID) { this.songID = songID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // 3. SỬA: Getter trả về String (nối các ca sĩ bằng dấu phẩy) để hiển thị lên UI không bị lỗi
    public String getArtist() { 
        if (artist == null || artist.isEmpty()) return "Unknown Artist";
        return String.join(", ", artist); 
    }
    
    // Setter nhận List (để Jackson dùng)
    public void setArtist(List<String> artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public int getPlayCount() { return playCount; }
    public void setPlayCount(int playCount) { this.playCount = playCount; }

    public Long getTotalLike() { return totalLike; }
    public void setTotalLike(Long totalLike) { this.totalLike = totalLike; }

    public List<Integer> getRatingList() { return ratingList; }
    public void setRatingList(List<Integer> ratingList) { this.ratingList = ratingList; }

    public List<String> getLyrics() { return lyricLines; }
    public void setLyrics(List<String> lyrics) { this.lyricLines = lyrics; }

    public List<Integer> getTimeStamps() { return timeStamps; }
    public void setTimeStamps(List<Integer> timeStamps) { this.timeStamps = timeStamps; }

    // Giữ nguyên logic lấy lời bài hát
    public int getIndexAtTime(double currentTimeInSeconds) {
        if (timeStamps == null || lyricLines == null || timeStamps.isEmpty()) return -1;
        int currentTimeMs = (int) (currentTimeInSeconds * 1000);
        if (currentTimeMs < 0 || currentTimeMs > (duration * 1000)) return -1;

        for (int i = 0; i < timeStamps.size(); i++) {
            int start = timeStamps.get(i);
            int end = (i + 1 < timeStamps.size()) ? timeStamps.get(i + 1) : (int)(duration * 1000);
            if (currentTimeMs >= start && currentTimeMs < end) return i;
        }
        return -1;
    }

    @Override
    public String toString() {
        return title + " - " + getArtist();
    }

    @Override
    public int compareTo(Song other) {
        return Comparator.comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Song::getArtist, String.CASE_INSENSITIVE_ORDER)
                .compare(this, other);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(songID, song.songID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songID);
    }
}
