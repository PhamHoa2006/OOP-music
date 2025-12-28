package com.musicPlayer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty("id")
    private String songID;

    private String title;
    // Không dùng List<String> nữa để tránh lỗi deserialization
    private String artist;

    private String album;
    private double duration;

    @JsonProperty("filePath")
    private String url;

    private List<String> genres;
    private int playCount;

    private List<Integer> ratingList;
    private List<String> lyricLines;
    private List<Integer> timeStamps;
    private Long totalLike;

    // --- CONSTRUCTOR MẶC ĐỊNH ---
    public Song() {
        this.songID = UUID.randomUUID().toString();
        this.artist = "Unknown"; // Khởi tạo chuỗi mặc định
        this.genres = new ArrayList<>();
        this.ratingList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        this.lyricLines = new ArrayList<>();
        this.timeStamps = new ArrayList<>();
        this.totalLike = 0L;
    }

    // --- CONSTRUCTOR CÓ THAM SỐ (Dùng khi upload) ---
    public Song(String title, String artist, String album, double duration, String url) {
        this.songID = UUID.randomUUID().toString();
        this.title = title;

        // Nhận thẳng chuỗi (Ví dụ: "Sơn Tùng - Snoop Dogg")
        this.artist = artist;

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

    // --- HÀM LẤY LINK NHẠC ---
    @JsonIgnore
    public String getPlayableUrl() {
        try {
            if (url != null && (url.startsWith("http") || url.startsWith("file:"))) {
                return url;
            }

            String projectDir = System.getProperty("user.dir");
            String normalizedPath = url.replace("\\", "/");

            File file = new File(projectDir, normalizedPath);

            // --- LOG DEBUG ---
            if (!file.exists()) {
                System.err.println("❌ FILE NOT FOUND: " + normalizedPath);
                return null;
            }
            // -----------------

            return file.toURI().toString();
        } catch (Exception e) {
            System.err.println("URL Error: " + e.getMessage());
            return null;
        }
    }

    // --- GETTERS & SETTERS ---

    public String getSongID() {
        return songID;
    }

    public void setSongID(String songID) {
        this.songID = songID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter trả về String trực tiếp
    public String getArtist() {
        if (artist == null)
            return "Unknown Artist";
        return artist;
    }

    // Setter nhận String trực tiếp
    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public Long getTotalLike() {
        return totalLike;
    }

    public void setTotalLike(Long totalLike) {
        this.totalLike = totalLike;
    }

    public List<Integer> getRatingList() {
        return ratingList;
    }

    public void setRatingList(List<Integer> ratingList) {
        this.ratingList = ratingList;
    }

    public List<String> getLyrics() {
        return lyricLines;
    }

    public void setLyrics(List<String> lyrics) {
        this.lyricLines = lyrics;
    }

    public List<Integer> getTimeStamps() {
        return timeStamps;
    }

    public void setTimeStamps(List<Integer> timeStamps) {
        this.timeStamps = timeStamps;
    }

    // --- LOGIC LYRICS ---
    public int getIndexAtTime(double currentTimeInSeconds) {
        if (timeStamps == null || lyricLines == null || timeStamps.isEmpty())
            return -1;
        int currentTimeMs = (int) (currentTimeInSeconds * 1000);
        if (currentTimeMs < 0 || currentTimeMs > (duration * 1000))
            return -1;

        for (int i = 0; i < timeStamps.size(); i++) {
            int start = timeStamps.get(i);
            int end = (i + 1 < timeStamps.size()) ? timeStamps.get(i + 1) : (int) (duration * 1000);
            if (currentTimeMs >= start && currentTimeMs < end)
                return i;
        }
        return -1;
    }

    // --- OVERRIDES ---

    @Override
    public String toString() {
        return title + " - " + artist;
    }

    @Override
    public int compareTo(Song other) {
        // So sánh String thì dùng String.CASE_INSENSITIVE_ORDER trực tiếp
        // Logic: So tên bài -> So tên ca sĩ
        int titleCompare = String.CASE_INSENSITIVE_ORDER.compare(this.title, other.title);
        if (titleCompare != 0)
            return titleCompare;
        return String.CASE_INSENSITIVE_ORDER.compare(this.artist, other.artist);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Song song = (Song) o;
        return Objects.equals(songID, song.songID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songID);
    }
}