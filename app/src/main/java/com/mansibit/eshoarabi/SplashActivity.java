package com.mansibit.eshoarabi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean ready = false;
    private boolean animDone = false;
    private boolean went = false;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash);

        final View logo = findViewById(R.id.logo);
        final BurstView burst = (BurstView) findViewById(R.id.burst);
        final View bar = findViewById(R.id.bar);
        float d = getResources().getDisplayMetrics().density;

        // PDF ব্যাকগ্রাউন্ডে তৈরি হয়ে থাকবে
        new Thread(new Runnable() {
            @Override public void run() {
                try { PdfHelper.init(getApplicationContext()); } catch (Exception ignored) { }
                ready = true;
                runOnUiThread(new Runnable() { @Override public void run() { tryGo(); } });
            }
        }).start();

        // লোগো "তৈরি" হওয়ার অ্যানিমেশন
        logo.setCameraDistance(9000f * d);
        logo.setAlpha(0f);
        logo.setScaleX(0.2f);
        logo.setScaleY(0.2f);
        logo.setRotationY(-90f);
        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).rotationY(0f)
                .setStartDelay(350).setDuration(1100)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .withStartAction(new Runnable() { @Override public void run() { burst.start(); } })
                .start();

        // নিচে ডেভেলপার ক্রেডিট ধাপে ধাপে আসবে
        int[] ids = {R.id.credit1, R.id.credit2, R.id.credit3};
        for (int i = 0; i < ids.length; i++) {
            View v = findViewById(ids[i]);
            v.setAlpha(0f);
            v.setTranslationY(28f * d);
            v.animate().alpha(1f).translationY(0f)
                    .setStartDelay(1500 + i * 350L).setDuration(700).start();
        }

        // নিচের লোডিং বার
        bar.setPivotX(0f);
        bar.setScaleX(0f);
        bar.animate().scaleX(1f).setDuration(3600).setInterpolator(new LinearInterpolator()).start();

        handler.postDelayed(new Runnable() {
            @Override public void run() { animDone = true; tryGo(); }
        }, 3800);

        findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { animDone = true; tryGo(); }
        });
    }

    private void tryGo() {
        if (went || !animDone || !ready) return;
        went = true;
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
