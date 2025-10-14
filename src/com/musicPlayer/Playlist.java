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
    private final String name;
    private final List<Song> songs;
    private final Set<String> likedByUsers;
    private final List<Comment> comments;	// All comments
    private final Map<String, List<Comment>> commentsByUser;	// All comment with the key: user id
    
    public Playlist(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.songs = new ArrayList<>();
        this.likedByUsers = new HashSet<>();
        this.comments = new ArrayList<>();
        this.commentsByUser = new HashMap<>();
    }
    
    // Get playlist ID
    public String getId() {
    	return this.id;
    }
    
    // Get the size of list
    public int getSize() {
    	return songs.size();
    }
    
    // get the total duration
    public int getTotalDurationSeconds() {
        int total = 0;
        for (Song s : songs) {
            total += s.getDuration(); // Errors disappear after finishing Song class
        }
        return total;
    }
    
    // Get playlist name
    public String getName() {
    	return this.name;
    }
    
    // Set or change name 
    
    // Get song list
    public List<Song> getSongs(){
    	return this.songs;
    }
    
    // Add a song to the end of list.
    public void addSong(Song s) {
        if (s != null) songs.add(s);	
    }
    
    // Add a song to play list, and insert it to a specitfic position.
    public void insertSong(Song s, int pos) {
    	if (s == null) return;
    	if (pos <= 0) {
    		songs.add(0, s);
    	}
    	
    	else if (pos >= songs.size()) {
    		songs.add(s);
    	}
    	
    	else {
    		songs.add(pos, s);
    	}
    }

    // Remove a song
    public boolean removeSong(Song s) {
    	if (s == null) return false;
        return songs.remove(s);
    }
    
    // Clear all songs in the list
    public void clear() {
    	songs.clear();
    }

    // Sort songs by title (using compareTo)
    public void sortSongs() {
        Collections.sort(songs); // ascending order.
    }

    // Search song by key (artist name or title)
    public List<Song> search(String keyword) {
        List<Song> result = new ArrayList<>();
        String keyLower = keyword.toLowerCase();
        for (Song s : songs) {
            if (s.getTitle().toLowerCase().contains(keyLower) || s.getArtist().toLowerCase().contains(keyLower)) {
                result.add(s);
            }
        }
        return result;
    }
    
    // like the playlist
    public boolean like(String userId) {
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId invalid");
        return likedByUsers.add(userId);
    }
    
    // unlike the playlist
    public boolean unlike(String userId) {
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId invalid");
        return likedByUsers.remove(userId);
    }
    
    public int getLikesCount() {	
    	return likedByUsers.size();
    }
    
    public Set<String> getLikedByUsers(){	// Get list of user liking the playlist.
    	return new HashSet<>(likedByUsers);
    }
    
    public String addComment(String userId, String text) {	// Add comment to playlist
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId invalid");
        if (text == null) throw new IllegalArgumentException("text invalid");
        Comment c = new Comment(userId, text);
        comments.add(c);
        commentsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(c);
        return c.getId();
    }
    
    public boolean removeComment(String commentId) {
        if (commentId == null) return false;
        for (Comment c : comments) {
            if (c.getId().equals(commentId)) {	// Found the target comment
            	// Comment list by specific user
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
   
    
    public static class Comment {
        private final String id;
        private final String userId;
        private final String text;
        private final Instant createdAt;

        private Comment(String userId, String text) {
            this.id = UUID.randomUUID().toString();
            this.userId = userId;
            this.text = text;
            this.createdAt = Instant.now();
        }

        private String getId() { return id; }
        private String getUserId() { return userId; }
        private String getText() { return text; }
        private Instant getCreatedAt() { return createdAt; }
    }

}
