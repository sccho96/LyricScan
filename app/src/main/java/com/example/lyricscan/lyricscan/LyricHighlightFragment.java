package com.example.lyricscan.lyricscan;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * LyricHighlightActivity (Activity)
 * @author sccho96
 * Description: This activty provides user interface for annotating a sheet music
 *         using highlighters.
 */
public class LyricHighlightFragment extends Fragment {

    private static final String FILE_PATH = "filePath";

    private LyricHighlightCanvasView mHighlightCanvasView;

    private Uri mFilePath;

    public LyricHighlightFragment() {
        // Empty constructor
    }

    public static LyricHighlightFragment newInstance() {
        LyricHighlightFragment fragment = new LyricHighlightFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static LyricHighlightFragment newInstance(Uri filePath) {
        LyricHighlightFragment fragment = new LyricHighlightFragment();
        Bundle args = new Bundle();
        args.putString(FILE_PATH, filePath.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFilePath = Uri.parse(getArguments().getString(FILE_PATH));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View lyricHighlightFragmentView = inflater.inflate(R.layout.fragment_lyric_highlight, container, false);

        mHighlightCanvasView = (LyricHighlightCanvasView)lyricHighlightFragmentView.findViewById(R.id.highlightCanvas);

        if (mFilePath != null) {
            setSheetMusicFilePath(mFilePath);
        }

        return lyricHighlightFragmentView;
    }

    public void setSheetMusicFilePath(Uri filePath) {
        mFilePath = filePath;
        if (mHighlightCanvasView != null) {
            mHighlightCanvasView.setSheetMusic(mFilePath);
        }
    }

    public void setEditEnabled(boolean editEnabled) {
        mHighlightCanvasView.setEditEnabled(editEnabled);
    }

    public void setEraserEnabled(boolean eraserEnabled) {
        mHighlightCanvasView.setEraserEnabled(eraserEnabled);
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
