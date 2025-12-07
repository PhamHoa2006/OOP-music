package com.users;

import com.musicPlayer.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

public class User {

    private String id;
    private String name;
    private int age;
    private String email;
    private String description;
    private ArrayList<Playlist> playLists = new ArrayList<>();


    private String username;
    private String passwordHash;

    private History history = new History();



    private User(String username, String password) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.id = UUID.randomUUID().toString();
    }
    static User create(String username, String password){
        return new User(username, password);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }



    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }



    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }



    public String getDescripsion() {
        return description;
    }
    public void setDescripsion(String description) {
        this.description = description;
    }

    public ArrayList<Playlist> getPlayLists() {
        return playLists;
    }
    public void setPlayLists(ArrayList<Playlist> playLists) {
        this.playLists = playLists;
    }


    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return passwordHash;
    }
    public void setPassword(String password){
        this.passwordHash = hashPassword(password);
    }
    

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

    public History getHistory() {
        return history;
    }


}
