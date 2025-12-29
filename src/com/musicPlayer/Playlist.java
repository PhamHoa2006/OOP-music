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

//Thêm dòng này để Jackson không bị lỗi khi đọc các trường size, likesCount...
@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {
    private String id; // id Playlist
    private String name; // Tên
    private List<Song> songs; // Danh sách bài

    private Set<String> likedByUsers; // Danh sách user thích
    private List<Comment> comments; // Bình luận
    private Map<String, List<Comment>> commentsByUser; // Bình luận bởi ...

    private String creator; // Tên tác giả
    private String description; // Mô tả
    private String privacy; // Chế độ

    // Khởi tạo playlist
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

    // Lấy id playlist
    public String getId() {
        return this.id;
    }

    // Thiết lập id playlist
    public void setId(String id) {
        this.id = id;
    }

    // Lấy chế độ
    public String getPrivacy() {
        return privacy;
    }

    // Thiết lập chế độ
    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    // Jackson sẽ dùng getter này để ghi ra "size": 0
    @JsonIgnore
    public int getSize() {
        return songs.size();
    } // Trả về số bài hát trong Playlist

    @JsonIgnore
    // Trả về tổng Duration các bài trong Playlist
    public int getTotalDurationSeconds() {
        int total = 0;
        for (Song s : songs)
            total += s.getDuration();
        return total;
    }

    public String getName() {
        return this.name;
    } // Lấy tên Playlist

    public void setName(String newName) {
        this.name = newName;
    } // Thiết lập tên Playlist

    @JsonIgnore
    public String getTitle() {
        return this.name;
    } // Lấy tên ( hiển thị cho người dùng )

    public String getCreator() {
        return creator != null ? creator : "Unknown";
    } // Lấy tên tác giả

    public void setCreator(String creator) {
        this.creator = creator;
    } // Thiết lập tên tác giả

    public String getDescription() {
        return description != null ? description : "";
    } // Lấy mô tả

    public void setDescription(String description) {
        this.description = description;
    } // Thiết lập mô tả

    public List<Song> getSongs() {
        return this.songs;
    } // Lấy danh sách bài hát trong Playlist

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    } // Thiết lập danh sách bài hát trong Playlist

    public void addSong(Song s) {
        if (s != null)
            songs.add(s);
    } // Thêm bài hát vào cuối Playlist

    // Thêm bài hát vào vị trí cụ thể của Playlist
    public void insertSong(Song s, int pos) {
        if (s == null)
            return;
        if (pos <= 0)
            songs.add(0, s);
        else if (pos >= songs.size())
            songs.add(s);
        else
            songs.add(pos, s);
    }

    public boolean removeSong(Song s) {
        return s != null && songs.remove(s);
    } // Bỏ bài khỏi Playlist

    public void clear() {
        songs.clear();
    } // Xóa các bài hát trong Playlist

    // Sắp xếp danh sách bài hát
    public void sortSongs() {
        try {
            Collections.sort(songs);
        } // Mặc định
        catch (Exception e) {
            songs.sort((s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
        } // Dự phòng
    }

    // Lấy bài hát có artist hoặc Title = keyword
    public List<Song> search(String keyword) {
        List<Song> result = new ArrayList<>();
        if (keyword == null)
            return result;
        String keyLower = keyword.toLowerCase();
        for (Song s : songs) {
            if (s.getTitle().toLowerCase().contains(keyLower) || s.getArtist().toLowerCase().contains(keyLower)) {
                result.add(s);
            }
        }
        return result;
    }

    // --- SOCIAL FEATURES ---

    // Like / unlike và trả về kết quả
    public boolean like(String userId) {
        return userId != null && !userId.isEmpty() && likedByUsers.add(userId);
    }

    public boolean unlike(String userId) {
        return userId != null && !userId.isEmpty() && likedByUsers.remove(userId);
    }

    @JsonIgnore
    public int getLikesCount() {
        return likedByUsers.size();
    } // Lấy số lượng like

    public Set<String> getLikedByUsers() {
        return new HashSet<>(likedByUsers);
    } // Trả về danh sách user thích ( bản sao, bảo vệ dữ liệu)

    public void setLikedByUsers(Set<String> likedByUsers) {
        this.likedByUsers = likedByUsers;
    } // Thiết lập ds user thích

    // Thêm bình luận
    public String addComment(String userId, String text) {
        if (userId == null || text == null)
            throw new IllegalArgumentException("Invalid input");
        Comment c = new Comment(userId, text);
        comments.add(c);
        commentsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(c);
        return c.getId();
    }

    // xóa bình luận
    public boolean removeComment(String commentId) {
        if (commentId == null)
            return false;
        java.util.Iterator<Comment> it = comments.iterator(); // Danh sách comment
        while (it.hasNext()) {
            Comment c = it.next();
            if (c.getId().equals(commentId)) {
                it.remove();
                List<Comment> userCommentList = commentsByUser.get(c.getUserId()); // Lấy danh sách comment của user
                                                                                   // hiện tại
                if (userCommentList != null) {
                    userCommentList.removeIf(x -> x.getId().equals(commentId));
                    if (userCommentList.isEmpty())
                        commentsByUser.remove(c.getUserId());
                }
                return true;
            }
        }
        return false;
    }

    public List<Comment> getComments() {
        return comments;
    } // Lấy cmt

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    } // thiết lập cmt

    // Lớp comment
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String id;
        private String userId;
        private String text;
        private Instant createdAt;

        public Comment() {
        }

        public Comment(String userId, String text) {
            this.id = UUID.randomUUID().toString();
            this.userId = userId;
            this.text = text;
            this.createdAt = Instant.now();
        }

        // Lấy, thiết lập Id
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        // Lấy, thiết lập userId
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        // Lấy, thiết lập nội dung comment
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        // Lấy, thiết lập thời điểm khởi tạo
        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }
}