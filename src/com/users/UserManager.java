package com.users;
import java.util.ArrayList;

public class UserManager {
    private static ArrayList<User> users = new ArrayList<>();


    public User findUser(String username){
        for (User u : users){
            if (((u.getUsername()).toLowerCase()).equals(username.toLowerCase())) return u;
        }
        return null;
    }

    public boolean register(String username, String password){
        if (username == null || (username.trim()).isEmpty()){
            System.out.println("Username khong hop le");
            return false;
        }
        if (password == null || password.length() < 6 || password.length() > 30) {
            System.out.println("Password không hợp lệ (>= 6 ký tự và <= 30)");
            return false;
        }
        if (findUser(username) != null) {
            System.out.println("Ten nguoi dung da ton tai");
            return false;        
        }
        users.add(User.create(username,password));
        System.out.println("Dang ky thanh cong");
        return true;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null){
            System.out.println("Ten dang nhap hoac mat khau khong hop le");
            return false;
        }
        User tmp = findUser(username);
        if (tmp == null || !tmp.getPassword().equals(password)) {
            System.out.println("Ten dang nhap hoac mat khau khong hop le");
            return false;
        }
        System.out.println("Dang nhap thanh cong"); 
        
        return true;    
    }

    public boolean changePassword(String username, String oldPwd, String newPwd){
        User tmp = findUser(username);
        if (tmp == null) {
            System.out.println("Nham tai khoan r");
            return false;
        }
        if (!tmp.getPassword().equals(oldPwd)){
            System.out.println("Nham mat khau r");
            return false;
        }
        System.out.println("Doi mat khau thanh cong");
        tmp.setPassword(newPwd);
        return true;
    }

    public boolean delete(String username, String password){
        User tmpUser = findUser(username);
        if (tmpUser == null || !tmpUser.getPassword().equals(password)) return false;
        users.remove(tmpUser);
        return true;
    }

    public int size(){
        return users.size();
    }

    public void listUsers(){
        if (users.isEmpty()){
            System.out.println("Chua co nguoi dung nao");
            return;
        }
        System.out.println("Danh sach User: ");
        for (User tmp : users) System.out.println("- " + tmp.getUsername());
        return;
    }   
}   

