package com.example.lyricscan.lyricscan;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * LyricHighlightActivity (Activity)
 * @author sccho96
 * Description: This activty provides user interface for annotating a sheet music
 *         using highlighters.
 */
public class LyricHighlightFragment extends Fragment {

    private static final String BITMAP = "bitmap";

    private LyricHighlightCanvasView mHighlightCanvasView;

    private Bitmap mBitmap;

    public LyricHighlightFragment() {
        // Empty constructor
    }

    public static LyricHighlightFragment newInstance() {
        LyricHighlightFragment fragment = new LyricHighlightFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static LyricHighlightFragment newInstance(Bitmap bitmap) {
        LyricHighlightFragment fragment = new LyricHighlightFragment();
        Bundle args = new Bundle();
        args.putParcelable(BITMAP, bitmap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBitmap = getArguments().getParcelable(BITMAP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View lyricHighlightFragmentView = inflater.inflate(R.layout.fragment_lyric_highlight, container, false);

        mHighlightCanvasView = (LyricHighlightCanvasView)lyricHighlightFragmentView.findViewById(R.id.highlightCanvas);

        if (mBitmap != null) {
            setSheetMusicBitmap(mBitmap);
        }

        return lyricHighlightFragmentView;
    }

    public void setSheetMusicBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (mHighlightCanvasView != null) {
            mHighlightCanvasView.setSheetMusic(mBitmap);
        }
    }

    public void setEditEnabled(boolean editEnabled) {
        mHighlightCanvasView.setEditEnabled(editEnabled);
    }

    public void setToolType(LyricHighlightPaletteAdapter.AnnotationToolType toolType) {
        mHighlightCanvasView.setToolType(toolType);
    }

    public void setColor(int color) {
        mHighlightCanvasView.setColor(color);
    }

    public Bitmap getSheetMusic() {
        return mHighlightCanvasView.getSheetMusic();
    }

    public Bitmap getHighlightMatrix() {
        return mHighlightCanvasView.getHighlightBitmap();
    }
}
