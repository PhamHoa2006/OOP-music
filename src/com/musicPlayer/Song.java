package com.musicPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Song implements Comparable<Song> {
    // I. Thuộc tính
    private String title;
    private String artist;
    private String album;
    private int duration;   // đơn vị: giây
    private String url;
    private List<String> genres; // thể loại
    private int playCount; // lượt nghe // có thể suggest bằng rating và playCount
    private double rating;
    private List<Integer> ratingList; // 1,2,3,4,5: sl dg, 0: tong dg, 6: tong sao
    private List<String> lyricLines; // lưu từng dọc lời bài hát
    private List<Integer> timeStamps; // thời gian đến từng lời bài hát, đơn vị mili giây. 5000 = 5s.
    private boolean like;

    // 1.Constructor basic
    
    public Song(String title, String artist, String album, int duration, String url) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url;
        this.playCount = 0;
        this.genres = new ArrayList<>();
        this.rating = 0;
        this.ratingList = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0));
        this.lyricLines = new ArrayList<>();
        this.timeStamps = new ArrayList<>();
    }
    
    // 2.Constructor có phần TimeStamps

    public Song(String title, String artist, String album, int duration, String url, List<String> genres, List<String> lyricLines, List<Integer> timeStamps) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url;
        this.genres = genres;
        this.playCount = 0;
        this.rating = 0;
        this.ratingList = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0));
        this.lyricLines = lyricLines;
        this.timeStamps = timeStamps;
        this.like = false;
    }

    // 3. Constructor không có TimeStamp

    public Song(String title, String artist, String album, int duration, String url, List<String> genres, List<String> lyricLines) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.url = url;
        this.genres = genres;
        this.playCount = 0;
        this.rating = 0;
        this.ratingList = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0));
        this.lyricLines = lyricLines;
        this.timeStamps = new ArrayList<> (); // bỏ qua
        this.like = false;
    }

    // II.Getter và Setter

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

    public double getRating() {return this.rating;}
    public void setRating(float rating) {this.rating = rating;}

    public List<Integer> getRatingList() {return this.ratingList;}
    public void setRatingList(List<Integer> lis) {
        int luot = 0, tong = 0;
        for (int i=1; i<=5; i++) {
            this.ratingList.set(i, lis.get(i));
            luot += lis.get(i);
            tong += lis.get(i)*i;
        }
        this.ratingList.set(0, luot);
        this.ratingList.set(6, tong);
        this.rating = (tong*1.0)/luot;
    }

    public List<String> getLyrics() {return this.lyricLines;}
    public void setLyrics(List<String> lyrics) {this.lyricLines = lyrics;}

    public List<Integer> getTimeStamps() {return this.timeStamps;}
    public void setTimeStamps(List<Integer> timeStamps) {this.timeStamps = timeStamps;}

    public int getIndexAtTime(int currentTime) {          // getter để lấy lời bài hát tại một khoảng thời gian (lấy Index bằng Time)
        int i; // i là LyricIndex, là thứ tự dòng của câu hiện tại
        if (timeStamps.isEmpty() || lyricLines.isEmpty()) {return -1;}
        if (currentTime < 0 || currentTime > duration) {return -1;}
        for (i = 0; i < timeStamps.size(); i++) {
            int startLyricLine = timeStamps.get(i);
            int endLyricLine = (i < timeStamps.size() -1) ? timeStamps.get(i+1) : duration;
            if (currentTime >= startLyricLine && currentTime < endLyricLine) break;
        }
        return i;
    }
    
    public void setLyricLine(String s, int i) {  // setter lời bài hát tại một dòng (lấy Line bằng Index)
        if (i >= 0 && i < lyricLines.size()) {
            lyricLines.set(i, s);
        }
    }
    public String getLyricLine(int i) {   // getter lời bài hát tại một dòng (lấy Line bằng Index)
        if (i >= 0 && i < lyricLines.size()) { 
        return lyricLines.get(i);
        }
    return ""; // trả về "" để tránh null
    }

    public boolean getLike() {return this.like;}
    public void setlike(boolean TF) {this.like = TF;}
    
    /// III. Phương thức

    //1. In ra một dòng lời bài hát (chỉ số thứ tự)

    public void displayLyricLine(int index) {
        if (index >= 0 && index < lyricLines.size()) {
        System.out.println(lyricLines.get(index));
        }
    }

    //2. In toàn bộ lời bài hát

    public void displayAllLyrics() {
        if (lyricLines == null || lyricLines.isEmpty()) {
            System.out.println("Chua co loi bai hat.");
            return;
        }
        System.out.println("Loi bai hat: " + title);
        for (int i = 0; i < lyricLines.size(); i++) {
        // Nếu có timeStamp thì in kèm
            if (i < timeStamps.size()) {
            System.out.printf("[%ds] %s%n", timeStamps.get(i), lyricLines.get(i));
            } else {
            System.out.println(lyricLines.get(i));
            }
        }
    }

    //3. Hiển thị thông tin bài hát

    @Override
    public String toString() {
        return title + " - " + artist;
    }
       
    //4. So sánh và sắp thứ tự bài hát theo chuỗi tên

    @Override
    public int compareTo(Song other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    //5. Tăng số lần nghe

    public void increasePlayCount() {this.playCount++;}

    //6. Rating cho bài hát

    public void rateThisSong(int score) {
        if (score < 1 || score > 5) System.out.println("Rating khong hop le");
        else {
            this.ratingList.set(score, this.ratingList.get(score) + 1); // tăng thêm 1 lượt điểm x (1-5)
            this.ratingList.set(0, this.ratingList.get(0) + 1); // tăng tổng đánh giá thêm 1
            this.ratingList.set(6, this.ratingList.get(6) + score); // tăng tổng điểm thêm x
            this.rating = this.ratingList.get(6) * 1.0 / this.ratingList.get(0); // chỉnh overall rating
        }
    }

    //7. Hiện danh sách Rating
    
    public void displayRating() {
        for (int i=1; i<=5; i++) {
            System.out.println(i + " sao: " + this.ratingList.get(i));
        }
        System.out.println("Overall: " + this.rating);
    }

    //8. Thêm ưa thích

    public void likeSong() {this.setlike(true);}
    public void unlikeSong() {this.setlike(false);}

    //9. Thêm thể loại

    public void addGenre(String G) {this.genres.add(G);}
    public void deleteGenre(String G) {this.genres.remove(G);}

}