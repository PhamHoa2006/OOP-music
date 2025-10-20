package com.users;

import com.musicPlayer.*;
import java.util.ArrayList;
import java.util.UUID;

public class User {

    private String id;
    private String name;
    private String age;
    private String email;
    private String decripsion;
    private ArrayList<Playlist> playLists = new ArrayList<>();


    private String username;
    private String password;



    private User(String username, String password) {
        this.username = username;
        this.password = password;
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



    public String getAge() {
        return age;
    }
    public void setAge(String age) {
        this.age = age;
    }



    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }



    public String getDecripsion() {
        return decripsion;
    }
    public void setDecripsion(String decripsion) {
        this.decripsion = decripsion;
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
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    

    
}
