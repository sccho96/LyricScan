package com.example.lyricscan.lyricscan;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ResultDialogActivity extends Activity {

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_dialog);

        mTextView = (TextView)findViewById(R.id.resultText);
        Intent intent = getIntent();
        HashMap<String, ArrayList<String>> result = (HashMap<String, ArrayList<String>>)intent.getExtras().get("result");
        Iterator it = result.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            List<String> list = (List)pair.getValue();
            mTextView.append((String)pair.getKey() + " : ");
            if (list.isEmpty()) {
                mTextView.append("\n");
                continue;
            }
            for (String value : list) {
                mTextView.append(value + "\n");
            }
        }
    }

}
