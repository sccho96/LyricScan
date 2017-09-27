package com.example.lyricscan.lyricscan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private HomeCardViewAdapter mAdapter;

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

        mRecyclerView = (RecyclerView)homeFragmentView.findViewById(R.id.homeRV);
        mLayoutManager = new LinearLayoutManager(homeFragmentView.getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new HomeCardViewAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new HomeCardViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                HomeCardViewAdapter.CardStruct item = mAdapter.getItem(position);
                if (item.fileSelect) {
                    Intent fileOpenIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileOpenIntent.setType("*/*");
                    startActivityForResult(fileOpenIntent, MainActivity.FILE_OPEN_REQ_CODE);
                }
            }
        });

        mAdapter.addItem(new HomeCardViewAdapter.CardStruct(null, "Files", "Load image from file", true));
        mAdapter.addItem(new HomeCardViewAdapter.CardStruct(null, "title1", "category1"));
        mAdapter.addItem(new HomeCardViewAdapter.CardStruct(null, "title2", "category2"));
        mAdapter.addItem(new HomeCardViewAdapter.CardStruct(null, "title3", "category3"));
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
