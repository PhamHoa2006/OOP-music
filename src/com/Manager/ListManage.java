package com.Manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListManage {

    // ================= ABSTRACT LIST ====================
    public static abstract class AbstractList {
        protected List<SongInfo> songs;

        public AbstractList() {
            this.songs = new ArrayList<>();
        }

        public void addSong(SongInfo song) {
            songs.add(song);
        }

        public void removeSong(SongInfo song) {
            songs.remove(song);
        }

        public int size() {
            return songs.size();
        }

        public abstract List<SongInfo> getAllSongs();
    }

    // ================= HISTORY LIST (max 20) ====================
    public static class HistoryList extends AbstractList {

        public HistoryList() {
            this.songs = new LinkedList<>(); // queue behavior
        }

        @Override
        public void addSong(SongInfo song) {
            // Nếu bài đã có → xóa để đưa lên đầu
            songs.remove(song);

            // Thêm bài vào cuối (vị trí mới nhất)
            songs.add(song);

            // Nếu quá 20 bài → xoá bài cũ nhất
            if (songs.size() > 20) {
                songs.remove(0);
            }
        }

        @Override
        public List<SongInfo> getAllSongs() {
            return songs;
        }
    }

    // ================= FAVOURITE LIST ====================
    public static class FavouriteList extends AbstractList {

        @Override
        public void addSong(SongInfo song) {
            if (!songs.contains(song)) {
                songs.add(song);
            }
        }

        @Override
        public List<SongInfo> getAllSongs() {
            return songs;
        }
    }

    // ================= PLAYLIST ====================
    public class PlaylistList extends AbstractList {

        private String name;

        public PlaylistList(String name) {
            this.name = name;
            this.songs = new ArrayList<>();
        }

        public String getName() {return name;}
        public void setName(String name) {this.name = name;}

        @Override
        public List<SongInfo> getAllSongs() {
            return songs;
        }
    }

    // ================= MANAGE PLAYLIST ====================
    private static List<PlaylistList> playlists = new ArrayList<>();

    public PlaylistList createPlaylist(String name) {
        PlaylistList pl = new PlaylistList(name);
        playlists.add(pl);
        return pl;
    }

    public void deletePlaylist(PlaylistList playlist) {
        playlists.remove(playlist);
    }

    public List<PlaylistList> getAllPlaylists() {
        return playlists;
    }
}
