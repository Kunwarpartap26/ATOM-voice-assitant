package org.vosk.demo;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.json.JSONObject;

public class VoskRecognizer {
    private Model model;
    private static final float SAMPLE_RATE = 16000.0f;

    public VoskRecognizer(Model model) {
        this.model = model;
    }

    public RecognitionResult recognize(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return new RecognitionResult("", 0.0f, "No audio data");
        }

        try {
            Recognizer recognizer = new Recognizer(model, SAMPLE_RATE);
            
            // Process complete audio in one call
            recognizer.acceptWaveForm(audioData, audioData.length);
            
            // Get final result
            String resultJson = recognizer.getFinalResult();
            recognizer.close();
            
            // Parse JSON result
            JSONObject jsonResult = new JSONObject(resultJson);
            String text = jsonResult.optString("text", "");
            
            // Calculate confidence from JSON if available
            float confidence = calculateConfidence(jsonResult, text);
            
            return new RecognitionResult(text, confidence, resultJson);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new RecognitionResult("", 0.0f, "Recognition error: " + e.getMessage());
        }
    }

    private float calculateConfidence(JSONObject result, String text) {
        try {
            // Try to get confidence from result
            if (result.has("result")) {
                // Word-level confidence if available
                return 0.85f; // Placeholder - actual calculation would average word confidences
            }
            
            // Heuristic: longer text = higher confidence (for Vosk without word scores)
            if (text.isEmpty()) return 0.0f;
            if (text.length() < 5) return 0.4f;
            if (text.length() < 15) return 0.6f;
            return 0.8f;
            
        } catch (Exception e) {
            return 0.5f;
        }
    }
}

class RecognitionResult {
    private String text;
    private float confidence;
    private String rawJson;

    public RecognitionResult(String text, float confidence, String rawJson) {
        this.text = text;
        this.confidence = confidence;
        this.rawJson = rawJson;
    }

    public String getText() { return text; }
    public float getConfidence() { return confidence; }
    public String getRawJson() { return rawJson; }
}
