package com.users;

import com.musicPlayer.Playlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistLibrary {
    private static PlaylistLibrary instance;
    private List<Playlist> allPlaylists;
    private final String FILE_PATH = "data/playlists.json";
    private ObjectMapper mapper = new ObjectMapper();

    private PlaylistLibrary() {
        loadFromJSON();
    }

    public static PlaylistLibrary getInstance() {
        if (instance == null) instance = new PlaylistLibrary();
        return instance;
    }

    public void loadFromJSON() {
        try {
            File file = new File(FILE_PATH);
            // Kiểm tra file tồn tại và KHÔNG trống
            if (file.exists() && file.length() > 0) {
                allPlaylists = mapper.readValue(file, new TypeReference<List<Playlist>>() {});
            } else {
                // Nếu file trống hoặc chưa tồn tại
                allPlaylists = new ArrayList<>();
                saveToJSON(); // Ghi [] vào file để Jackson không báo lỗi lần sau
            }
        } catch (Exception e) {
            allPlaylists = new ArrayList<>();
            System.err.println("Lỗi đọc JSON, khởi tạo danh sách mới: " + e.getMessage());
        }
    }

    public void saveToJSON() {
        try {
            mapper.writeValue(new File(FILE_PATH), allPlaylists);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Playlist> getAllPlaylists() { return allPlaylists; }
    
    public void addPlaylist(Playlist p) {
        if (allPlaylists == null) allPlaylists = new ArrayList<>();
        allPlaylists.add(p);
        saveToJSON();
    }
}