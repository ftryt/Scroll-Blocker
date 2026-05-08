package com.example.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public interface BlockerStrategy {
    void execute(AccessibilityEvent event, AccessibilityService service);
}