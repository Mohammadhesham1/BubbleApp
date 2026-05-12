package com.bubbleapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.btnStart);
        Button stopBtn = findViewById(R.id.btnStop);

        startBtn.setOnClickListener(v -> {
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Please enable BubbleApp in Accessibility Settings", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return;
            }
            if (Settings.canDrawOverlays(this)) {
                startBubbleService();
            } else {
                requestOverlayPermission();
            }
        });

        stopBtn.setOnClickListener(v -> {
            stopService(new Intent(this, BubbleService.class));
            Toast.makeText(this, "Bubble stopped", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isAccessibilityServiceEnabled() {
        String prefString = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String service = getPackageName() + "/" + CopyPasteAccessibilityService.class.getName();
        return prefString != null && prefString.contains(service);
    }

    private void startBubbleService() {
        Intent intent = new Intent(this, BubbleService.class);
        startForegroundService(intent);
        Toast.makeText(this, "Bubble started", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (Settings.canDrawOverlays(this)) {
                startBubbleService();
            } else {
                Toast.makeText(this, "Overlay permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
