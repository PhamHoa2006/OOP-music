package com.users; // Đặt đúng package com.users

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicPlayer.Song; // Import lớp Song từ package khác

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongLibrary {
    // Singleton
    private static SongLibrary instance;
    private List<Song> allSongs;
    
    // Đường dẫn file JSON (Tương đối từ gốc dự án)
    private static final String LIBRARY_FILE_PATH = "data/library.json";

    private SongLibrary() {
        allSongs = new ArrayList<>();
        loadFromJSON(); // Tự động tải khi khởi tạo
    }

    public static SongLibrary getInstance() {
        if (instance == null) {
            instance = new SongLibrary();
        }
        return instance;
    }

    // Thêm bài hát vào kho
    public void addSong(Song song) {
        // Kiểm tra trùng lặp (dựa trên URL/Path)
        for (Song s : allSongs) {
            if (s.getUrl().equals(song.getUrl())) {
                System.out.println("Bài hát đã tồn tại: " + song.getTitle());
                return;
            }
        }
        allSongs.add(song);
        saveToJSON(); // Lưu ngay
        System.out.println("Đã thêm vào thư viện: " + song.getTitle());
    }

    public List<Song> getAllSongs() {
        return allSongs;
    }

    // Lưu danh sách ra file JSON
    public void saveToJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(LIBRARY_FILE_PATH);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs(); // Tạo thư mục data nếu chưa có
            }
            // Ghi file (format đẹp)
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, allSongs);
        } catch (IOException e) {
            System.err.println("Lỗi lưu library.json: " + e.getMessage());
        }
    }

    // Đọc danh sách từ file JSON
    public void loadFromJSON() {
        File file = new File(LIBRARY_FILE_PATH);
        if (!file.exists()) return;

        ObjectMapper mapper = new ObjectMapper();
        try {
            allSongs = mapper.readValue(file, new TypeReference<List<Song>>() {});
            System.out.println("Đã tải " + allSongs.size() + " bài hát từ thư viện.");
        } catch (IOException e) {
            System.err.println("Lỗi đọc library.json: " + e.getMessage());
        }
    }
}