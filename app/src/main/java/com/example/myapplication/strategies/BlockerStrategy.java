package com.example.myapplication.strategies;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public interface BlockerStrategy {
    void execute(AccessibilityEvent event, AccessibilityService service);
}