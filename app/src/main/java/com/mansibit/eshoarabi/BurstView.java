package com.mansibit.eshoarabi;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** লোগো তৈরি হওয়ার সময় চারপাশে ছড়িয়ে পড়া রঙিন কণা ও রিং। */
public class BurstView extends View {
    private static final int N = 32;
    private static final int[] COLORS = {0xFFE53935, 0xFFFFFFFF, 0xFF1E9BF0, 0xFFFFC107};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float p = -1f;

    public BurstView(Context c) { super(c); }

    public BurstView(Context c, AttributeSet a) { super(c, a); }

    public void start() {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(1300);
        va.setInterpolator(new DecelerateInterpolator(1.6f));
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator a) {
                p = (Float) a.getAnimatedValue();
                invalidate();
            }
        });
        va.start();
    }

    @Override protected void onDraw(Canvas c) {
        if (p < 0f || p >= 1f) return;
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float max = Math.min(getWidth(), getHeight()) * 0.62f;
        float d = getResources().getDisplayMetrics().density;
        float fade = 1f - p;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f * d * fade + 1f);
        paint.setColor(0xFF4FC3F7);
        paint.setAlpha((int) (200 * fade));
        c.drawCircle(cx, cy, max * 0.35f + max * 0.65f * p, paint);

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < N; i++) {
            double ang = Math.PI * 2 * i / N + (i % 2 == 0 ? 0 : 0.1);
            float dist = max * (i % 3 == 0 ? 1f : (i % 3 == 1 ? 0.8f : 0.6f)) * p;
            float x = cx + (float) Math.cos(ang) * dist;
            float y = cy + (float) Math.sin(ang) * dist;
            paint.setColor(COLORS[i % COLORS.length]);
            paint.setAlpha((int) (255 * fade));
            c.drawCircle(x, y, (2f + 5f * fade) * d, paint);
        }
    }
}
