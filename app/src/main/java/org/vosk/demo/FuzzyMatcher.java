package org.vosk.demo;

public class FuzzyMatcher {
    
    // Levenshtein distance
    public static int distance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
            
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                    s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }
    
    public static boolean fuzzyMatch(String text, String target, int maxDistance) {
        return distance(text, target) <= maxDistance;
    }
    
    public static boolean containsFuzzy(String text, String target, int maxDistance) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (distance(word, target) <= maxDistance) {
                return true;
            }
        }
        return false;
    }
}
