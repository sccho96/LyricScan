package com.example.lyricscan.lyricscan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Empty constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
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
        // Inflate the layout for this fragment
        View homeFragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        Button startButton = (Button)homeFragmentView.findViewById(R.id.startBtn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileOpenIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileOpenIntent.setType("*/*");
                startActivityForResult(fileOpenIntent, MainActivity.FILE_OPEN_REQ_CODE);
            }
        });
        return homeFragmentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case MainActivity.FILE_OPEN_REQ_CODE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getContext(), "Could not open file", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri res = intent.getData();
                mListener.onFilesChanged(res);
                break;
            default:

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFilesChanged(Uri uri);
    }

}
