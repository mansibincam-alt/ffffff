package com.mansibit.eshoarabi;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.text.InputType;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderActivity extends AppCompatActivity {
    private ViewPager2 pager;
    private View topBar, bottomBar, root;
    private TextView title, pageLabel, btnBm, btnNight;
    private SeekBar seek;
    private int total, current = 1, renderW;
    private boolean night;
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private LruCache<Integer, Bitmap> cache;
    private ColorMatrixColorFilter invert;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_reader);

        try {
            PdfHelper.init(getApplicationContext());
        } catch (IOException e) {
            Toast.makeText(this, "বইটি খোলা যায়নি", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        total = PdfHelper.pageCount();

        root = findViewById(R.id.root);
        pager = (ViewPager2) findViewById(R.id.pager);
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
        title = (TextView) findViewById(R.id.title);
        pageLabel = (TextView) findViewById(R.id.pageLabel);
        btnBm = (TextView) findViewById(R.id.btnBm);
        btnNight = (TextView) findViewById(R.id.btnNight);
        seek = (SeekBar) findViewById(R.id.seek);

        invert = new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                -1, 0, 0, 0, 255,
                0, -1, 0, 0, 255,
                0, 0, -1, 0, 255,
                0, 0, 0, 1, 0}));

        int screenW = getResources().getDisplayMetrics().widthPixels;
        renderW = Math.min(1500, Math.round(screenW * 1.5f));
        cache = new LruCache<Integer, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 5)) {
            @Override protected int sizeOf(Integer k, Bitmap b) { return b.getByteCount(); }
        };

        night = Store.isNight(this);
        applyNightBg();

        int start = Math.max(1, Math.min(total, getIntent().getIntExtra("page", 1)));
        pager.setAdapter(new PageAdapter());
        pager.setOffscreenPageLimit(1);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { onPage(position + 1); }
        });
        pager.setCurrentItem(start - 1, false);
        onPage(start);

        seek.setMax(Math.max(1, total - 1));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                if (fromUser) pager.setCurrentItem(p, false);
            }
            @Override public void onStartTrackingTouch(SeekBar s) { }
            @Override public void onStopTrackingTouch(SeekBar s) { }
        });

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        findViewById(R.id.btnGo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { askPage(); }
        });
        btnNight.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                night = !night;
                Store.setNight(ReaderActivity.this, night);
                applyNightBg();
                pager.getAdapter().notifyDataSetChanged();
            }
        });
        btnBm.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean added = Store.toggleBookmark(ReaderActivity.this, current);
                Toast.makeText(ReaderActivity.this, added ? "বুকমার্ক করা হয়েছে" : "বুকমার্ক মুছে গেছে", Toast.LENGTH_SHORT).show();
                updateBm();
            }
        });
    }

    private void applyNightBg() {
        root.setBackgroundColor(night ? Color.BLACK : 0xFFE9EDF2);
        btnNight.setText(night ? "☀" : "☾");
    }

    private void toggleBars() {
        int vis = topBar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        topBar.setVisibility(vis);
        bottomBar.setVisibility(vis);
    }

    private void onPage(int p) {
        current = p;
        Store.setLast(this, p);
        int vi = Book.volumeOf(p);
        title.setText(Book.VOLS[vi].title + " · পৃষ্ঠা " + Book.bn(p));
        pageLabel.setText(Book.bn(p) + "/" + Book.bn(total));
        if (seek.getProgress() != p - 1) seek.setProgress(p - 1);
        updateBm();
        prefetch(p - 2);
        prefetch(p);
    }

    private void updateBm() {
        boolean on = Store.isBookmarked(this, current);
        btnBm.setText(on ? "★" : "☆");
        btnBm.setTextColor(on ? 0xFFFFC107 : Color.WHITE);
    }

    private void prefetch(final int idx) {
        if (idx < 0 || idx >= total || cache.get(idx) != null) return;
        exec.execute(new Runnable() {
            @Override public void run() {
                if (cache.get(idx) != null) return;
                try { cache.put(idx, PdfHelper.render(idx, renderW)); } catch (Throwable ignored) { }
            }
        });
    }

    private void askPage() {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setHint("১ – " + Book.bn(total));
        new AlertDialog.Builder(this)
                .setTitle("কোন পৃষ্ঠায় যাবেন?")
                .setView(et)
                .setPositiveButton("যান", new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) {
                        try {
                            int n = Integer.parseInt(et.getText().toString().trim());
                            if (n >= 1 && n <= total) pager.setCurrentItem(n - 1, false);
                            else Toast.makeText(ReaderActivity.this, "সঠিক পৃষ্ঠা নম্বর দিন", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(ReaderActivity.this, "সঠিক পৃষ্ঠা নম্বর দিন", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    @Override protected void onPause() {
        super.onPause();
        if (total > 0) Store.setLast(this, current);
    }

    @Override protected void onDestroy() {
        exec.shutdownNow();
        super.onDestroy();
    }

    // ================= পাতার অ্যাডাপ্টার =================
    private class PageAdapter extends RecyclerView.Adapter<PageAdapter.VH> {
        class VH extends RecyclerView.ViewHolder {
            final ZoomImageView view;
            int pos = -1;
            VH(ZoomImageView v) { super(v); view = v; }
        }

        @Override public VH onCreateViewHolder(ViewGroup parent, int type) {
            ZoomImageView z = new ZoomImageView(parent.getContext());
            z.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            z.setTapListener(new ZoomImageView.TapListener() {
                @Override public void onTap() { toggleBars(); }
            });
            return new VH(z);
        }

        @Override public void onBindViewHolder(final VH h, final int pos) {
            h.pos = pos;
            h.view.setColorFilter(night ? invert : null);
            Bitmap c = cache.get(pos);
            if (c != null) { h.view.setImageBitmap(c); return; }
            h.view.setImageBitmap(null);
            exec.execute(new Runnable() {
                @Override public void run() {
                    Bitmap b = cache.get(pos);
                    if (b == null) {
                        try { b = PdfHelper.render(pos, renderW); } catch (Throwable t) { return; }
                        cache.put(pos, b);
                    }
                    final Bitmap fb = b;
                    runOnUiThread(new Runnable() {
                        @Override public void run() { if (h.pos == pos) h.view.setImageBitmap(fb); }
                    });
                }
            });
        }

        @Override public int getItemCount() { return total; }
    }
}
