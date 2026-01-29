package org.vosk.demo.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import org.vosk.demo.tetra.TetraController;

public class VoiceAccessibilityService extends AccessibilityService {
    private static final String TAG = "VOICE_ACCESSIBILITY";
    private static VoiceAccessibilityService instance;
    private TetraController tetraController;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Service created");
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility Service Connected");
        
        // CRITICAL: Initialize TetraController singleton
        tetraController = new TetraController(this);
        TetraController.setInstance(tetraController);
        
        Log.d(TAG, "TetraController initialized and singleton set");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Handle accessibility events if needed
        // Currently handled via TetraController directly
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "Service destroyed");
    }

    public static VoiceAccessibilityService getInstance() {
        return instance;
    }
    
    public TetraController getTetraController() {
        return tetraController;
    }
}
