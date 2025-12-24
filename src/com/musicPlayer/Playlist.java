package com.musicPlayer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Playlist {
    private final String id;
    private String name;
    private final List<Song> songs;
    
    // Social features
    private final Set<String> likedByUsers;
    private final List<Comment> comments;   // All comments
    private final Map<String, List<Comment>> commentsByUser;    // Key: user id
    
    // Metadata
    private String creator;
    private String description;

    // --- CONSTRUCTORS ---

    // Constructor mặc định (quan trọng cho Jackson/JSON và khởi tạo rỗng)
    public Playlist() {
        this.id = UUID.randomUUID().toString();
        this.songs = new ArrayList<>();
        this.likedByUsers = new HashSet<>();
        this.comments = new ArrayList<>();
        this.commentsByUser = new HashMap<>();
        
        // [QUAN TRỌNG] Khởi tạo giá trị mặc định để tránh NullPointerException
        this.name = "New Playlist";
        this.creator = "Unknown";
        this.description = ""; 
    }

    // Constructor có tên
    public Playlist(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.songs = new ArrayList<>();
        this.likedByUsers = new HashSet<>();
        this.comments = new ArrayList<>();
        this.commentsByUser = new HashMap<>();
        
        // [QUAN TRỌNG] Khởi tạo giá trị mặc định
        this.creator = "Unknown"; // Mặc định là Unknown, sau này setCreator sau
        this.description = "";    // Mặc định rỗng
    }
    
    // --- GETTERS & SETTERS ---

    public String getId() {
        return this.id;
    }
    
    public int getSize() {
        return songs.size();
    }
    
    public int getTotalDurationSeconds() {
        int total = 0;
        for (Song s : songs) {
            total += s.getDuration(); 
        }
        return total;
    }
    
    // Lấy tên Playlist (Gốc)
    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    // [ALIAS] Lấy tiêu đề (Dùng cho MainController đỡ phải sửa nhiều)
    public String getTitle() {
        return this.name; 
    }

    // Creator
    public String getCreator() {
        return creator != null ? creator : "Unknown";
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    // Description
    public String getDescription() {
        // Trả về chuỗi rỗng nếu null để hiển thị lên UI không bị lỗi
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    // --- SONG MANAGEMENT ---

    public List<Song> getSongs(){
        return this.songs;
    }
    
    public void addSong(Song s) {
        if (s != null) songs.add(s);    
    }
    
    public void insertSong(Song s, int pos) {
        if (s == null) return;
        if (pos <= 0) {
            songs.add(0, s);
        } else if (pos >= songs.size()) {
            songs.add(s);
        } else {
            songs.add(pos, s);
        }
    }

    public boolean removeSong(Song s) {
        if (s == null) return false; 
        return songs.remove(s);
    }
    
    public void clear() {
        songs.clear();
    }

    public void sortSongs() {
        // Yêu cầu class Song phải implements Comparable<Song> hoặc dùng Comparator
        try {
            Collections.sort(songs); 
        } catch (Exception e) {
            // Fallback nếu Song chưa implement Comparable: Sắp xếp theo tên
            songs.sort((s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
        }
    }

    public List<Song> search(String keyword) {
        List<Song> result = new ArrayList<>();
        if (keyword == null) return result;
        
        String keyLower = keyword.toLowerCase();
        for (Song s : songs) {
            if (s.getTitle().toLowerCase().contains(keyLower) || s.getArtist().toLowerCase().contains(keyLower)) {
                result.add(s);
            }
        }
        return result;
    }
    
    // --- SOCIAL FEATURES (LIKES & COMMENTS) ---

    public boolean like(String userId) {
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId invalid");
        return likedByUsers.add(userId);
    }
    
    public boolean unlike(String userId) {
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId invalid");
        return likedByUsers.remove(userId);
    }
    
    public int getLikesCount() {    
        return likedByUsers.size();
    }
    
    public Set<String> getLikedByUsers(){
        return new HashSet<>(likedByUsers);
    }
    
    public String addComment(String userId, String text) {
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId invalid");
        if (text == null) throw new IllegalArgumentException("text invalid");
        
        Comment c = new Comment(userId, text);
        comments.add(c);
        commentsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(c);
        return c.getId();
    }
    
    public boolean removeComment(String commentId) {
        if (commentId == null) return false;

        java.util.Iterator<Comment> it = comments.iterator();
        while (it.hasNext()) {
            Comment c = it.next();
            if (c.getId().equals(commentId)) {
                it.remove(); // Xóa khỏi list tổng
                
                // Xóa khỏi map theo user
                List<Comment> userCommentList = commentsByUser.get(c.getUserId());
                if (userCommentList != null) {
                    userCommentList.removeIf(x -> x.getId().equals(commentId));
                    if (userCommentList.isEmpty()) {
                        commentsByUser.remove(c.getUserId());
                    }
                }
                return true;
            }
        }
        return false;
    }
   
    // --- INNER CLASS: COMMENT ---
    public static class Comment {
        private final String id;
        private final String userId;
        private final String text;
        private final Instant createdAt;

        public Comment(String userId, String text) {
            this.id = UUID.randomUUID().toString();
            this.userId = userId;
            this.text = text;
            this.createdAt = Instant.now();
        }

        public String getId() { return id; }
        public String getUserId() { return userId; }
        public String getText() { return text; }
        public Instant getCreatedAt() { return createdAt; }
    }
}