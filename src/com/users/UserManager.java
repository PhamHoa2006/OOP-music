package com.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    // Singleton pattern (Nên dùng 1 instance duy nhất cho toàn app)
    private static UserManager instance;
    
    // Map lưu user
    private Map<String, User> users;
    
    private static final String USER_FILE_PATH = "data/users.json";

    private UserManager() {
        users = new HashMap<>();
        loadFromJSON(); // Tự động load khi khởi tạo
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public User findUser(String username) {
        if (username == null) return null;
        return users.get(username.toLowerCase());
    }

    // Đăng ký
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username không hợp lệ");
            return false;
        }
        if (password == null || password.length() < 6) {
            System.out.println("Password quá ngắn (<6 ký tự)");
            return false;
        }

        String key = username.toLowerCase();
        if (users.containsKey(key)) {
            System.out.println("Tên người dùng đã tồn tại");
            return false;
        }

        User newUser = new User(username, password);
        users.put(key, newUser);
        saveToJSON(); // Lưu ngay sau khi đăng ký
        System.out.println("Đăng ký thành công: " + username);
        return true;
    }

    // Đăng nhập: Trả về User object thay vì boolean
    public User login(String username, String password) {
        if (username == null || password == null) return null;

        User user = findUser(username);
        if (user != null && user.getPasswordHash().equals(User.hashPassword(password))) {
            System.out.println("Đăng nhập thành công: " + username);
            return user; // Trả về user session
        }
        
        System.out.println("Sai tên đăng nhập hoặc mật khẩu");
        return null;
    }
    
    // Lưu ra file JSON
    public void saveToJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(USER_FILE_PATH);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            
            // Convert Map values to List to save
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users.values());
        } catch (IOException e) {
            System.err.println("Lỗi lưu users.json: " + e.getMessage());
        }
    }

    // Load từ file JSON
    public void loadFromJSON() {
        File file = new File(USER_FILE_PATH);
        if (!file.exists()) return;

        ObjectMapper mapper = new ObjectMapper();
        try {
            // Đọc list user xong map ngược lại vào HashMap
            User[] userList = mapper.readValue(file, User[].class);
            users.clear();
            for (User u : userList) {
                users.put(u.getUsername().toLowerCase(), u);
            }
            System.out.println("Đã load " + users.size() + " users.");
        } catch (IOException e) {
            System.err.println("Lỗi đọc users.json: " + e.getMessage());
        }
    }
}
