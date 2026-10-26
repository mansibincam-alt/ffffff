package com.mansibit.eshoarabi;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** শেষ পড়া পাতা, বুকমার্ক ও নাইট মোড সেভ রাখে। পাতার নম্বর ১ থেকে শুরু। */
public final class Store {
    private Store() {}

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("esho_arabi", Context.MODE_PRIVATE);
    }

    public static int getLast(Context c) { return sp(c).getInt("last", -1); }

    public static void setLast(Context c, int page) { sp(c).edit().putInt("last", page).apply(); }

    public static boolean isNight(Context c) { return sp(c).getBoolean("night", false); }

    public static void setNight(Context c, boolean v) { sp(c).edit().putBoolean("night", v).apply(); }

    public static List<Integer> bookmarks(Context c) {
        Set<String> s = sp(c).getStringSet("bm", new HashSet<String>());
        List<Integer> out = new ArrayList<>();
        for (String x : s) {
            try { out.add(Integer.parseInt(x)); } catch (NumberFormatException ignored) { }
        }
        Collections.sort(out);
        return out;
    }

    public static boolean isBookmarked(Context c, int page) { return bookmarks(c).contains(page); }

    /** @return true হলে বুকমার্ক যোগ হয়েছে, false হলে মুছে গেছে */
    public static boolean toggleBookmark(Context c, int page) {
        Set<String> s = new HashSet<>(sp(c).getStringSet("bm", new HashSet<String>()));
        String k = String.valueOf(page);
        boolean added;
        if (s.contains(k)) { s.remove(k); added = false; } else { s.add(k); added = true; }
        sp(c).edit().putStringSet("bm", s).apply();
        return added;
    }
}
