package com.users;

import com.musicPlayer.Playlist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
// Load dữ liệu từ playlist json
public class PlaylistLibrary {
    private static PlaylistLibrary instance;
    private List<Playlist> allPlaylists;
    private final String FILE_PATH = "data/playlists.json";
    private ObjectMapper mapper;

    private PlaylistLibrary() {
        mapper = new ObjectMapper();
        allPlaylists = new ArrayList<>();
        loadFromJSON();
    }

    public static PlaylistLibrary getInstance() {
        if (instance == null) instance = new PlaylistLibrary();
        return instance;
    }

    public void loadFromJSON() {
        try {
            File file = new File(FILE_PATH);
            // Tạo thư mục data nếu chưa có
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            
            if (file.exists() && file.length() > 0) {
                allPlaylists = mapper.readValue(file, new TypeReference<List<Playlist>>() {});
            } else {
                allPlaylists = new ArrayList<>();
                saveToJSON(); // Tạo file rỗng chuẩn mảng []
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc playlists.json: " + e.getMessage());
            allPlaylists = new ArrayList<>(); // Fallback để không crash app
        }
    }

    public void saveToJSON() {
        try {
            // Dùng writerWithDefaultPrettyPrinter để file json dễ đọc (xuống dòng, thụt lề)
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), allPlaylists);
            System.out.println(" Đã lưu playlists.json thành công.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Playlist> getAllPlaylists() {
        return allPlaylists;
    }
    
    public void addPlaylist(Playlist p) {
        if (allPlaylists == null) allPlaylists = new ArrayList<>();
        allPlaylists.add(p);
        saveToJSON();
    }
}