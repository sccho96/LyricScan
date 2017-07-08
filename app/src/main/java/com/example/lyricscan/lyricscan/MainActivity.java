package com.example.lyricscan.lyricscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener {

    public static final int FILE_OPEN_REQ_CODE = 1000;
    public static final int WRITE_REQ_CODE = 1001;
    public static final int NUM_PAGES = 3;
    public static final String TESSDATA = "tessdata";
    public static final String LYRICSCAN = "LyricScan";
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/" + LYRICSCAN + "/";
    public static final String ERROR_TAG = "1234";

    private BottomNavigationView mBottomNavigationView;

    private TabViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private HomeFragment mHomeFragment;
    private HighlightWorkspaceFragment mHighlightWorkspaceFragment;
    private SettingsFragment mSettingsFragment;

    private boolean enabled = false;

    private class TabPagerAdapter extends FragmentStatePagerAdapter {
        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mHomeFragment = HomeFragment.newInstance();
                    return mHomeFragment;
                case 1:
                    mHighlightWorkspaceFragment = HighlightWorkspaceFragment.newInstance();
                    return mHighlightWorkspaceFragment;
                case 2:
                    mSettingsFragment = SettingsFragment.newInstance();
                    return mSettingsFragment;
                default:
                    mHomeFragment = HomeFragment.newInstance();
                    return mHomeFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public void onFilesChanged(Uri uri) {
        if (mHighlightWorkspaceFragment != null) {
            ArrayList<Uri> uriList = new ArrayList<>();
            uriList.add(uri);
            mHighlightWorkspaceFragment.setSheetMusicList(uriList);
            enabled = true;
            mBottomNavigationView.setSelectedItemId(R.id.workspace_tab);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_REQ_CODE);
        } else {
            prepareDataPath();
        }

        mPager = (TabViewPager)findViewById(R.id.pager);
        mPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mBottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_tab:
                        mPager.setCurrentItem(0, true);
                        return true;
                    case R.id.workspace_tab:
                        mPager.setCurrentItem(1, true);
                        return true;
                    case R.id.settings_tab:
                        mPager.setCurrentItem(2, true);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            default:

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "File Write Permission Granted", Toast.LENGTH_SHORT).show();
                    prepareDataPath();
                } else {
                    Toast.makeText(getApplicationContext(), "File Write Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

        }
    }

    private void prepareDataPath() {
        /* create LyricScan/tessdata directory if it doesn't exist */
        File newDir = new File(DATA_PATH + TESSDATA);
        if (!newDir.exists() && !newDir.mkdirs()) {
            Log.e(ERROR_TAG, "Data path directory not created");
            return;
        }

        /* check if the tessdata directory contains required data files */
        File data1 = new File(DATA_PATH + TESSDATA + "/" + "eng.traineddata");
        File data2 = new File(DATA_PATH + TESSDATA + "/" + "kor.traineddata");
        if (data1.exists() && data2.exists()) {
            return;
        }

        /* copy data files to the new directory */
        try {
            String[] fileList = getAssets().list(TESSDATA);
            for (String filename : fileList) {
                InputStream in = getAssets().open(TESSDATA + "/" + filename);
                OutputStream out = new FileOutputStream(DATA_PATH + TESSDATA + "/" + filename);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }

        } catch (IOException e) {
            Log.e(ERROR_TAG, "Error: " + e.getMessage());
        }
    }
}
