package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button btnGrantPermission;
    private CheckBox checkInstagram;
    private CheckBox checkYouTube;

    private CheckBox checkTikTok;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("Blocker", "STARING THE APP!");

        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("BlockerPrefs", MODE_PRIVATE);

        statusText = findViewById(R.id.statusText);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        checkInstagram = findViewById(R.id.checkInstagram);
        checkYouTube = findViewById(R.id.checkYouTube);
        checkTikTok = findViewById(R.id.checkTikTok);

        checkInstagram.setChecked(prefs.getBoolean("com.instagram.android", false));
        checkYouTube.setChecked(prefs.getBoolean("com.google.android.youtube", false));
        checkTikTok.setChecked(prefs.getBoolean("com.zhiliaoapp.musically", false));

        btnGrantPermission.setOnClickListener(v -> {
            // Open accessibility settings
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        checkInstagram.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("com.instagram.android", isChecked).apply();
            Toast.makeText(this, isChecked ? "Instagram lock activated" : "Instagram lock is off", Toast.LENGTH_SHORT).show();
        });

        checkYouTube.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("com.google.android.youtube", isChecked).apply();
            Toast.makeText(this, isChecked ? "YouTube lock activated" : "YouTube lock is off", Toast.LENGTH_SHORT).show();
        });

        checkTikTok.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("com.zhiliaoapp.musically", isChecked).apply();
            Toast.makeText(this, isChecked ? "TikTok lock activated" : "TikTok lock is off", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We update the status every time we return to the app
        if (isAccessibilityServiceEnabled()) {
            statusText.setText("Status: ON ✅");
            statusText.setTextColor(Color.GREEN);
            btnGrantPermission.setVisibility(View.GONE);
        } else {
            statusText.setText("Status: OFF ❌");
            statusText.setTextColor(Color.RED);
            btnGrantPermission.setVisibility(View.VISIBLE);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String serviceId = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        return enabledServices != null && enabledServices.contains(serviceId);
    }
}