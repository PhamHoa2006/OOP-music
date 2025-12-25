package com.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static UserManager instance;
    private Map<String, User> users;
    private static final String USER_FILE_PATH = "data/users.json";
    private User currentUser = null;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    private UserManager() {
        users = new HashMap<>();
        loadFromJSON(); 
    }

    public static UserManager getInstance() {
        if (instance == null) instance = new UserManager();
        return instance;
    }

    public User findUser(String username) {
        if (username == null) return null;
        return users.get(username.toLowerCase());
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.length() < 6) return false;

        String key = username.toLowerCase();
        if (users.containsKey(key)) return false;

        User newUser = new User(username, password);
        users.put(key, newUser);
        
        // Lưu ngay sau khi đăng ký
        saveToJSON(); 
        return true;
    }

    public User login(String username, String password) {
        if (username == null || password == null) return null;
        String key = username.toLowerCase();
        User user = users.get(key);
        
        if (user == null) {
            System.out.println("❌ User không tồn tại: " + username);
            return null;
        }
        
        String inputHash = User.hashPassword(password);
        if (user.getPasswordHash().equals(inputHash)) {
            this.currentUser = user;
            System.out.println("🔓 Đăng nhập thành công: " + username);
            return user; 
        }
        
        System.out.println("❌ Sai mật khẩu!");
        return null;
    }
    
    public void saveToJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(USER_FILE_PATH);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            
            // Ghi đè file cũ
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users.values());
            System.out.println("💾 [UserManager] Đã lưu " + users.size() + " users vào ổ cứng.");
        } catch (IOException e) {
            System.err.println("❌ Lỗi CRITICAL: Không thể lưu file users.json! " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Trong file UserManager.java

    public void loadFromJSON() {
        File file = new File(USER_FILE_PATH);
        if (!file.exists()) {
            System.out.println("⚠️ File users.json chưa tồn tại. Sẽ tạo mới khi đăng ký.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            // Đọc mảng User[]
            User[] userList = mapper.readValue(file, User[].class);
            users.clear();
            for (User u : userList) {
                if (u.getUsername() != null) {
                    // Key lưu lowercase để login không phân biệt hoa thường
                    users.put(u.getUsername().toLowerCase(), u);
                }
            }
            System.out.println("📂 [UserManager] Đã load thành công " + users.size() + " users.");
        } catch (IOException e) {
            System.err.println("❌ Lỗi ĐỌC file users.json! File có thể bị hỏng hoặc sai cấu trúc.");
            e.printStackTrace(); // In lỗi đầy đủ ra để debug
            
            // [FIX] Nếu lỗi format, đổi tên file cũ để backup và reset data
            File backup = new File(USER_FILE_PATH + ".bak");
            if(file.renameTo(backup)) {
                System.out.println("👉 Đã backup file lỗi thành users.json.bak. Dữ liệu sẽ được reset.");
            }
            users.clear(); 
        }
    }
}