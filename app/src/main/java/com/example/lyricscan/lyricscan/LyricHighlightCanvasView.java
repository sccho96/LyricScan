package com.example.lyricscan.lyricscan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.lyricscan.lyricscan.MainActivity.ERROR_TAG;

/**
 * LyricHighlightCanvasView (View)
 * @author sccho96
 * Description: This view displays sheet music with lyrics and allows user to annotate
 *         by drawing colored lines on the sheet music.
 */
public class LyricHighlightCanvasView extends View {

    private class HighlightPathStruct {
        public Path path;
        public int color;
        public int id;

        public HighlightPathStruct(Path path, int color, int id) {
            this.path = path;
            this.color = color;
            this.id = id;
        }
    }

    private class EraseRunnable implements Runnable {
        int id = -1;
        @Override
        public void run() {
            if (id != -1) {
                mPathListLock.lock();
                int res = searchPathById(id);
                mPathListLock.unlock();
                if (res != -1) {
                    mPathListLock.lock();
                    mHighlightPathList.remove(res);
                    mPathListLock.unlock();
                }
            }
        }
        public void setId(int id) {
            this.id = id;
        }
    }

    private static final float HIGHLIGHT_WIDTH = 20;
    private static final int DEFAULT_COLOR = Color.argb(100, 255, 0, 0);
    private static final int COLOR_ALPHA_MASK = 0xff000000;

    private int mColor;
    private Path mHighlightPath;
    private Paint mPaint;
    private Bitmap mSheetMusic;

    private Paint mHighlightPaint;
    private Bitmap mHighlightBitmap;
    private Bitmap mHighlightMatrix;
    private Bitmap mMatrix;
    private Canvas mHighlightCanvas;
    private Canvas mHighlightMatrixCanvas;
    private int mIdCount;
    private boolean mEraserEnabled;
    private Thread eraseThread;
    private EraseRunnable eraseRunnable;
    private ReentrantLock mPathListLock;

    private boolean mEditEnabled;

    private boolean testFlag = true;

    private ArrayList<HighlightPathStruct> mHighlightPathList;

    public LyricHighlightCanvasView(Context context) {
        super(context);
        init(null, 0);
    }

    public LyricHighlightCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LyricHighlightCanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LyricHighlightCanvasView, defStyle, 0);

        // Initialize member variables
        mEditEnabled = true;

        mColor = DEFAULT_COLOR;
        mHighlightPath = new Path();

        mPaint = new Paint();
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(HIGHLIGHT_WIDTH);

        mSheetMusic = null;

        mHighlightBitmap = null;
        mHighlightCanvas = null;

        mIdCount = 1 | COLOR_ALPHA_MASK;

        mHighlightPaint = new Paint();
        mHighlightPaint.setColor(mIdCount);
        mHighlightPaint.setStyle(Paint.Style.STROKE);
        mHighlightPaint.setStrokeWidth(HIGHLIGHT_WIDTH);

        mHighlightPathList = new ArrayList<HighlightPathStruct>();

        eraseRunnable = new EraseRunnable();

        mPathListLock = new ReentrantLock();

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // TODO: remove this conditional statement
        if (testFlag) {
            mHighlightBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mHighlightCanvas = new Canvas(mHighlightBitmap);
            mHighlightMatrix = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mHighlightMatrixCanvas = new Canvas(mHighlightMatrix);
            testFlag = false;
        }

        mPathListLock.lock();
        for (HighlightPathStruct item : mHighlightPathList) {
            mPaint.setColor(item.color);
            canvas.drawPath(item.path, mPaint);
            mPaint.setColor(item.color | COLOR_ALPHA_MASK);
            mHighlightMatrixCanvas.drawPath(item.path, mPaint);
            mHighlightPaint.setColor(item.id);
            mHighlightCanvas.drawPath(item.path, mHighlightPaint);
        }
        mPathListLock.unlock();

        mPaint.setColor(mColor);
        canvas.drawPath(mHighlightPath, mPaint);
        mPaint.setColor(mColor | COLOR_ALPHA_MASK);
        mHighlightMatrixCanvas.drawPath(mHighlightPath, mPaint);
        mHighlightPaint.setColor(mIdCount);
        mHighlightCanvas.drawPath(mHighlightPath, mHighlightPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {

        if (!mEditEnabled) {
            return false;
        }

        int touchAction = touchEvent.getAction();
        float cursorX = Math.min(Math.max(touchEvent.getX(), 0), getWidth() - 1);
        float cursorY = Math.min(Math.max(touchEvent.getY(), 0), getHeight() - 1);

        switch (touchAction) {
            case MotionEvent.ACTION_DOWN:
                // Initialize the highlight path
                if (mEraserEnabled) {
                    int searchId = mHighlightBitmap.getPixel((int)cursorX, (int)cursorY);
                    if (searchId != 0 && (eraseThread == null || !eraseThread.isAlive())) {
                        eraseRunnable.setId(searchId);
                        eraseThread = new Thread(eraseRunnable);
                        eraseThread.start();
                    }
                } else {
                    mPaint.setColor(mColor);
                    mHighlightPaint.setColor(mIdCount);
                    mHighlightPath.moveTo(cursorX, cursorY);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                // Draw  or erase the highlight path
                if (mEraserEnabled) {
                    int searchId = mHighlightBitmap.getPixel((int)cursorX, (int)cursorY);
                    if (searchId != 0 && (eraseThread == null || !eraseThread.isAlive())) {
                        eraseRunnable.setId(searchId);
                        eraseThread = new Thread(eraseRunnable);
                        eraseThread.start();
                    }
                } else {
                    mHighlightPath.lineTo(cursorX, cursorY);
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (!mEraserEnabled) {
                    mPathListLock.lock();
                    mHighlightPathList.add(new HighlightPathStruct(mHighlightPath, mColor, mIdCount));
                    mPathListLock.unlock();
                    mIdCount++;
                    mHighlightPath = new Path();
                }
                invalidate();
                mMatrix = mHighlightMatrix;
                return true;
            default:
                return false;
        }
    }

    public void setEraserEnabled(boolean eraserEnabled) {
        mEraserEnabled = eraserEnabled;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public Bitmap getHighlightBitmap() {
        return mMatrix;
    }

    public void setSheetMusic(Uri sheetMusicUri) {
        try {
            mSheetMusic = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), sheetMusicUri);
            // Initialize the bitmap and canvas objects
            post(new Runnable() {
                @Override
                public void run() {
                    // Display the sheet music in the background
                    mSheetMusic = Bitmap.createScaledBitmap(mSheetMusic, getWidth(), getHeight(), false);
                    BitmapDrawable drawableImage = new BitmapDrawable(getResources(), mSheetMusic);
                    setBackground(drawableImage);
                    mHighlightBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    mHighlightCanvas = new Canvas(mHighlightBitmap);
                }
            });
        } catch (IOException e) {
            Log.e(ERROR_TAG, e.getMessage());
        }
    }

    private int searchPathById(int id) {
        // Use binary search to find the path with the id
        int index;
        int foundItem;

        int min = 0;
        int max = mHighlightPathList.size() - 1;

        while (true) {
            if (min > max) {
                return -1;
            }

            index = (min + max) / 2;
            foundItem = mHighlightPathList.get(index).id;

            if (foundItem < id) {
                min = index + 1;
            } else if (foundItem > id) {
                max = index - 1;
            } else {
                return index;
            }
        }
    }

    public void setEditEnabled(boolean editEnabled) {
        mEditEnabled = editEnabled;
    }

    public boolean getEditEnabled() {
        return mEditEnabled;
    }

    public Bitmap getSheetMusic() {
        return mSheetMusic;
    }
}
