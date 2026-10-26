package com.mansibit.eshoarabi;

/**
 * বইয়ের সূচিপত্র। পাতার নম্বর PDF-এর পাতা অনুযায়ী (১ থেকে শুরু)।
 * নতুন অধ্যায় যোগ করতে চাইলে নিচের তালিকায় একটি লাইন বাড়ালেই হবে।
 */
public final class Book {
    private Book() {}

    public static final class Vol {
        public final String title, arabic;
        public final int start, end;
        public final String[] secTitles;
        public final int[] secPages;

        Vol(String title, String arabic, int start, int end, String[] st, int[] sp) {
            this.title = title; this.arabic = arabic; this.start = start; this.end = end;
            this.secTitles = st; this.secPages = sp;
        }
    }

    public static final Vol[] VOLS = new Vol[]{
        new Vol("প্রথম খণ্ড", "الجزء الأول", 1, 102,
            new String[]{"শিরোনাম পাতা", "প্রকাশনা তথ্য", "উৎসর্গ (الإهداء)", "কিছু কথা", "ভূমিকা", "প্রথম অধ্যায় (الباب الأول)"},
            new int[]{1, 2, 3, 5, 12, 13}),
        new Vol("দ্বিতীয় খণ্ড", "الجزء الثاني", 103, 254,
            new String[]{"শিরোনাম পাতা", "প্রথম অধ্যায় (الباب الأول)"},
            new int[]{103, 105}),
        new Vol("তৃতীয় খণ্ড", "الجزء الثالث", 255, 446,
            new String[]{"শিরোনাম পাতা", "প্রথম অধ্যায় (الباب الأول)", "দ্বিতীয় অধ্যায় (الباب الثاني)"},
            new int[]{255, 257, 317}),
    };

    /** কোন খণ্ডে পাতাটি আছে (০, ১ বা ২) */
    public static int volumeOf(int page) {
        for (int i = 0; i < VOLS.length; i++) {
            if (page >= VOLS[i].start && page <= VOLS[i].end) return i;
        }
        return VOLS.length - 1;
    }

    /** ইংরেজি সংখ্যা → বাংলা সংখ্যা */
    public static String bn(int n) {
        String s = String.valueOf(n);
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch >= '0' && ch <= '9') b.append((char) ('\u09E6' + (ch - '0'))); else b.append(ch);
        }
        return b.toString();
    }
}
