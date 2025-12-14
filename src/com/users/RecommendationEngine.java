package com.users;

import com.musicPlayer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

// Gợi ý một số bài hát cho người dùng
public class RecommendationEngine {

    // Gợi ý list bài hát user nghe nhiều nhất
    public List<Song> suggestBasedOnMostPlayed(History history, int numberOfSuggestions) {
        if (numberOfSuggestions <= 0) {
            return new ArrayList<>();
        }

        List<Song> mostPlayed = history.suggestMostPlayed(numberOfSuggestions);

        return mostPlayed;
    }

    // Gợi ý list bài hát mọi người nghe nhiều nhất
    public List<Song> suggestBasedOnGlobalTrending(int numberOfSuggestions) {
        if (numberOfSuggestions <= 0) {
            return new ArrayList<>();
        }

        List<Song> trendingMap = History.getGlobalTrending(numberOfSuggestions);    
        
        return trendingMap;
    }
    
    // Lấy danh sách n bài hát gợi ý (sắp xếp ngẫu nhiên)
    public List<Song> getListOfSuggestedSongs(History history, int n) {
        Random rand = new Random();
        int tmp = rand.nextInt(n) + 1;

        List<Song> suggestions = suggestBasedOnGlobalTrending(tmp);
        suggestions.addAll(suggestBasedOnMostPlayed(history, n - tmp));
        Collections.shuffle(suggestions);

        return suggestions;
    }
}
