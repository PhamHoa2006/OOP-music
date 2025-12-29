package com.users;

import com.musicPlayer.Song;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationEngine {

    // Gợi ý từ lịch sử nghe cá nhân
    public List<Song> suggestBasedOnMostPlayed(History history, int numberOfSuggestions) {
        if (numberOfSuggestions <= 0 || history == null) {
            return new ArrayList<>();
        }
        return history.suggestMostPlayed(numberOfSuggestions);
    }

    // Gợi ý từ xu hướng toàn cục
    public List<Song> suggestBasedOnGlobalTrending(int numberOfSuggestions) {
        if (numberOfSuggestions <= 0) {
            return new ArrayList<>();
        }
        return History.getGlobalTrending(numberOfSuggestions);     
    }
    
    // TỔNG HỢP: Lấy danh sách n bài hát gợi ý (Mix cả 2 nguồn)
    public List<Song> getListOfSuggestedSongs(History history, int n) {
        if (n <= 0) return new ArrayList<>(); // Fix lỗi crash nếu n=0

        Set<Song> uniqueSuggestions = new HashSet<>();
        Random rand = new Random();

        //Lấy khoảng 40-60% là nhạc Trending
        int trendingCount = (n > 1) ? rand.nextInt(n / 2) + 1 : 1; 
        uniqueSuggestions.addAll(suggestBasedOnGlobalTrending(trendingCount));

        // Còn lại lấp đầy bằng nhạc User hay nghe
        if (history != null) {
            int remaining = n - uniqueSuggestions.size();
            if (remaining > 0) {
                uniqueSuggestions.addAll(suggestBasedOnMostPlayed(history, remaining + 2)); 
                // Lấy dư ra (+2) đề phòng trường hợp bị trùng lặp nhiều
            }
        }

        //Chuyển về List và trộn
        List<Song> finalResult = new ArrayList<>(uniqueSuggestions);
        Collections.shuffle(finalResult);

        // Cắt đúng n bài (vì bước trên có thể lấy dư)
        if (finalResult.size() > n) {
            return finalResult.subList(0, n);
        }

        return finalResult;
    }
    
    // --- [BONUS] Tính năng nâng cao: Gợi ý theo thể loại (Genre) ---
    
    public List<Song> suggestByGenre(SongLibrary library, String genre, int n) {
        List<Song> allSongs = library.getAllSongs();
        return allSongs.stream()
                .filter(s -> s.getGenres().contains(genre))
                .limit(n)
                .collect(Collectors.toList());
    }
}