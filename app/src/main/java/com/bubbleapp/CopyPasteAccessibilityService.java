package com.bubbleapp;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class CopyPasteAccessibilityService extends AccessibilityService {

    // Raw action IDs — stable across all API levels, no symbol lookup needed
    private static final int ACTION_SELECT_ALL = 0x00100000; // AccessibilityNodeInfo.ACTION_SELECT_ALL
    private static final int ACTION_COPY       = 0x00004000; // AccessibilityNodeInfo.ACTION_COPY
    private static final int ACTION_PASTE      = 0x00008000; // AccessibilityNodeInfo.ACTION_PASTE

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
        node.performAction(ACTION_SELECT_ALL);
        node.performAction(ACTION_COPY);
        node.recycle();
    }

    public void performSelectAllThenPaste() {
        AccessibilityNodeInfo node = findFocusedEditableNode();
        if (node == null) return;
        node.performAction(ACTION_SELECT_ALL);
        node.performAction(ACTION_PASTE);
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
