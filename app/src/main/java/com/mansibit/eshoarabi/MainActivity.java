package com.mansibit.eshoarabi;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int TOTAL_FALLBACK = 446;
    private LinearLayout list;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        list = (LinearLayout) findViewById(R.id.list);
    }

    @Override protected void onResume() {
        super.onResume();
        build();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private TextView tv(String text, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        return t;
    }

    private void open(int page) {
        Intent i = new Intent(this, ReaderActivity.class);
        i.putExtra("page", page);
        startActivity(i);
    }

    private void ripple(View v) {
        TypedValue t = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, t, true);
        v.setBackgroundResource(t.resourceId);
    }

    private void build() {
        list.removeAllViews();
        int last = Store.getLast(this);
        int total = TOTAL_FALLBACK;

        // ===== পড়া চালিয়ে যান =====
        LinearLayout cont = new LinearLayout(this);
        cont.setOrientation(LinearLayout.VERTICAL);
        cont.setPadding(dp(18), dp(16), dp(18), dp(16));
        cont.setBackground(round(0xFFE53935, 18));
        cont.setElevation(dp(4));
        final int target = last >= 1 ? last : 1;
        if (last >= 1) {
            cont.addView(tv("▶  পড়া চালিয়ে যান", 18, Color.WHITE, true));
            int vi = Book.volumeOf(last);
            TextView sub = tv(Book.VOLS[vi].title + " · পৃষ্ঠা " + Book.bn(last) + " / " + Book.bn(total), 13, 0xFFFFE0DE, false);
            sub.setPadding(0, dp(4), 0, dp(10));
            cont.addView(sub);
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(total);
            pb.setProgress(last);
            pb.setProgressTintList(ColorStateList.valueOf(Color.WHITE));
            pb.setProgressBackgroundTintList(ColorStateList.valueOf(0x55FFFFFF));
            cont.addView(pb, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(6)));
        } else {
            cont.addView(tv("▶  পড়া শুরু করুন", 18, Color.WHITE, true));
            TextView sub = tv("প্রথম পাতা থেকে শুরু হবে", 13, 0xFFFFE0DE, false);
            sub.setPadding(0, dp(4), 0, 0);
            cont.addView(sub);
        }
        cont.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { open(target); }
        });
        list.addView(cont, lp(0));

        // ===== সূচিপত্র =====
        TextView head = tv("সূচিপত্র", 20, 0xFF0A1A3A, true);
        head.setPadding(dp(4), dp(22), 0, dp(8));
        list.addView(head);

        int[] badge = {0xFFE53935, 0xFF1E9BF0, 0xFF2E7D32};
        for (int i = 0; i < Book.VOLS.length; i++) {
            final Book.Vol vol = Book.VOLS[i];
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(round(Color.WHITE, 18));
            card.setElevation(dp(3));

            LinearLayout headRow = new LinearLayout(this);
            headRow.setOrientation(LinearLayout.HORIZONTAL);
            headRow.setGravity(Gravity.CENTER_VERTICAL);
            headRow.setPadding(dp(14), dp(14), dp(14), dp(10));
            ripple(headRow);

            TextView num = tv(Book.bn(i + 1), 20, Color.WHITE, true);
            num.setGravity(Gravity.CENTER);
            num.setBackground(round(badge[i % badge.length], 14));
            headRow.addView(num, new LinearLayout.LayoutParams(dp(46), dp(46)));

            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.setPadding(dp(12), 0, 0, 0);
            mid.addView(tv(vol.title, 18, 0xFF0A1A3A, true));
            mid.addView(tv(vol.arabic + "  ·  পৃষ্ঠা " + Book.bn(vol.start) + " – " + Book.bn(vol.end), 13, 0xFF607D8B, false));
            headRow.addView(mid, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            headRow.addView(tv("পড়ুন ›", 14, badge[i % badge.length], true));
            headRow.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { open(vol.start); }
            });
            card.addView(headRow);

            for (int s = 0; s < vol.secTitles.length; s++) {
                final int page = vol.secPages[s];
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(18), dp(11), dp(18), dp(11));
                ripple(row);
                TextView t = tv("•  " + vol.secTitles[s], 15, 0xFF263238, false);
                row.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(tv(Book.bn(page), 14, 0xFF78909C, false));
                row.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { open(page); }
                });
                card.addView(row);
            }
            card.setPadding(0, 0, 0, dp(6));
            list.addView(card, lp(12));
        }

        // ===== বুকমার্ক =====
        List<Integer> bms = Store.bookmarks(this);
        if (!bms.isEmpty()) {
            TextView bh = tv("★ বুকমার্ক", 20, 0xFF0A1A3A, true);
            bh.setPadding(dp(4), dp(22), 0, dp(8));
            list.addView(bh);
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(round(Color.WHITE, 18));
            card.setElevation(dp(3));
            for (final Integer pg : bms) {
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(18), dp(12), dp(8), dp(12));
                ripple(row);
                row.addView(tv("পৃষ্ঠা " + Book.bn(pg) + "  ·  " + Book.VOLS[Book.volumeOf(pg)].title, 15, 0xFF263238, false),
                        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                TextView del = tv("✕", 18, 0xFFB0BEC5, false);
                del.setPadding(dp(14), dp(4), dp(14), dp(4));
                del.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { Store.toggleBookmark(MainActivity.this, pg); build(); }
                });
                row.addView(del);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { open(pg); }
                });
                card.addView(row);
            }
            list.addView(card, lp(0));
        }

        // ===== ফুটার =====
        TextView foot = tv("The application made by\nMansibur Rahman · Studio: Mansib IT", 12, 0xFF78909C, false);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0, dp(28), 0, dp(12));
        list.addView(foot, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private LinearLayout.LayoutParams lp(int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(topDp);
        return p;
    }
}
