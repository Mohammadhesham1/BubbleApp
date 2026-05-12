package com.bubbleapp;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class CopyPasteAccessibilityService extends AccessibilityService {

    private static CopyPasteAccessibilityService instance;

    public static CopyPasteAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public void performSelectAllThenCopy() {
        AccessibilityNodeInfo node = findFocusedEditableNode();
        if (node == null) return;
        node.performAction(AccessibilityNodeInfo.ACTION_SELECT_ALL);
        node.performAction(AccessibilityNodeInfo.ACTION_COPY);
        node.recycle();
    }

    public void performSelectAllThenPaste() {
        AccessibilityNodeInfo node = findFocusedEditableNode();
        if (node == null) return;
        node.performAction(AccessibilityNodeInfo.ACTION_SELECT_ALL);
        node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        node.recycle();
    }

    private AccessibilityNodeInfo findFocusedEditableNode() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return null;

        // Try input-focused first, then accessibility-focused
        AccessibilityNodeInfo node = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (node == null) {
            node = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        }
        root.recycle();
        return node;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}
}
