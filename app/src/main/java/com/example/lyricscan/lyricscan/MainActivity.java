package com.example.lyricscan.lyricscan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final int FILE_OPEN_REQ_CODE = 1000;
    private static final int WRITE_REQ_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_REQ_CODE);
        }

        Button startButton = (Button)findViewById(R.id.startBtn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileOpenIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileOpenIntent.setType("*/*");
                startActivityForResult(fileOpenIntent, FILE_OPEN_REQ_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case FILE_OPEN_REQ_CODE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Could not open file", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri res = intent.getData();
                Intent lyricHighlightIntent = new Intent(this, LyricHighlightActivity.class);
                lyricHighlightIntent.putExtra("filename", res.getPath());
                startActivity(lyricHighlightIntent);
                break;
            default:

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "123", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

        }
    }
}
