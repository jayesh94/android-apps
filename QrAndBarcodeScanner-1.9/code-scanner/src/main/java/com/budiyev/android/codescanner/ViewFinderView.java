/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.codescanner;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;

final class ViewFinderView extends View {

    protected static final int MAX_RESULT_POINTS = 20;

    protected static final int POINT_SIZE = 6;
    public static final int ALPHA1 = 200; // range 0-255
    private final BlurMaskFilter blurMaskFilter1;

    protected int scannerAlpha;

    private final Paint mMaskPaint;
    private final Paint mFramePaint;
    private final Paint line;
    private final Paint line1;
    private final Paint line2;
    private final Paint paint;
    private final Path mPath;
    private Rect mFrameRect;

    private int mFrameCornersSize = 0;
    private int mFrameCornersRadius = 0;
    private float mFrameRatioWidth = 1f;
    private float mFrameRatioHeight = 1f;
    private float mFrameSize = 0.75f;

    private boolean revAnimation;
    private float endY, top, left;
    private int frames = 4;

    int width;
    int height;

    private final float moveFrameUpPercentageHeightOfScreen = 0.1f;
    private final int lineStrokeWidth = 5;


    protected final int laserColor;
    protected final int shadowColor;
    protected final int resultPointColor;

    protected List<ResultPoint> possibleResultPoints;
    protected List<ResultPoint> lastPossibleResultPoints;

    public ViewFinderView(@NonNull final Context context) {
        super(context);
        Log.e("ViewFinderView", "ViewFinderView");
        mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskPaint.setStyle(Paint.Style.FILL);
        mFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFramePaint.setStyle(Paint.Style.STROKE);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        line = new Paint();
        line1 = new Paint();
        line2 = new Paint();

        blurMaskFilter1 = new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL);

        this.laserColor = getResources().getColor(R.color.zxing_viewfinder_laser);
        this.shadowColor = getResources().getColor(R.color.zxing_status_text);
        this.resultPointColor = getResources().getColor(R.color.zxing_possible_result_points);

        final Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        mPath = path;

        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
        lastPossibleResultPoints = new ArrayList<>(MAX_RESULT_POINTS);
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        final Rect frame = mFrameRect;

//        Log.e("ViewFinderView", "onDraw1");

        if (frame == null) {
            return;
        }
//        Log.e("ViewFinderView", "onDraw2");
        width = getWidth();
        height = getHeight();

        top = frame.getTop() - (height * this.moveFrameUpPercentageHeightOfScreen);
        left = frame.getLeft();
        final float right = frame.getRight();
        final float bottom = frame.getBottom() - (height * moveFrameUpPercentageHeightOfScreen);

//        Log.e("ViewFinderViewFrame T: ", String.valueOf(top));
//        Log.e("ViewFinderViewFrame L: ", String.valueOf(left));
//        Log.e("ViewFinderViewFrame R: ", String.valueOf(right));
//        Log.e("ViewFinderViewFrame B: ", String.valueOf(bottom));
//
//        Log.e("ViewFinderViewFrame W: ", String.valueOf(width));
//        Log.e("ViewFinderViewFrame H: ", String.valueOf(height));

        final float frameCornersSize = mFrameCornersSize;
        final float frameCornersRadius = mFrameCornersRadius;
        final Path path = mPath;
        if (frameCornersRadius > 0) {
            final float normalizedRadius =
                    Math.min(frameCornersRadius, Math.max(frameCornersSize - 1, 0));
            path.reset();
            path.moveTo(left, top + normalizedRadius);
            path.quadTo(left, top, left + normalizedRadius, top);
            path.lineTo(right - normalizedRadius, top);
            path.quadTo(right, top, right, top + normalizedRadius);
            path.lineTo(right, bottom - normalizedRadius);
            path.quadTo(right, bottom, right - normalizedRadius, bottom);
            path.lineTo(left + normalizedRadius, bottom);
            path.quadTo(left, bottom, left, bottom - normalizedRadius);
            path.lineTo(left, top + normalizedRadius);
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.lineTo(0, 0);
            canvas.drawPath(path, mMaskPaint);
            path.reset();
            path.moveTo(left, top + frameCornersSize);
            path.lineTo(left, top + normalizedRadius);
            path.quadTo(left, top, left + normalizedRadius, top);
            path.lineTo(left + frameCornersSize, top);
            path.moveTo(right - frameCornersSize, top);
            path.lineTo(right - normalizedRadius, top);
            path.quadTo(right, top, right, top + normalizedRadius);
            path.lineTo(right, top + frameCornersSize);
            path.moveTo(right, bottom - frameCornersSize);
            path.lineTo(right, bottom - normalizedRadius);
            path.quadTo(right, bottom, right - normalizedRadius, bottom);
            path.lineTo(right - frameCornersSize, bottom);
            path.moveTo(left + frameCornersSize, bottom);
            path.lineTo(left + normalizedRadius, bottom);
            path.quadTo(left, bottom, left, bottom - normalizedRadius);
            path.lineTo(left, bottom - frameCornersSize);
            canvas.drawPath(path, mFramePaint);
        } else {
            path.reset();
            path.moveTo(left, top);
            path.lineTo(right, top);
            path.lineTo(right, bottom);
            path.lineTo(left, bottom);
            path.lineTo(left, top);
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.lineTo(0, 0);
            canvas.drawPath(path, mMaskPaint);
            path.reset();
            path.moveTo(left, top + frameCornersSize);
            path.lineTo(left, top);
            path.lineTo(left + frameCornersSize, top);
            path.moveTo(right - frameCornersSize, top);
            path.lineTo(right, top);
            path.lineTo(right, top + frameCornersSize);
            path.moveTo(right, bottom - frameCornersSize);
            path.lineTo(right, bottom);
            path.lineTo(right - frameCornersSize, bottom);
            path.moveTo(left + frameCornersSize, bottom);
            path.lineTo(left, bottom);
            path.lineTo(left, bottom - frameCornersSize);
            canvas.drawPath(path, mFramePaint);
        }

//        paint.setColor(laserColor);

//        paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

        final int frameHeight = (int) (bottom - top);
        final int frameWidth = (int) (right - left);
        final int middle = (int) (top + frameHeight/2);

//        canvas.drawRect(left + 2, middle - 1, right - 2, middle + 2, paint);

        // draw horizontal line


        line.setColor(shadowColor);
        line.setAlpha(ALPHA1);
        line.setStrokeWidth(lineStrokeWidth);

        line1.setColor(laserColor);
        line1.setStrokeWidth(lineStrokeWidth);
//        line1.setMaskFilter(blurMaskFilter1);

//        line2.setColor(resultPointColor);
//        line2.setStrokeWidth(lineStrokeWidth);

//        line2.setShadowLayer(10, 0, 0, Color.RED);
        line.setShadowLayer(5, 0, 0, shadowColor);

//        canvas.drawLine(left, top, left + frameWidth, top, line1);
//        canvas.drawLine(left, top + 50, left + frameWidth, top + 50, line2);

//        Log.e("ViewFinderView endY: ", String.valueOf(endY));
//        Log.e("View revAnimation: ", String.valueOf(revAnimation));

        // draw the line to product animation
        if (endY >= top + frameHeight + frames) {
            revAnimation = true;
        } else if (endY <= top + frames) {
            revAnimation = false;
        }

        // check if the line has reached to bottom
        if (revAnimation) {
            endY -= frames;
        } else {
            endY += frames;
        }
        canvas.drawLine(left, endY, left + frameWidth, endY, line);
        canvas.drawLine(left, endY, left + frameWidth, endY, line1);

//        Log.e("ViewFinderView left: ", String.valueOf(left));
//        Log.e("ViewFinderView endY: ", String.valueOf(endY));
//        Log.e("ViewFinderView stopX: ", String.valueOf(left + frameWidth));
//        Log.e("ViewFinderView stopY: ", String.valueOf(endY));
//
//        Log.e("ViewFinderView frames: ", String.valueOf(frames));

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.e("ViewFinderView: ","onSizeChanged");
        left = (w - width) / 2;
        top = (h - height) / 2;
        endY = top;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Only call from the UI thread.
     *
     * @param point a point to draw, relative to the preview frame
     */
    public void addPossibleResultPoint(ResultPoint point) {
        if (possibleResultPoints.size() < MAX_RESULT_POINTS)
            possibleResultPoints.add(point);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right,
            final int bottom) {
        Log.e("ViewFinderView", "onLayout");
        invalidateFrameRect(right - left, bottom - top);
    }

    @Nullable
    Rect getFrameRect() {
        return mFrameRect;
    }

    void setFrameAspectRatio(@FloatRange(from = 0, fromInclusive = false) final float ratioWidth,
            @FloatRange(from = 0, fromInclusive = false) final float ratioHeight) {
        mFrameRatioWidth = ratioWidth;
        mFrameRatioHeight = ratioHeight;
        invalidateFrameRect();
        if (isLaidOut()) {
            invalidate();
        }
    }

    @FloatRange(from = 0, fromInclusive = false)
    float getFrameAspectRatioWidth() {
        return mFrameRatioWidth;
    }

    void setFrameAspectRatioWidth(
            @FloatRange(from = 0, fromInclusive = false) final float ratioWidth) {
        mFrameRatioWidth = ratioWidth;
        invalidateFrameRect();
        if (isLaidOut()) {
            invalidate();
        }
    }

    @FloatRange(from = 0, fromInclusive = false)
    float getFrameAspectRatioHeight() {
        return mFrameRatioHeight;
    }

    void setFrameAspectRatioHeight(
            @FloatRange(from = 0, fromInclusive = false) final float ratioHeight) {
        mFrameRatioHeight = ratioHeight;
        invalidateFrameRect();
        if (isLaidOut()) {
            invalidate();
        }
    }

    @ColorInt
    int getMaskColor() {
        return mMaskPaint.getColor();
    }

    void setMaskColor(@ColorInt final int color) {
        mMaskPaint.setColor(color);
        if (isLaidOut()) {
            invalidate();
        }
    }

    @ColorInt
    int getFrameColor() {
        return mFramePaint.getColor();
    }

    void setFrameColor(@ColorInt final int color) {
        mFramePaint.setColor(color);
        if (isLaidOut()) {
            invalidate();
        }
    }

    @Px
    int getFrameThickness() {
        return (int) mFramePaint.getStrokeWidth();
    }

    void setFrameThickness(@Px final int thickness) {
        mFramePaint.setStrokeWidth(thickness);
        if (isLaidOut()) {
            invalidate();
        }
    }

    @Px
    int getFrameCornersSize() {
        return mFrameCornersSize;
    }

    void setFrameCornersSize(@Px final int size) {
        mFrameCornersSize = size;
        if (isLaidOut()) {
            invalidate();
        }
    }

    @Px
    int getFrameCornersRadius() {
        return mFrameCornersRadius;
    }

    void setFrameCornersRadius(@Px final int radius) {
        mFrameCornersRadius = radius;
        if (isLaidOut()) {
            invalidate();
        }
    }

    @FloatRange(from = 0.1, to = 1.0)
    public float getFrameSize() {
        return mFrameSize;
    }

    void setFrameSize(@FloatRange(from = 0.1, to = 1.0) final float size) {
        mFrameSize = size;
        invalidateFrameRect();
        if (isLaidOut()) {
            invalidate();
        }
    }

    private void invalidateFrameRect() {
        invalidateFrameRect(getWidth(), getHeight());
    }

    private void invalidateFrameRect(final int width, final int height) {

        Log.e("ViewFinderView", "invalidateFrameRect");

        if (width > 0 && height > 0) {
            final float viewAR = (float) width / (float) height;
            final float frameAR = mFrameRatioWidth / mFrameRatioHeight;
            final int frameWidth;
            final int frameHeight;
            if (viewAR <= frameAR) {
                frameWidth = Math.round(width * mFrameSize);
                frameHeight = Math.round(frameWidth / frameAR);
            } else {
                frameHeight = Math.round(height * mFrameSize);
                frameWidth = Math.round(frameHeight * frameAR);
            }
            final int frameLeft = (width - frameWidth) / 2;
            final int frameTop = (height - frameHeight) / 2;
            mFrameRect =
                    new Rect(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight);

            Log.e("ViewFinderViewFrame H", frameLeft+ " "+frameTop+ " "+ (frameLeft + frameWidth)+" "+ (frameTop + frameHeight));
        }
    }
}
