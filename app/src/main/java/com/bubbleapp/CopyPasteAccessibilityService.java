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
        // Select all text in the field first
        node.performAction(ACTION_SELECT_ALL);
        // Then copy the selection to clipboard
        node.performAction(ACTION_COPY);
        node.recycle();
    }

    public void performSelectAllThenPaste() {
        AccessibilityNodeInfo node = findFocusedEditableNode();
        if (node == null) return;
        // Select all existing text first (so paste replaces it)
        node.performAction(ACTION_SELECT_ALL);
        // Then paste clipboard over the selection
        node.performAction(ACTION_PASTE);
        node.recycle();
    }

    private AccessibilityNodeInfo findFocusedEditableNode() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return null;

        // First try: keyboard-focused node (cursor is in this field)
        AccessibilityNodeInfo node = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);

        // Second try: walk tree for any focused editable node
        if (node == null) {
            node = findEditableNode(root);
        }

        if (node != null && node == root) {
            // don't recycle root if we're returning it
            return node;
        }
        root.recycle();
        return node;
    }

    private AccessibilityNodeInfo findEditableNode(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isEditable() && node.isFocused()) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo result = findEditableNode(child);
            if (child != null && result == null) child.recycle();
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}
}
