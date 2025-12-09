package com.users;
import java.util.HashMap;

public class UserManager {
    // key: username (chuyển về chữ thường để tránh trùng lặp)
    private static HashMap<String, User> users = new HashMap<>();

    // Tìm kiếm user
    public User findUser(String username) {
        if (username == null) return null;
        return users.get(username.toLowerCase());
    }

    // Đăng ký (register)
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username khong hop le");
            return false;
        }
        if (password == null || password.length() < 6 || password.length() > 30) {
            System.out.println("Password không hợp lệ (>= 6 ký tự và <= 30)");
            return false;
        }
        String key = username.toLowerCase();
        if (users.containsKey(key)) {
            System.out.println("Ten nguoi dung da ton tai");
            return false;
        }
        users.put(key, User.create(username, password));
        System.out.println("Dang ky thanh cong");
        return true;
    }

    // Đăng nhập (login)
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            System.out.println("Ten dang nhap hoac mat khau khong hop le");
            return false;
        }
        User tmp = findUser(username);
        if (tmp == null || !tmp.getPassword().equals(User.hashPassword(password))) {
            System.out.println("Ten dang nhap hoac mat khau khong hop le");
            return false;
        }
        System.out.println("Dang nhap thanh cong");
        return true;
    }

    // Thay đổi mật khẩu
    public boolean changePassword(String username, String oldPwd, String newPwd) {
        User tmp = findUser(username);
        if (tmp == null) {
            System.out.println("Nham tai khoan r");
            return false;
        }
        if (!tmp.getPassword().equals(User.hashPassword(oldPwd))) {
            System.out.println("Nham mat khau r");
            return false;
        }
        tmp.setPassword(newPwd);
        System.out.println("Doi mat khau thanh cong");
        return true;
    }

    // Xóa user
    public boolean delete(String username, String password) {
        User tmp = findUser(username);
        if (tmp == null || !tmp.getPassword().equals(User.hashPassword(password))) {
            return false;
        }
        users.remove(username.toLowerCase());
        System.out.println("Xoa thanh cong");
        return true;
    }

    // Số lượng user
    public int size() {
        return users.size();
    }

    // In danh sách user
    public void listUsers() {
        if (users.isEmpty()) {
            System.out.println("Chua co nguoi dung nao");
            return;
        }
        System.out.println("Danh sach User:");
        for (User u : users.values()) {
            System.out.println("- " + u.getUsername());
        }
    }
}
