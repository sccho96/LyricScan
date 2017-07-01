package com.example.lyricscan.lyricscan;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;

/**
 * LyricHighlightActivity (Activity)
 * @author sccho96
 * Description: This activty provides user interface for annotating a sheet music
 *         using highlighters.
 */
public class LyricHighlightActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LyricHighlightPaletteAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LyricHighlightCanvasView mHighlightCanvasView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_highlight);

        Intent intent = getIntent();
        String filename = intent.getStringExtra("filename");
        File mSheetMusic = new File(filename.split(":")[1]);

        mHighlightCanvasView = (LyricHighlightCanvasView)findViewById(R.id.highlightCanvas);
        mHighlightCanvasView.setSheetMusic(mSheetMusic);

        mRecyclerView = (RecyclerView)findViewById(R.id.paletteList);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new LyricHighlightPaletteAdapter();

        // TODO: this is test code
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,0,0), "red"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,0,255,0), "green"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,0,0,255), "blue"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,255,0), "yellow"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,0,255), "purple"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,0,0), "red"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,0,255,0), "green"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,0,0,255), "blue"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,255,0), "yellow"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(100,255,0,255), "purple"));
        mAdapter.addItem(new LyricHighlightPaletteAdapter.PaletteColorStruct(Color.argb(255,255,255,255), "eraser"));

        mAdapter.setOnItemClickListener(new LyricHighlightPaletteAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                LyricHighlightPaletteAdapter.PaletteColorStruct item = mAdapter.getItem(position);
                if (item.color == Color.argb(255,255,255,255)) {
                    mHighlightCanvasView.setEraserEnabled(true);
                    return;
                }
                mHighlightCanvasView.setEraserEnabled(false);
                mHighlightCanvasView.setColor(item.color);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }
}
