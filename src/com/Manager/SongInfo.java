package com.Manager;

public class SongInfo {
    String name;
    String artist;
    String album;
    int duaration;
    String filePath;

    public SongInfo(String name, String artist, String album, int duaration, String filePath) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.duaration = duaration;
        this.filePath = filePath;
    }

    String getName() {return name;}
    void setName(String name) {this.name = name;}

    String getArtist() {return artist;}
    void setArtist(String artist) {this.artist = artist;}

    String getAlbum() {return album;}
    void setAlbum(String album) {this.album = album;}

    int getDuaration() {return duaration;}
    void setDuaration(int duaration) {this.duaration = duaration;}

    String getFilePath() {return filePath;}
    void setFilePath(String filePath) {this.filePath = filePath;}
}
