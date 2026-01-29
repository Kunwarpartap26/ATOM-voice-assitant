package org.vosk.demo;

public class ParsedIntent {
    private String action;
    private String parameter;
    private String target;
    private String targetApp;
    private float confidence;  // ADDED: For VoskActivity compatibility
    
    // Full constructor
    public ParsedIntent(String action, String parameter, String target, String targetApp, float confidence) {
        this.action = action;
        this.parameter = parameter;
        this.target = target;
        this.targetApp = targetApp;
        this.confidence = confidence;
    }
    
    // Backward compatibility constructors
    public ParsedIntent(String action, String parameter, String target, String targetApp) {
        this(action, parameter, target, targetApp, 0.8f);
    }
    
    public ParsedIntent(String action, String parameter, String target) {
        this(action, parameter, target, null, 0.8f);
    }
    
    public String getAction() {
        return action;
    }
    
    public String getParameter() {
        return parameter;
    }
    
    public String getTarget() {
        return target;
    }
    
    public String getTargetApp() {
        return targetApp;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public boolean hasTargetApp() {
        return targetApp != null && !targetApp.isEmpty();
    }
}
