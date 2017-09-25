package com.example.lyricscan.lyricscan;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * LyricHighlightPaletteAdapter
 * @author sccho96
 * Description: This implements the adapter for the recycler view that holds highlighter
 *         color information.
 */

@SuppressWarnings("WeakerAccess")
public class LyricHighlightPaletteAdapter extends RecyclerView.Adapter<LyricHighlightPaletteAdapter.ViewHolder> {
    private static final int PALETTE_SIZE_X = 100;
    private static final int PALETTE_SIZE_Y = 100;

    private ArrayList<PaletteColorStruct> mColorData;
    private ArrayList<Bitmap> mBitmapList;
    private Bitmap mBitmapImage;
    private Canvas mCanvas;
    private Paint mPaint;
    private OnItemClickListener onItemClickListener;

    interface OnItemClickListener {
        void onClick(int position);
    }

    public enum AnnotationToolType {
        HIGHLIGHTER, ERASER, BREAKPOINT
    }

    public static class PaletteColorStruct {
        public int color;
        public String name;
        public AnnotationToolType toolType;
        public PaletteColorStruct(int color) {
            this.color = color;
            this.name = "";
            this.toolType = AnnotationToolType.HIGHLIGHTER;
        }
        public PaletteColorStruct(int color, String name) {
            this.color = color;
            this.name = name;
            this.toolType = AnnotationToolType.HIGHLIGHTER;
        }
        public PaletteColorStruct(AnnotationToolType toolType, String name) {
            this.color = 0;
            this.name = name;
            this.toolType = toolType;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View item;
        ImageView highlighterImage;
        TextView highlighterName;
        public ViewHolder(View item) {
            super(item);
            highlighterImage = (ImageView)item.findViewById(R.id.highlighterImage);
            highlighterName = (TextView)item.findViewById(R.id.highlighterName);
            this.item = item;
        }
    }

    public LyricHighlightPaletteAdapter() {
        mColorData = new ArrayList<PaletteColorStruct>();
        mCanvas = new Canvas();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        onItemClickListener = null;
        mBitmapList = new ArrayList<Bitmap>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_palette_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        mBitmapImage = Bitmap.createBitmap(PALETTE_SIZE_X, PALETTE_SIZE_Y, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmapImage);
        mPaint.setColor(mColorData.get(position).color);
        mCanvas.drawCircle(PALETTE_SIZE_X/2, PALETTE_SIZE_Y/2, Math.min(PALETTE_SIZE_X, PALETTE_SIZE_Y)/2, mPaint);
        holder.highlighterImage.setImageBitmap(mBitmapImage);
        mBitmapList.add(mBitmapImage);
        holder.highlighterName.setText(mColorData.get(position).name);

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColorData.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public PaletteColorStruct getItem(int position) {
        if (position >= mColorData.size()) {
            return null;
        }
        return mColorData.get(position);
    }

    public void addItem(PaletteColorStruct item) {
        mColorData.add(item);
    }

    public void resetItem() {
        mColorData = new ArrayList<PaletteColorStruct>();
        mBitmapList = new ArrayList<Bitmap>();
    }
}
