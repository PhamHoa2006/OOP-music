package com.musicPlayer;


//Test nha Dung
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Song implements Comparable<Song> {
    // I. Thuộc tính
    private final String songID;
    private String title;
    private String artist;
    private String album;
    private int duration;   // đơn vị: giây
    private String url;
    private List<String> genres; // thể loại
    private int playCount; // lượt nghe // có thể suggest bằng rating và playCount
    
    private List<Integer> ratingList;
    private List<String> lyricLines; // lưu từng dọc lời bài hát
    private List<Integer> timeStamps; // thời gian đến từng lời bài hát, đơn vị mili giây. 5000 = 5s.
    private Long totalLike;

    // 1.Constructor basic
    
    public Song(String title, String artist, String album, int duration, String url) {
        this.songID = UUID.randomUUID().toString();
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url;
        this.playCount = 0;
        this.genres = new ArrayList<>();
        ratingList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        this.lyricLines = new ArrayList<>();
        this.timeStamps = new ArrayList<>();
        this.totalLike = 0L;
    }
    
    // 2.Constructor có phần TimeStamps

    public Song(String title, String artist, String album, int duration, String url, List<String> genres, List<String> lyricLines, List<Integer> timeStamps) {
        this.songID = UUID.randomUUID().toString();
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url;
        this.genres = genres;
        this.playCount = 0;
        ratingList = new ArrayList<>(Arrays.asList(0,0,0,0,0));
        this.lyricLines = lyricLines;
        this.timeStamps = timeStamps;
        this.totalLike = 0L;
    }

    // 3. Constructor không có TimeStamp

    public Song(String title, String artist, String album, int duration, String url, List<String> genres, List<String> lyricLines) {
        this.songID = UUID.randomUUID().toString();
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url;
        this.genres = genres;
        this.playCount = 0;
        ratingList = new ArrayList<>(Arrays.asList(0,0,0,0,0));
        this.lyricLines = lyricLines;
        this.timeStamps = new ArrayList<> (); // bỏ qua
        this.totalLike = 0L;
    }

    // II.Getter và Setter

    public String getSongID() {return this.songID;}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public int getPlayCount() {return playCount;}
    public void setPlayCount(int playCount) {this.playCount = playCount;}

    public Long getTotalLike() {return totalLike;}
    public void setTotalLike(Long totalLike) {this.totalLike = totalLike;}
 
    
    public List<Integer> getRatingList() {return this.ratingList;}
    public void setRatingList(List<Integer> ratingList) {
        if (ratingList == null) {throw new IllegalArgumentException("Empty list.");}
        if (ratingList.size() != 5) {throw new IllegalArgumentException("Wrong size.");}
        for (int rating : ratingList) {if (rating < 0) {throw new IllegalArgumentException("Negative value");}}
        this.ratingList = new ArrayList<>(ratingList);
    }

    public List<String> getLyrics() {return this.lyricLines;}
    public void setLyrics(List<String> lyrics) {this.lyricLines = lyrics;}

    public List<Integer> getTimeStamps() {return this.timeStamps;}
    public void setTimeStamps(List<Integer> timeStamps) {this.timeStamps = timeStamps;}

    public int getIndexAtTime(int currentTime) { // nhập thời gian lấy dòng bài hát tại đó
        if (timeStamps == null || lyricLines == null || timeStamps.isEmpty() || lyricLines.isEmpty()) return -1;
        if (currentTime < 0 || currentTime > duration * 1000) return -1;
        for (int i = 0; i < timeStamps.size(); i++) {
            int start = timeStamps.get(i);
            int end = (i + 1 < timeStamps.size()) ? timeStamps.get(i + 1) : duration;
            if (currentTime >= start && currentTime < end)
            return i;
        }
    return -1; // nếu không khớp với khoảng nào
    }
    
    public void setLyricLine(String s, int i) {  // setter lời bài hát tại một dòng (lấy Line bằng Index)
        if (i >= 0 && i < lyricLines.size()) {lyricLines.set(i, s);}}
    public String getLyricLine(int i) {   // getter lời bài hát tại một dòng (lấy Line bằng Index)
        if (i >= 0 && i < lyricLines.size()) return lyricLines.get(i);
        else return ""; // trả về "" để tránh null
    }
    
    /// III. Phương thức

    //1. Trả về thông tin cơ bản của bài hát

    @Override
    public String toString() {
        return title + " - " + artist;
    }
       
    //2. So sánh và sắp thứ tự bài hát theo chuỗi tên, nghệ sĩ, thời lượng.

    @Override
    public int compareTo(Song other) {
    return Comparator
        .comparing(Song::getTitle, String.CASE_INSENSITIVE_ORDER)
        .thenComparing(Song::getArtist, String.CASE_INSENSITIVE_ORDER)
        .thenComparingInt(Song::getDuration)
        .compare(this, other);
    }
    
    //3. Sắp theo ID

    public static void sortById(List<Song> songs) {
    songs.sort(Comparator.comparing(Song::getSongID));
    }

    //3. Tăng số lần nghe.

    public void increasePlayCount() {this.playCount++;}

    //4. Tăng số lượt thích.

    public void increaseTotalLike() {this.totalLike++;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Nếu là cùng 1 đối tượng
        if (o == null || getClass() != o.getClass()) return false; // Nếu khác kiểu
        Song song = (Song) o;
        // Coi 2 bài hát là một nếu ID giống nhau (đây là cách tốt nhất)
        return java.util.Objects.equals(songID, song.songID); 
    }

    @Override
    public int hashCode() {
    // Chỉ hash dựa trên ID
    return java.util.Objects.hash(songID);
    }
}