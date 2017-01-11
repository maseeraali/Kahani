package com.example.maseera.kahani;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by maseera on 28/12/16.
 */

public class MovieDetailAdapter extends ArrayAdapter<MovieDetail> {

    private Context mContext;
    private int mResource;
    private ArrayList<MovieDetail> mInfoList;

    public MovieDetailAdapter(Context context, int resource, ArrayList<MovieDetail> infoList) {
        super(context, resource, infoList);
        mContext = context;
        mResource = resource;
        mInfoList = infoList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mResource, parent, false);

            holder = new Holder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.list_item_image);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Picasso.with(mContext)
                .load(mInfoList.get(position).getPosterImgUrl())
                .placeholder(R.drawable.placeholder_poster)
                .into(holder.imageView);
        return convertView;
    }

    static class Holder {
        ImageView imageView;
    }

}

