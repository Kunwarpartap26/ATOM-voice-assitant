package org.vosk.demo.tetra;

public class TetraResult {
    private boolean success;
    private String message;
    
    private TetraResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public static TetraResult success(String message) {
        return new TetraResult(true, message);
    }
    
    public static TetraResult failure(String message) {
        return new TetraResult(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
}
