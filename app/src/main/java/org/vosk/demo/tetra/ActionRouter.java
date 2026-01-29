package org.vosk.demo.tetra;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ActionRouter {
    private static final String TAG = "ROUTER";
    private static final int DURATION_TAP = 100;
    private static final int DURATION_SCROLL = 400;
    
    private final AccessibilityService service;
    private final Handler handler;
    
    public ActionRouter(AccessibilityService service) {
        this.service = service;
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void launchApp(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.e(TAG, "Cannot launch null or empty package");
            return;
        }
        
        try {
            Intent intent = service.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                service.startActivity(intent);
                Log.d(TAG, "Launched app: " + packageName);
            } else {
                Log.e(TAG, "No launch intent found for: " + packageName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error launching app: " + e.getMessage());
        }
    }
    
    public void performGlobalBack() {
        handler.post(() -> {
            boolean result = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            Log.d(TAG, "Global back performed: " + result);
        });
    }
    
    public void performGlobalHome() {
        handler.post(() -> {
            boolean result = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            Log.d(TAG, "Global home performed: " + result);
        });
    }
    
    public void performClick(final int x, final int y) {
        handler.post(() -> {
            try {
                Path clickPath = new Path();
                clickPath.moveTo(x, y);
                
                GestureDescription.Builder builder = new GestureDescription.Builder();
                builder.addStroke(new GestureDescription.StrokeDescription(
                    clickPath, 0, DURATION_TAP));
                
                boolean dispatched = service.dispatchGesture(builder.build(), 
                    new AccessibilityService.GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            Log.d(TAG, "Click completed at (" + x + ", " + y + ")");
                            super.onCompleted(gestureDescription);
                        }
                        
                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            Log.w(TAG, "Click cancelled at (" + x + ", " + y + ")");
                            super.onCancelled(gestureDescription);
                        }
                    }, null);
                
                if (!dispatched) {
                    Log.e(TAG, "Failed to dispatch click gesture");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in performClick: " + e.getMessage());
            }
        });
    }
    
    public void scrollScreen(final String direction) {
        handler.post(() -> {
            try {
                Path scrollPath = new Path();
                android.util.DisplayMetrics metrics = service.getResources().getDisplayMetrics();
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                
                int startX = width / 2;
                int startY = height / 2;
                int endX = width / 2;
                int endY = height / 2;
                
                String dir = direction != null ? direction.toLowerCase() : "down";
                
                switch (dir) {
                    case "down":
                    case "bottom":
                        startY = (int)(height * 0.8);
                        endY = (int)(height * 0.2);
                        break;
                    case "up":
                    case "top":
                        startY = (int)(height * 0.2);
                        endY = (int)(height * 0.8);
                        break;
                    case "left":
                        startX = (int)(width * 0.8);
                        endX = (int)(width * 0.2);
                        break;
                    case "right":
                        startX = (int)(width * 0.2);
                        endX = (int)(width * 0.8);
                        break;
                    default:
                        Log.w(TAG, "Unknown scroll direction: " + direction);
                        return;
                }
                
                scrollPath.moveTo(startX, startY);
                scrollPath.lineTo(endX, endY);
                
                GestureDescription.Builder builder = new GestureDescription.Builder();
                builder.addStroke(new GestureDescription.StrokeDescription(
                    scrollPath, 0, DURATION_SCROLL));
                
                boolean dispatched = service.dispatchGesture(builder.build(),
                    new AccessibilityService.GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            Log.d(TAG, "Scroll " + dir + " completed");
                            super.onCompleted(gestureDescription);
                        }
                        
                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            Log.w(TAG, "Scroll " + dir + " cancelled");
                            super.onCancelled(gestureDescription);
                        }
                    }, null);
                
                if (!dispatched) {
                    Log.e(TAG, "Failed to dispatch scroll gesture");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in scrollScreen: " + e.getMessage());
            }
        });
    }
}
