package com.example.myapplication.strategies;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class YouTubeStrategy implements BlockerStrategy{
    private long lastChechTime = 0;
    private static final long CHECK_DELAY = 500; // Save your CPU

    @Override
    public void execute(AccessibilityEvent event, AccessibilityService service) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChechTime <= CHECK_DELAY) {
            return;
        }
        lastChechTime = currentTime;

        if (isInShorts(rootNode)){
            Log.d("Blocker", "Watching shorts, returning to main page... ");

            // Find home button
            var homeButton = findHomeButton(rootNode);
            if (homeButton != null && 1 == 0) { // TEMP
                Log.d("Blocker", "Pressing home!");
                pressHome(homeButton);
            } else {
                // If for some reason home button is inaccessible do back action
                Log.d("Blocker", "Home not found!");
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }

        }
    }

    private void pressHome(AccessibilityNodeInfo button) {
        button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private AccessibilityNodeInfo findHomeButton(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> buttons = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/text");
        for (AccessibilityNodeInfo button : buttons){
            CharSequence buttonText = button.getText();
            Log.d("Blocker", "Button: " + buttonText);
            if (button.isVisibleToUser() && "Home".contentEquals(buttonText))
                return button;
        }

        return null;
    }

    private boolean isInShorts(AccessibilityNodeInfo rootNode) {
        // com.google.android.youtube:id/reel_watch_player

        List<AccessibilityNodeInfo> shortsWindows = rootNode.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_watch_player");
        for (AccessibilityNodeInfo node : shortsWindows){
            if (node.isVisibleToUser())
                return true;
        }

        return false;
    }
}
