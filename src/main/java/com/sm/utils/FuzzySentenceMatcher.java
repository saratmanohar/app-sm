package com.sm.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FuzzySentenceMatcher {

    // Threshold for word similarity (0.70 means words must be at least 70% similar)
    private static final double SIMILARITY_THRESHOLD = 0.70;

    public static int getFuzzyMatchPercentage(String sentence1, String sentence2) {
        if (sentence1 == null || sentence2 == null || sentence1.trim().isEmpty() || sentence2.trim().isEmpty()) {
            return 0;
        }

        // Clean text: lowercase and remove punctuation
        String clean1 = sentence1.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
        String clean2 = sentence2.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");

        // Tokenize into unique word sets
        Set<String> words1 = new HashSet<>(Arrays.asList(clean1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(clean2.split("\\s+")));

        int commonWordsCount = 0;
        Set<String> usedFromWords2 = new HashSet<>();

        // Perform fuzzy matching between the two sets
        for (String w1 : words1) {
            for (String w2 : words2) {
                // Skip if this word from sentence 2 was already matched
                if (usedFromWords2.contains(w2)) {
                    continue;
                }

                if (isFuzzyMatch(w1, w2)) {
                    commonWordsCount++;
                    usedFromWords2.add(w2); // Mark as matched to avoid double counting
                    break; 
                }
            }
        }

        // Total unique words concept for Jaccard-like denominator in fuzzy context
        int totalUniqueWords = words1.size() + words2.size() - commonWordsCount;

        if (totalUniqueWords == 0) {
            return 0;
        }

        double percentage = ((double) commonWordsCount / totalUniqueWords) * 100;
        return (int) Math.round(percentage);
    }

    // Helper to check if two words are a fuzzy match using Levenshtein Distance
    private static boolean isFuzzyMatch(String w1, String w2) {
        if (w1.equals(w2)) return true;

        int distance = getLevenshteinDistance(w1, w2);
        int maxLength = Math.max(w1.length(), w2.length());

        // Calculate similarity score between 0.0 and 1.0
        double similarity = 1.0 - ((double) distance / maxLength);
        return similarity >= SIMILARITY_THRESHOLD;
    }

    // Standard Levenshtein Distance implementation
    private static int getLevenshteinDistance(String s1, String s2) {
        int[] dp = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int temp = dp[j];
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = Math.min(Math.min(dp[j - 1], dp[j]), prev) + 1;
                }
                prev = temp;
            }
        }
        return dp[s2.length()];
    }
}
