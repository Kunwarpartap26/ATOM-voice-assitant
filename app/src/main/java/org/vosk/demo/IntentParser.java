package org.vosk.demo;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class IntentParser {
    private static final String TAG = "INTENT_PARSER";
    
    private static final Map<String, String> APP_SYNONYMS = new HashMap<>();
    private static final Map<String, String> COMMAND_VARIATIONS = new HashMap<>();
    
    static {
        APP_SYNONYMS.put("insta", "instagram");
        APP_SYNONYMS.put("instagram", "instagram");
        APP_SYNONYMS.put("the gram", "instagram");
        
        COMMAND_VARIATIONS.put("hop on", "open");
        COMMAND_VARIATIONS.put("hop in", "open");
        COMMAND_VARIATIONS.put("hopping", "open");
        COMMAND_VARIATIONS.put("launch", "open");
        COMMAND_VARIATIONS.put("start", "open");
        COMMAND_VARIATIONS.put("go to", "open");
    }
    
    public static ParsedIntent parse(String voiceInput) {
        if (voiceInput == null || voiceInput.trim().isEmpty()) {
            return new ParsedIntent("unknown", null, null, null, 0.0f);
        }
        
        String lower = voiceInput.toLowerCase().trim();
        Log.d(TAG, "Parsing input: " + lower);
        
        lower = normalizeInput(lower);
        Log.d(TAG, "Normalized: " + lower);
        
        // Scroll commands
        if (containsAny(lower, new String[]{"scroll down", "scroll bottom", "move down"})) {
            return new ParsedIntent("scroll", "down", null, null, 0.9f);
        }
        if (containsAny(lower, new String[]{"scroll up", "scroll top", "move up"})) {
            return new ParsedIntent("scroll", "up", null, null, 0.9f);
        }
        
        // Like post commands
        if (containsAny(lower, new String[]{"like this post", "like the post", "like post", "press like", "hit like"})) {
            return new ParsedIntent("like_post", null, null, null, 0.9f);
        }
        
        // Navigation commands
        if (containsAny(lower, new String[]{"go back", "go backwards", "previous screen", "press back"})) {
            return new ParsedIntent("go_back", null, null, null, 0.9f);
        }
        if (containsAny(lower, new String[]{"go home", "go to home", "home screen", "press home"})) {
            return new ParsedIntent("go_home", null, null, null, 0.9f);
        }
        
        // Open app commands
        if (lower.startsWith("open ")) {
            String app = lower.substring(5).trim();
            String normalizedApp = normalizeAppName(app);
            return new ParsedIntent("open_app", null, normalizedApp, null, 0.9f);
        }
        
        // Handle "hop on instagram"
        if (lower.contains("hop on ") || lower.contains("hop in ")) {
            String app = lower.replaceAll(".*hop (on|in) ", "").trim();
            String normalizedApp = normalizeAppName(app);
            return new ParsedIntent("open_app", null, normalizedApp, null, 0.8f);
        }
        
        // Handle "launch instagram"
        if (lower.startsWith("launch ") || lower.startsWith("start ")) {
            String app = lower.replaceAll("^(launch|start) ", "").trim();
            String normalizedApp = normalizeAppName(app);
            return new ParsedIntent("open_app", null, normalizedApp, null, 0.9f);
        }
        
        // Instagram shortcuts
        if (lower.contains("instagram") || lower.contains("insta")) {
            if (lower.contains("open") || lower.contains("launch") || lower.contains("start")) {
                return new ParsedIntent("open_app", null, "instagram", null, 0.8f);
            }
        }
        
        Log.d(TAG, "Unknown command: " + lower);
        return new ParsedIntent("unknown", null, null, null, 0.0f);
    }
    
    private static String normalizeInput(String input) {
        String result = input;
        for (Map.Entry<String, String> entry : COMMAND_VARIATIONS.entrySet()) {
            if (result.contains(entry.getKey())) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private static String normalizeAppName(String appName) {
        String normalized = appName.toLowerCase().trim();
        normalized = normalized.replace("the ", "").replace("app ", "").trim();
        
        for (Map.Entry<String, String> entry : APP_SYNONYMS.entrySet()) {
            if (normalized.equals(entry.getKey()) || normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return normalized;
    }
    
    private static boolean containsAny(String text, String[] patterns) {
        for (String pattern : patterns) {
            if (text.contains(pattern)) return true;
        }
        return false;
    }
}
