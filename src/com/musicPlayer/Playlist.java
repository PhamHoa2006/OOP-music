package com.musicPlayer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // <--- NHỚ IMPORT DÒNG NÀY

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

// [FIX QUAN TRỌNG] Thêm dòng này để Jackson không bị lỗi khi đọc các trường size, likesCount...
@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {
    // [FIX] BỎ 'final' để Jackson đọc được dữ liệu
    private String id;
    private String name;
    private List<Song> songs;
    
    private Set<String> likedByUsers;
    private List<Comment> comments;
    private Map<String, List<Comment>> commentsByUser;
    
    private String creator;
    private String description;
    private String privacy;

    public Playlist() {
        this.id = UUID.randomUUID().toString();
        this.songs = new ArrayList<>();
        this.likedByUsers = new HashSet<>();
        this.comments = new ArrayList<>();
        this.commentsByUser = new HashMap<>();
        this.name = "New Playlist";
        this.creator = "Unknown";
        this.description = ""; 
    }

    public Playlist(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.songs = new ArrayList<>();
        this.likedByUsers = new HashSet<>();
        this.comments = new ArrayList<>();
        this.commentsByUser = new HashMap<>();
        this.creator = "Unknown";
        this.description = "";
    }
    
    // --- GETTERS & SETTERS ---

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }
    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }
    
    // Jackson sẽ dùng getter này để ghi ra "size": 0
    @JsonIgnore
    public int getSize() { return songs.size(); }
    
    @JsonIgnore
    public int getTotalDurationSeconds() {
        int total = 0;
        for (Song s : songs) total += s.getDuration(); 
        return total;
    }
    
    public String getName() { return this.name; }
    public void setName(String newName) { this.name = newName; }

    @JsonIgnore
    public String getTitle() { return this.name; }

    public String getCreator() { return creator != null ? creator : "Unknown"; }
    public void setCreator(String creator) { this.creator = creator; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }
    
    public List<Song> getSongs(){ return this.songs; }
    public void setSongs(List<Song> songs) { this.songs = songs; }
    
    public void addSong(Song s) { if (s != null) songs.add(s); }
    
    public void insertSong(Song s, int pos) {
        if (s == null) return;
        if (pos <= 0) songs.add(0, s);
        else if (pos >= songs.size()) songs.add(s);
        else songs.add(pos, s);
    }

    public boolean removeSong(Song s) { return s != null && songs.remove(s); }
    public void clear() { songs.clear(); }

    public void sortSongs() {
        try { Collections.sort(songs); } 
        catch (Exception e) { songs.sort((s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle())); }
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
    
    // --- SOCIAL FEATURES ---

    public boolean like(String userId) { return userId != null && !userId.isEmpty() && likedByUsers.add(userId); }
    public boolean unlike(String userId) { return userId != null && !userId.isEmpty() && likedByUsers.remove(userId); }
    
    @JsonIgnore
    public int getLikesCount() { return likedByUsers.size(); }
    
    public Set<String> getLikedByUsers(){ return new HashSet<>(likedByUsers); }
    public void setLikedByUsers(Set<String> likedByUsers) { this.likedByUsers = likedByUsers; }
    
    public String addComment(String userId, String text) {
        if (userId == null || text == null) throw new IllegalArgumentException("Invalid input");
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
                it.remove();
                List<Comment> userCommentList = commentsByUser.get(c.getUserId());
                if (userCommentList != null) {
                    userCommentList.removeIf(x -> x.getId().equals(commentId));
                    if (userCommentList.isEmpty()) commentsByUser.remove(c.getUserId());
                }
                return true;
            }
        }
        return false;
    }
    
    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String id;
        private String userId;
        private String text;
        private Instant createdAt;

        public Comment() {}

        public Comment(String userId, String text) {
            this.id = UUID.randomUUID().toString();
            this.userId = userId;
            this.text = text;
            this.createdAt = Instant.now();
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }
}