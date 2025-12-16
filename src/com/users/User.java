package com.users;

import com.musicPlayer.Playlist;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List; // Dùng List interface cho chuẩn
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private String id;
    private String name;
    private String age;
    private String email;
    private String description; // Đã sửa chính tả
    private List<Playlist> playLists; // Dùng List thay vì ArrayList

    private String username;
    private String passwordHash;
    private History history;

    // 1. QUAN TRỌNG: Constructor rỗng cho Jackson
    public User() {
        this.playLists = new ArrayList<>();
        this.history = new History();
    }

    // Constructor dùng khi đăng ký mới
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.id = UUID.randomUUID().toString();
        this.playLists = new ArrayList<>();
        this.history = new History();
    }

    // Static Factory (Giữ lại của bạn)
    public static User create(String username, String password) {
        return new User(username, password);
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; } // Cần setter cho Jackson

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Playlist> getPlayLists() { return playLists; }
    public void setPlayLists(List<Playlist> playLists) { this.playLists = playLists; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; } // Cần setter

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public History getHistory() { return history; }
    public void setHistory(History history) { this.history = history; }

    // Logic đổi pass
    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }

    // Logic Hash (Giữ nguyên)
    public static String hashPassword(String password) {
        if (password == null) throw new IllegalArgumentException("password is null");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
