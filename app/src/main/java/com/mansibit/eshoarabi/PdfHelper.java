package com.mansibit.eshoarabi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** assets/book.pdf কে ফোনের নিজস্ব PdfRenderer দিয়ে দেখায়। */
public final class PdfHelper {
    private PdfHelper() {}

    // PDF বদলালে এই সংখ্যাটা বাড়িয়ে দেবেন, যাতে ফোনে নতুন কপি তৈরি হয়।
    private static final String FILE_NAME = "book_v1.pdf";
    private static PdfRenderer renderer;

    public static synchronized void init(Context ctx) throws IOException {
        if (renderer != null) return;
        File f = new File(ctx.getApplicationContext().getFilesDir(), FILE_NAME);
        if (!f.exists() || f.length() == 0) {
            File tmp = new File(f.getPath() + ".tmp");
            try (InputStream in = ctx.getApplicationContext().getAssets().open("book.pdf");
                 OutputStream out = new FileOutputStream(tmp)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
            if (!tmp.renameTo(f)) throw new IOException("copy failed");
        }
        renderer = new PdfRenderer(ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY));
    }

    public static synchronized int pageCount() {
        return renderer == null ? 0 : renderer.getPageCount();
    }

    /** index: ০ থেকে শুরু */
    public static synchronized Bitmap render(int index, int targetWidth) {
        PdfRenderer.Page p = renderer.openPage(index);
        try {
            float ratio = (float) p.getHeight() / (float) p.getWidth();
            int h = Math.max(1, Math.round(targetWidth * ratio));
            Bitmap b = Bitmap.createBitmap(targetWidth, h, Bitmap.Config.ARGB_8888);
            b.eraseColor(Color.WHITE);
            p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            return b;
        } finally {
            p.close();
        }
    }
}
