package com.example.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashSet;
import java.util.List;

public class InstagramStrategy implements BlockerStrategy {
    private long lastActionTime = 0;
    private static final long DEBOUNCE_DELAY = 500;
    private static final long SOFTLOCK_MAIN_PAGE = 5;

    private final HashSet<CharSequence> mainPageReelsSet;

    private boolean inChat = false;

    private CharSequence allowedRellDesc;

    public InstagramStrategy() {
        this.mainPageReelsSet = new HashSet<>();
    }

    @Override
    public void execute(AccessibilityEvent event, AccessibilityService service) {
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) return;

        // Allows watching video your friends sent, but no scrolling
        handleAllowedReel(rootNode);
        // Changes inChat
        chatStateDetection(rootNode);
        // Prevent entering Reels (hardlock), except that your friends sent
        handleReelsBlock(rootNode, service);
        // Prevent scrolling main page (softlock) with SOFTLOCK_MAIN_PAGE
        handleFeedBlock(rootNode, service);
    }

    private void handleFeedBlock(AccessibilityNodeInfo rootNode, AccessibilityService service) {
        // Prevent scrolling main page (softlock)
        List<AccessibilityNodeInfo> mainPageRells = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/row_feed_photo_imageview");

        for (AccessibilityNodeInfo reel : mainPageRells){
            mainPageReelsSet.add(reel.getContentDescription());
            Log.d("Blocker", mainPageReelsSet.size() + " Reel added to set: " + reel.getContentDescription());
        }

        if (mainPageReelsSet.size() >= SOFTLOCK_MAIN_PAGE){
            mainPageReelsSet.clear();
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            return;
        }
    }

    private void handleReelsBlock(AccessibilityNodeInfo rootNode, AccessibilityService service) {
        // Prevent entering Reels (hardlock), except that your friends sent
        List<AccessibilityNodeInfo> clipNodes = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/root_clips_layout");

        for (AccessibilityNodeInfo node : clipNodes) {
            if (node.isVisibleToUser()){
                // Find node desc
                List<AccessibilityNodeInfo> reelDesc = node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_caption_component");
                if (!reelDesc.isEmpty() && findRelevantReelDesc(reelDesc.get(0)) != null && findRelevantReelDesc(reelDesc.get(0)).equals(allowedRellDesc)) return;

                Log.d("Blocker", "Detected not allowed reel : " + node.getClassName());

                // Back action with debounce delay
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastActionTime > DEBOUNCE_DELAY) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    lastActionTime = currentTime;
                    Log.d("Blocker", "Back action performed");
                    return;
                }
            }
        }
    }

    private void handleAllowedReel(AccessibilityNodeInfo rootNode) {
        // Writes to global 'allowedRellDesc' description about a reel you just opened from chat
        // Need to be executed before detecting whether in chat, bc reads info directly from opened reel

        if (inChat){
            List<AccessibilityNodeInfo> reelDescFromChat = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_caption_component");
            if (!reelDescFromChat.isEmpty() && reelDescFromChat.get(0).isVisibleToUser()){
                allowedRellDesc = findRelevantReelDesc(reelDescFromChat.get(0));
                Log.d("Blocker", "Found reel desc from chat: " + allowedRellDesc);
            }
        }
    }

    private void chatStateDetection(AccessibilityNodeInfo rootNode) {
        // Changes inChat bool depending on whether windows with Instagram chat is open
        List<AccessibilityNodeInfo> messageFragments = rootNode.findAccessibilityNodeInfosByViewId("com.instagram.android:id/message_actions_fragment");
        if (!messageFragments.isEmpty())    inChat = messageFragments.get(0).isVisibleToUser();
        else                                inChat = false;

        Log.d("Blocker", "In chat state " + inChat);
    }

    private CharSequence findRelevantReelDesc(AccessibilityNodeInfo node) {
        if (node == null) return null;

        if ("android.view.ViewGroup".contentEquals(node.getClassName())) {
            CharSequence desc = node.getContentDescription();
            if (desc != null && desc.toString().length() > 5) { // heuristic
                return desc;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            CharSequence result = findRelevantReelDesc(node.getChild(i));
            if (result != null) return result;
        }

        return null;
    }
}
