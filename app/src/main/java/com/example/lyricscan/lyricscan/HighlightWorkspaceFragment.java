package com.example.lyricscan.lyricscan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.HashMap;

public class HighlightWorkspaceFragment extends Fragment {

    TabViewPager mPager;
    PagerAdapter mPagerAdapter;
    FloatingActionButton mEditButton;
    FloatingActionButton mSubmitButton;

    ArrayList<LyricHighlightFragment> mFragmentList;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    LyricHighlightPaletteAdapter mAdapter;

    ArrayList<LyricHighlightPaletteAdapter.PaletteColorStruct> mPaletteList;

    private class CanvasPagerAdapter extends FragmentStatePagerAdapter {
        public CanvasPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
    }

        @Override
        public int getItemPosition(Object o) {
            return POSITION_NONE;
        }
    }

    public HighlightWorkspaceFragment() {
        // Required empty public constructor
    }

    public static HighlightWorkspaceFragment newInstance() {
        HighlightWorkspaceFragment fragment = new HighlightWorkspaceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View highlightWorkspaceView = inflater.inflate(R.layout.fragment_highlight_workspace, container, false);

        mPaletteList = new ArrayList<>();
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,0,0), "red"));
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,0,255,0), "green"));
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,0,0,255), "blue"));
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,255,0), "yellow"));
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,0,255), "purple"));
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(255,255,255,255), "eraser"));

        mFragmentList = new ArrayList<>();

        mPager = (TabViewPager)highlightWorkspaceView.findViewById(R.id.canvasPager);
        mPagerAdapter = new CanvasPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mEditButton = (FloatingActionButton)highlightWorkspaceView.findViewById(R.id.editBtn);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = mPager.getScrollEnabled();
                if (enabled) {
                    mEditButton.setImageResource(R.drawable.ic_content_copy_black_24dp);
                } else {
                    mEditButton.setImageResource(R.drawable.ic_edit_black_24dp);
                }
                mPager.setScrollEnabled(!enabled);
                for (LyricHighlightFragment fragment : mFragmentList) {
                    fragment.setEditEnabled(enabled);
                }
            }
        });

        mSubmitButton = (FloatingActionButton)highlightWorkspaceView.findViewById(R.id.submitBtn);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, ArrayList<String>> result = processTextImage();
                Intent resultIntent = new Intent(getContext(), ResultDialogActivity.class);
                resultIntent.putExtra("result", result);
                startActivity(resultIntent);
            }
        });

        mRecyclerView = (RecyclerView)highlightWorkspaceView.findViewById(R.id.paletteList);
        mLayoutManager = new LinearLayoutManager(highlightWorkspaceView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new LyricHighlightPaletteAdapter();

        mAdapter.setOnItemClickListener(new LyricHighlightPaletteAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                LyricHighlightPaletteAdapter.PaletteColorStruct item = mAdapter.getItem(position);
                if (item.color == Color.argb(255,255,255,255)) {
                    for (LyricHighlightFragment fragment : mFragmentList) {
                        fragment.setEraserEnabled(true);
                    }
                    return;
                }
                for (LyricHighlightFragment fragment : mFragmentList) {
                    fragment.setEraserEnabled(false);
                    fragment.setColor(item.color);
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);

        return highlightWorkspaceView;
    }

    public void setSheetMusicList(ArrayList<Uri> fileList) {
        initializePalette();
        mFragmentList.clear();
        for (Uri file : fileList) {
            // TODO: for debugging
            mFragmentList.add(LyricHighlightFragment.newInstance(file));
            mFragmentList.add(LyricHighlightFragment.newInstance(file));
        }
        mPagerAdapter.notifyDataSetChanged();
    }

    public void initializePalette() {
        mAdapter.resetItem();
        for (LyricHighlightPaletteAdapter.PaletteColorStruct colorStruct : mPaletteList) {
            mAdapter.addItem(colorStruct);
        }
        mAdapter.notifyDataSetChanged();
    }

    public HashMap<String, ArrayList<String>> processTextImage() {

        Bitmap bitmapImage;
        Bitmap highlightMatrix;

        HashMap<String, ArrayList<String>> result = new HashMap<>();

        int width, height;

        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(MainActivity.DATA_PATH, "eng");

        for (LyricHighlightFragment fragment : mFragmentList) {

            // Create bitmap of same size
            highlightMatrix = fragment.getHighlightMatrix();
            if (highlightMatrix == null) {
                continue;
            }
            width = highlightMatrix.getWidth();
            height = highlightMatrix.getHeight();
            bitmapImage = fragment.getSheetMusic();
            bitmapImage = Bitmap.createScaledBitmap(bitmapImage, width, height, false);

            // Prepare new bitmap to draw on
            ArrayList<Bitmap> extractedTextList = new ArrayList<>();
            ArrayList<Canvas> extractedTextCanvas = new ArrayList<>();
            Paint paint = new Paint();

            Bitmap bitmap;
            Canvas canvas;
            for (int i = 0; i < mPaletteList.size() - 1; i++) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                paint.setColor(Color.WHITE);
                canvas.drawRect(0, 0, width-1, height-1, paint);
                extractedTextList.add(bitmap);
                extractedTextCanvas.add(canvas);
            }

            paint.setColor(Color.BLACK);

            // Extract words from highlighted image
            int color, highlightColor;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    if (highlightMatrix.getPixel(i, j) == 0) {
                        continue;
                    }
                    color = bitmapImage.getPixel(i, j);
                    if (Color.red(color) < 200 && Color.green(color) < 200
                            && Color.blue(color) < 200) {
                        highlightColor = highlightMatrix.getPixel(i, j);

                        for (int k = 0; k < mPaletteList.size() - 1; k++) {
                            if ((mPaletteList.get(k).color | 0xFF000000) == highlightColor) {
                                extractedTextCanvas.get(k).drawPoint(i, j, paint);
                                break;
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < extractedTextList.size(); i++) {
                tessBaseAPI.setImage(extractedTextList.get(i));
                if (result.get(mPaletteList.get(i).name) == null) {
                    result.put(mPaletteList.get(i).name, new ArrayList<String>());
                }
                result.get(mPaletteList.get(i).name).add(tessBaseAPI.getUTF8Text().replace('\n', ' '));
            }
        }
        return result;
    }
}
