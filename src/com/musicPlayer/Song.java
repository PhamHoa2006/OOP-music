package com.musicPlayer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Annotation để Jackson bỏ qua các trường thừa trong JSON (nếu có)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Song implements Comparable<Song>, Serializable {
    private static final long serialVersionUID = 1L; // Version cho Serialization

    // I. Thuộc tính
    private String songID; // Bỏ final để Jackson có thể set giá trị khi đọc JSON
    private String title;
    private String artist;
    private String album;
    private double duration;   // ĐÃ SỬA: Đổi sang double (giây) cho thống nhất
    
    // Lưu đường dẫn TƯƠNG ĐỐI (ví dụ: "data/music/BaiHat.mp3")
    private String url; 
    
    private List<String> genres;
    private int playCount;
    
    private List<Integer> ratingList;
    private List<String> lyricLines;
    private List<Integer> timeStamps;
    private Long totalLike;

    // Constructor mặc định (BẮT BUỘC cho Jackson)
    public Song() {
        this.songID = UUID.randomUUID().toString();
        this.genres = new ArrayList<>();
        this.ratingList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        this.lyricLines = new ArrayList<>();
        this.timeStamps = new ArrayList<>();
        this.totalLike = 0L;
    }

    // 1. Constructor chính (dùng khi Import nhạc)
    public Song(String title, String artist, String album, double duration, String url) {
        this.songID = UUID.randomUUID().toString();
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url; // Lưu đường dẫn tương đối
        
        this.playCount = 0;
        this.genres = new ArrayList<>();
        this.ratingList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        this.lyricLines = new ArrayList<>();
        this.timeStamps = new ArrayList<>();
        this.totalLike = 0L;
    }

    // --- LOGIC ĐƯỜNG DẪN (QUAN TRỌNG) ---
    /**
     * Lấy đường dẫn file thực tế để MediaPlayer phát.
     * Tự động ghép thư mục dự án với đường dẫn tương đối.
     */
    @JsonIgnore // Không lưu kết quả này vào JSON
    public String getPlayableUrl() {
        try {
            // Nếu url là link online (http...), trả về luôn
            if (url != null && (url.startsWith("http") || url.startsWith("file:"))) {
                return url;
            }
            
            // Nếu là file local (đường dẫn tương đối)
            String projectDir = System.getProperty("user.dir");
            File file = new File(projectDir, url);
            return file.toURI().toString(); // Trả về dạng file:///...
        } catch (Exception e) {
            System.err.println("Lỗi đường dẫn: " + url);
            return null;
        }
    }

    // II. Getter và Setter
    public String getSongID() { return songID; }
    public void setSongID(String songID) { this.songID = songID; } // Cần setter cho Jackson

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

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

    // Logic lấy lời bài hát (Đã sửa cho double duration)
    public int getIndexAtTime(double currentTimeInSeconds) {
        if (timeStamps == null || lyricLines == null || timeStamps.isEmpty()) return -1;
        
        int currentTimeMs = (int) (currentTimeInSeconds * 1000);
        if (currentTimeMs < 0 || currentTimeMs > (duration * 1000)) return -1;

        for (int i = 0; i < timeStamps.size(); i++) {
            int start = timeStamps.get(i);
            int end = (i + 1 < timeStamps.size()) ? timeStamps.get(i + 1) : (int)(duration * 1000);
            
            if (currentTimeMs >= start && currentTimeMs < end)
                return i;
        }
        return -1;
    }

    // Override toString, equals, hashCode (Như bạn đã làm)
    @Override
    public String toString() {
        return title + " - " + artist;
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