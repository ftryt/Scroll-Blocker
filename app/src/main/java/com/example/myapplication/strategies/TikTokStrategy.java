package com.example.myapplication.strategies;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class TikTokStrategy implements BlockerStrategy {

    @Override
    public void execute(AccessibilityEvent event, AccessibilityService service) {
        // does the simplest thing possible if "Home" is active then switch to "Inbox"

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) return;

        var menu = findNavMenu(rootNode);

        if (menu == null)
            return;

        if (isHomeSelected(menu))
            pressInbox(menu);
    }

    public boolean isHomeSelected(AccessibilityNodeInfo navMenu) {
        Log.d("Blocker", "Home: " + navMenu.getChild(0).getContentDescription().toString());

        return navMenu.getChild(0).isSelected();
    }

    public void pressInbox(AccessibilityNodeInfo navMenu) {
        if (navMenu.getChild(3) != null){
            navMenu.getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    // Finds nav bar using class signature
    private AccessibilityNodeInfo findNavMenu(AccessibilityNodeInfo node) {
        // Signature:
        // LinearLayout:
        //      FrameLayout
        //      FrameLayout
        //      Button
        //      FrameLayout
        //      FrameLayout

        if (node == null) return null;

        // Check current element
        if ("android.widget.LinearLayout".equals(node.getClassName().toString()) && node.getChildCount() == 5) {
            boolean success = true;

            for (int i = 0; i < 2; i += 1) {
                if (!"android.widget.FrameLayout".equals(node.getChild(i).getClassName().toString())) {
                    success = false;
                    break;
                }
            }

            if (!"android.widget.Button".equals(node.getChild(2).getClassName().toString()))
                success = false;

            for (int i = 3; i < 5; i += 1) {
                if (!"android.widget.FrameLayout".equals(node.getChild(i).getClassName().toString())) {
                    success = false;
                    break;
                }
            }

            if (success)
                return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo result = findNavMenu(node.getChild(i));
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
