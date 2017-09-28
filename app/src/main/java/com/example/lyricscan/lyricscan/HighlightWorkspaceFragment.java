package com.example.lyricscan.lyricscan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.lyricscan.lyricscan.MainActivity.ERROR_TAG;

public class HighlightWorkspaceFragment extends Fragment {

    interface OnGetSettingsListener {
        String onGetLanguageSetting();
    }

    TabViewPager mPager;
    PagerAdapter mPagerAdapter;
    FloatingActionButton mEditButton;
    FloatingActionButton mSubmitButton;

    ArrayList<LyricHighlightFragment> mFragmentList;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    LyricHighlightPaletteAdapter mAdapter;

    TextView mNoFileMsgView;

    ArrayList<LyricHighlightPaletteAdapter.PaletteColorStruct> mPaletteList;

    OnGetSettingsListener onGetSettingsListener;

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

    public void setOnGetSettingsListener(OnGetSettingsListener onGetSettingsListener) {
        this.onGetSettingsListener = onGetSettingsListener;
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
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(LyricHighlightPaletteAdapter
                .AnnotationToolType.ERASER, "eraser"));
        mPaletteList.add(new LyricHighlightPaletteAdapter.PaletteColorStruct(LyricHighlightPaletteAdapter
                .AnnotationToolType.BREAKPOINT, "break"));

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
                if (item.toolType == LyricHighlightPaletteAdapter.AnnotationToolType.ERASER) {
                    for (LyricHighlightFragment fragment : mFragmentList) {
                        fragment.setToolType(LyricHighlightPaletteAdapter.AnnotationToolType.ERASER);
                    }
                    return;
                }/* else if (item.toolType == LyricHighlightPaletteAdapter.AnnotationToolType.BREAKPOINT) {
                    // TODO: breakpoint selected
                    return;
                } */else {
                    for (LyricHighlightFragment fragment : mFragmentList) {
                        fragment.setToolType(LyricHighlightPaletteAdapter.AnnotationToolType.HIGHLIGHTER);
                        fragment.setColor(item.color);
                    }
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);

        mNoFileMsgView = (TextView)highlightWorkspaceView.findViewById(R.id.no_file_msg);

        return highlightWorkspaceView;
    }

    public void setSheetMusicList(ArrayList<Uri> fileList) {
        initializePalette();
        mFragmentList.clear();

        ArrayList<Bitmap> bitmapList = new ArrayList<>();

        for (Uri fileUri : fileList) {
            try {
                String uriString = fileUri.toString();
                String extension = uriString.substring(uriString.lastIndexOf('.'));
                if (extension.equals(".pdf")) {
                    File workspace = new File(MainActivity.DATA_PATH + "workspace");
                    workspace.mkdirs();
                    File pdfFile = File.createTempFile("temp", ".pdf", workspace);
                    InputStream in = getContext().getContentResolver().openInputStream(fileUri);
                    FileOutputStream out = new FileOutputStream(pdfFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    PdfRenderer pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile,
                            ParcelFileDescriptor.MODE_READ_ONLY));
                    for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
                        PdfRenderer.Page page = pdfRenderer.openPage(i);
                        Bitmap sheetMusicBitmap = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888);
                        page.render(sheetMusicBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        bitmapList.add(sheetMusicBitmap);
                        page.close();
                    }
                } else if (extension.equals(".png") || extension.equals(".jpg")){
                    Bitmap sheetMusicBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),
                            fileUri);
                    bitmapList.add(sheetMusicBitmap);
                } else {
                    Toast.makeText(getContext(), "Invalid files type", Toast.LENGTH_SHORT).show();
                }
                if (mNoFileMsgView != null) {
                    mNoFileMsgView.setText("");
                }
            } catch (IOException exception) {
                Log.d(ERROR_TAG, "Error rendering pdf pages.");
            }
        }
        for (Bitmap bitmap : bitmapList) {
            mFragmentList.add(LyricHighlightFragment.newInstance(bitmap));
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

        String setting_lang = onGetSettingsListener.onGetLanguageSetting();

        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(MainActivity.DATA_PATH, setting_lang);

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
