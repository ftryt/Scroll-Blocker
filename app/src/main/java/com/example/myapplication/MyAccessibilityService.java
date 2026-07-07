package com.example.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.myapplication.strategies.BlockerStrategy;
import com.example.myapplication.strategies.InstagramStrategy;
import com.example.myapplication.strategies.TikTokStrategy;
import com.example.myapplication.strategies.YouTubeStrategy;

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
        strategies.put("com.google.android.youtube", new YouTubeStrategy());
        strategies.put("com.zhiliaoapp.musically", new TikTokStrategy());

        Log.d("Blocker", "Service Connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";

        // Log.d("Blocker", "Package name: " + packageName);

        SharedPreferences prefs = getSharedPreferences("BlockerPrefs", MODE_PRIVATE);

        if (strategies.containsKey(packageName)){
            boolean isEnabled = prefs.getBoolean(packageName, false);

            // Log.d("Blocker", "Strategy found: " + packageName + " boolean position: " + isEnabled);

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
