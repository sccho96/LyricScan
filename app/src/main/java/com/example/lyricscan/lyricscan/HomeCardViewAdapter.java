package com.example.lyricscan.lyricscan;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by yaroo on 7/11/17.
 */

public class HomeCardViewAdapter extends RecyclerView.Adapter<HomeCardViewAdapter.ViewHolder>{

    private ArrayList<CardStruct> mCardArrayList;
    private OnItemClickListener onItemClickListener;

    interface OnItemClickListener {
        void onClick(int position);
    }

    public static class CardStruct {
        public Bitmap preview;
        public String title;
        public String category;
        public boolean fileSelect;
        public CardStruct(Bitmap preview, String title) {
            this.preview = preview;
            this.title = title;
            this.category = "";
            this.fileSelect = false;
        }
        public CardStruct(Bitmap preview, String title, String category) {
            this.preview = preview;
            this.title = title;
            this.category = category;
            this.fileSelect = false;
        }
        public CardStruct(Bitmap preview, String title, String category, boolean fileSelect) {
            this.preview = preview;
            this.title = title;
            this.category = category;
            this.fileSelect = fileSelect;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View item;
        ImageView cardImage;
        TextView cardTitle;
        TextView cardCat;

        public ViewHolder (View item) {
            super(item);
            cardImage = (ImageView)item.findViewById(R.id.cardPreview);
            cardTitle = (TextView)item.findViewById(R.id.cardTitle);
            cardCat = (TextView)item.findViewById(R.id.cardCategory);
            this.item = item;
        }
    }

    public HomeCardViewAdapter() {
        mCardArrayList = new ArrayList<CardStruct>();
        onItemClickListener = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_cv, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        holder.cardImage.setImageBitmap(mCardArrayList.get(position).preview);

        holder.cardTitle.setText(mCardArrayList.get(position).title);
        holder.cardCat.setText(mCardArrayList.get(position).category);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null) {
                    onItemClickListener.onClick(pos);
                }
            }
        });
    }

    public int getItemCount() {
        return mCardArrayList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public CardStruct getItem(int position) {
        if(position >= mCardArrayList.size()) {
            return null;
        }
        return mCardArrayList.get(position);
    }

    public void addItem(CardStruct item) {
        mCardArrayList.add(item);
    }
}
