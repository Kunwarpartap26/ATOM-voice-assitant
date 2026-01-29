package org.vosk.demo.tetra;

import java.util.HashMap;
import java.util.Map;

public class AppPackageMapper {
    
    private static final Map<String, String> APP_PACKAGES = new HashMap<>();
    
    static {
        // Social Media
        APP_PACKAGES.put("instagram", "com.instagram.android");
        APP_PACKAGES.put("whatsapp", "com.whatsapp");
        APP_PACKAGES.put("facebook", "com.facebook.katana");
        APP_PACKAGES.put("twitter", "com.twitter.android");
        APP_PACKAGES.put("snapchat", "com.snapchat.android");
        
        // Google Apps
        APP_PACKAGES.put("chrome", "com.android.chrome");
        APP_PACKAGES.put("youtube", "com.google.android.youtube");
        APP_PACKAGES.put("gmail", "com.google.android.gm");
        APP_PACKAGES.put("maps", "com.google.android.apps.maps");
        APP_PACKAGES.put("photos", "com.google.android.apps.photos");
        
        // Utilities
        APP_PACKAGES.put("camera", "com.android.camera2");
        APP_PACKAGES.put("gallery", "com.android.gallery3d");
        APP_PACKAGES.put("settings", "com.android.settings");
    }
    
    public static String getPackage(String appName) {
        if (appName == null) return null;
        return APP_PACKAGES.get(appName.toLowerCase());
    }
}
