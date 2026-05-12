package com.bubbleapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class BubbleService extends Service {

    private static final String CHANNEL_ID = "bubble_channel";
    private WindowManager windowManager;
    private View bubbleView;
    private View panelView;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;

    // Drag tracking
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private boolean isDragging;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        showBubble();
    }

    // ─── Bubble ────────────────────────────────────────────────────────────────

    private void showBubble() {
        if (panelView != null) {
            windowManager.removeView(panelView);
            panelView = null;
        }

        bubbleView = LayoutInflater.from(this).inflate(R.layout.view_bubble, null);

        bubbleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = 50;
        bubbleParams.y = 300;

        bubbleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = bubbleParams.x;
                        initialY = bubbleParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int dx = (int)(event.getRawX() - initialTouchX);
                        int dy = (int)(event.getRawY() - initialTouchY);
                        if (Math.abs(dx) > 8 || Math.abs(dy) > 8) {
                            isDragging = true;
                        }
                        if (isDragging) {
                            bubbleParams.x = initialX + dx;
                            bubbleParams.y = initialY + dy;
                            windowManager.updateViewLayout(bubbleView, bubbleParams);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            showPanel();
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(bubbleView, bubbleParams);
    }

    // ─── Panel ─────────────────────────────────────────────────────────────────

    private void showPanel() {
        if (bubbleView != null) {
            windowManager.removeView(bubbleView);
            bubbleView = null;
        }

        panelView = LayoutInflater.from(this).inflate(R.layout.view_panel, null);

        panelParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        panelParams.gravity = Gravity.CENTER;

        // Copy button: Select All then Copy via AccessibilityService
        panelView.findViewById(R.id.btnCopy).setOnClickListener(v -> {
            if (CopyPasteAccessibilityService.getInstance() != null) {
                CopyPasteAccessibilityService.getInstance().performSelectAllThenCopy();
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Enable Accessibility Service first", Toast.LENGTH_LONG).show();
                openAccessibilitySettings();
            }
        });

        // Paste button: Select All then Paste via AccessibilityService
        panelView.findViewById(R.id.btnPaste).setOnClickListener(v -> {
            if (CopyPasteAccessibilityService.getInstance() != null) {
                CopyPasteAccessibilityService.getInstance().performSelectAllThenPaste();
                Toast.makeText(this, "Pasted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Enable Accessibility Service first", Toast.LENGTH_LONG).show();
                openAccessibilitySettings();
            }
        });

        // Minimize button
        panelView.findViewById(R.id.btnMinimize).setOnClickListener(v -> showBubble());

        windowManager.addView(panelView, panelParams);
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // ─── Notification ──────────────────────────────────────────────────────────

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Bubble Service", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Keeps bubble overlay running");
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bubble Active")
                .setContentText("Tap to manage")
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    // ─── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bubbleView != null) windowManager.removeView(bubbleView);
        if (panelView != null) windowManager.removeView(panelView);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
