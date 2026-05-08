package com.example.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashMap;
import java.util.Map;

public class MyAccessibilityService extends AccessibilityService {
    private final Map<String, BlockerStrategy> strategies = new HashMap<>();

    public MyAccessibilityService() {
        super();
        android.util.Log.i("Blocker", "SERVICE CONSTRUCTOR CALLED!");
    }

    @Override
    protected void onServiceConnected() {
        // Perform initialization here
        super.onServiceConnected();

        // Registering strategies
        strategies.put("com.instagram.android", new InstagramStrategy());
        // Сюди потім додаси YouTubeStrategy

        Log.d("Blocker", "Service Connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";

        SharedPreferences prefs = getSharedPreferences("BlockerPrefs", MODE_PRIVATE);

        if (packageName.equals("com.instagram.android")) {
            boolean isEnabled = prefs.getBoolean("block_instagram", false);
            if (isEnabled) {
                BlockerStrategy strategy = strategies.get(packageName);
                if (strategy != null) {
                    strategy.execute(event, this);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Interrupt any ongoing feedback
    }
}
