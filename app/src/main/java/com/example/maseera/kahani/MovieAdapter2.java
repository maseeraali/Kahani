package com.example.maseera.kahani;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by maseera on 26/1/17.
 */

public class MovieAdapter2 extends android.widget.CursorAdapter{

        public MovieAdapter2(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

    static class Holder {
        ImageView imageView;
        TextView textView;
    }

        /*
            Remember that these views are reused as needed.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
            Holder holder=new Holder();
            holder.imageView = (ImageView) view.findViewById(R.id.list_item_image);
            holder.textView = (TextView) view.findViewById(R.id.list_item_title);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            Holder holder = (Holder) view.getTag();
            holder.textView.setText(cursor.getString(2));
            Picasso.with(context)
                    .load(cursor.getString(3))
                    .placeholder(R.drawable.placeholder_poster)
                    .into(holder.imageView);
        }

}

