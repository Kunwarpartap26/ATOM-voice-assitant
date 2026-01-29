package org.vosk.demo.agents;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class InstagramAgent {
    private static final String TAG = "INSTAGRAM_AGENT";
    private static final int MAX_DEPTH = 10;
    
    public static class LikeButtonInfo {
        public final int x;
        public final int y;
        public final String contentDesc;
        
        public LikeButtonInfo(int x, int y, String contentDesc) {
            this.x = x;
            this.y = y;
            this.contentDesc = contentDesc;
        }
    }
    
    public static class ScreenSkeleton {
        public boolean isInstagramFocused = false;
        public final List<LikeButtonInfo> likeButtons = new ArrayList<>();
        public String errorMessage = null;
    }
    
    public static ScreenSkeleton captureScreenSkeleton(AccessibilityService service) {
        ScreenSkeleton skeleton = new ScreenSkeleton();
        
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            skeleton.errorMessage = "Cannot access screen content. Check accessibility permissions.";
            return skeleton;
        }
        
        try {
            CharSequence pkgName = rootNode.getPackageName();
            if (pkgName == null || !pkgName.toString().toLowerCase().contains("instagram")) {
                skeleton.errorMessage = "Instagram not in focus. Current app: " + 
                    (pkgName != null ? pkgName.toString() : "unknown");
                return skeleton;
            }
            
            skeleton.isInstagramFocused = true;
            findLikeButtonsRecursive(rootNode, skeleton.likeButtons, 0);
            
            Log.d(TAG, "Scan complete. Found " + skeleton.likeButtons.size() + " like buttons");
            
        } finally {
            rootNode.recycle();
        }
        
        return skeleton;
    }
    
    private static void findLikeButtonsRecursive(AccessibilityNodeInfo node, 
                                                  List<LikeButtonInfo> buttons, 
                                                  int depth) {
        if (node == null || depth > MAX_DEPTH) {
            return;
        }
        
        try {
            CharSequence desc = node.getContentDescription();
            if (desc != null) {
                String descStr = desc.toString().toLowerCase();
                // Instagram like buttons have "like" but not "unlike" in content description
                if ((descStr.contains("like") && !descStr.contains("unlike")) ||
                    (descStr.equals("like"))) {
                    
                    Rect bounds = new Rect();
                    node.getBoundsInScreen(bounds);
                    
                    if (bounds.width() > 0 && bounds.height() > 0) {
                        int centerX = bounds.centerX();
                        int centerY = bounds.centerY();
                        buttons.add(new LikeButtonInfo(centerX, centerY, desc.toString()));
                        Log.d(TAG, "Found like button at (" + centerX + ", " + centerY + 
                              ") desc: " + desc.toString());
                    }
                }
            }
            
            // Recurse into children
            int childCount = node.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                findLikeButtonsRecursive(child, buttons, depth + 1);
                if (child != null) {
                    child.recycle();
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error scanning node: " + e.getMessage());
        }
    }
}
