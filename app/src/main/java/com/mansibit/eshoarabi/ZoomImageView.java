package com.mansibit.eshoarabi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

/** পিঞ্চ করে জুম, ডাবল ট্যাপে জুম, জুম অবস্থায় টেনে সরানো। */
public class ZoomImageView extends AppCompatImageView {
    public interface TapListener { void onTap(); }

    private final Matrix m = new Matrix();
    private final float[] v = new float[9];
    private float base = 1f, bw = 0f, bh = 0f;
    private TapListener tapListener;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    public ZoomImageView(Context c) { super(c); init(c); }

    public ZoomImageView(Context c, AttributeSet a) { super(c, a); init(c); }

    public void setTapListener(TapListener l) { tapListener = l; }

    private void init(Context c) {
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(c, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector d) {
                float cur = cur();
                float target = Math.max(1f, Math.min(4f, cur * d.getScaleFactor()));
                float f = target / cur;
                m.postScale(f, f, d.getFocusX(), d.getFocusY());
                fix();
                setImageMatrix(m);
                return true;
            }
        });

        gestureDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }

            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                if (tapListener != null) tapListener.onTap();
                return true;
            }

            @Override public boolean onDoubleTap(MotionEvent e) {
                float cur = cur();
                float target = cur > 1.05f ? 1f : 2.5f;
                float f = target / cur;
                m.postScale(f, f, e.getX(), e.getY());
                fix();
                setImageMatrix(m);
                return true;
            }

            @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                if (cur() <= 1.01f) return false;
                getParent().requestDisallowInterceptTouchEvent(true);
                m.getValues(v);
                float cw = bw * v[Matrix.MSCALE_X];
                float tx = v[Matrix.MTRANS_X];
                boolean atRight = tx + cw <= getWidth() + 1f;
                boolean atLeft = tx >= -1f;
                if ((dx > 0 && atRight) || (dx < 0 && atLeft)) {
                    // কিনারায় পৌঁছালে পাতা বদলের সুযোগ দাও
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                m.postTranslate(-dx, -dy);
                fix();
                setImageMatrix(m);
                return true;
            }
        });
    }

    private float cur() {
        m.getValues(v);
        return base == 0f ? 1f : v[Matrix.MSCALE_X] / base;
    }

    private void fit() {
        if (bw <= 0f || bh <= 0f || getWidth() == 0 || getHeight() == 0) return;
        base = Math.min(getWidth() / bw, getHeight() / bh);
        m.reset();
        m.postScale(base, base);
        m.postTranslate((getWidth() - bw * base) / 2f, (getHeight() - bh * base) / 2f);
        setImageMatrix(m);
    }

    private void fix() {
        m.getValues(v);
        float s = v[Matrix.MSCALE_X];
        float cw = bw * s, ch = bh * s;
        float tx = v[Matrix.MTRANS_X], ty = v[Matrix.MTRANS_Y];
        float vw = getWidth(), vh = getHeight();
        float dx = 0f, dy = 0f;
        if (cw <= vw) dx = (vw - cw) / 2f - tx;
        else if (tx > 0f) dx = -tx;
        else if (tx + cw < vw) dx = vw - (tx + cw);
        if (ch <= vh) dy = (vh - ch) / 2f - ty;
        else if (ty > 0f) dy = -ty;
        else if (ty + ch < vh) dy = vh - (ty + ch);
        m.postTranslate(dx, dy);
    }

    @Override public void setImageBitmap(Bitmap b) {
        super.setImageBitmap(b);
        bw = b == null ? 0f : b.getWidth();
        bh = b == null ? 0f : b.getHeight();
        fit();
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        fit();
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (getDrawable() == null) return super.onTouchEvent(e);
        int a = e.getActionMasked();
        if ((a == MotionEvent.ACTION_DOWN && cur() > 1.01f) || a == MotionEvent.ACTION_POINTER_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        scaleDetector.onTouchEvent(e);
        if (!scaleDetector.isInProgress()) gestureDetector.onTouchEvent(e);
        return true;
    }
}
