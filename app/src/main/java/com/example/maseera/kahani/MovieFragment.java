package com.example.maseera.kahani;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.maseera.kahani.Model.MovieDetail;

import java.util.ArrayList;

import static com.example.maseera.kahani.FetchMovieTask.COL_DATE;
import static com.example.maseera.kahani.FetchMovieTask.COL_IMAGE;
import static com.example.maseera.kahani.FetchMovieTask.COL_IMAGE2;
import static com.example.maseera.kahani.FetchMovieTask.COL_MOVIE_ID;
import static com.example.maseera.kahani.FetchMovieTask.COL_OVERVIEW;
import static com.example.maseera.kahani.FetchMovieTask.COL_RATING;
import static com.example.maseera.kahani.FetchMovieTask.COL_TITLE;
import static com.example.maseera.kahani.FetchMovieTask.MOVIE_COLUMNS;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private MovieAdapter movieAdapter;
    private static final String FAVORITE = "favorite";
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    private ArrayList<MovieDetail> mMovies = new ArrayList<>();

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    public interface Callback {
        void onItemSelected(MovieDetail movie);
    }

    private void updateList() {
        String orderBy = Utility.getPreferredOrder(getActivity());
        if (!orderBy.contentEquals(FAVORITE)) {
            FetchMovieTask movieTask = new FetchMovieTask(getActivity(),movieAdapter);
            movieTask.execute(orderBy);
        } else {
            new FetchFavoriteMoviesTask(getActivity()).execute();
        }




    }

    @Override
    public void onStart()  {
        super.onStart();
        updateList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity(),R.layout.list_item_movie, mMovies);
        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);

        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                MovieDetail movie =  movieAdapter.getItem(position);
                    ((Callback) getActivity())
                            .onItemSelected(movie);
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    public class FetchFavoriteMoviesTask extends AsyncTask<Void, Void, ArrayList<MovieDetail>> {

        private Context mContext;

        public FetchFavoriteMoviesTask(Context context) {
            mContext = context;
        }

        private ArrayList<MovieDetail> getFavoriteMoviesDataFromCursor(Cursor cursor) {
            ArrayList<MovieDetail> results = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MovieDetail movie = new MovieDetail(cursor.getInt(COL_MOVIE_ID),
                            cursor.getString(COL_TITLE),cursor.getString(COL_IMAGE),
                            cursor.getString(COL_IMAGE2),cursor.getString(COL_OVERVIEW),cursor.getString(COL_DATE),
                            cursor.getDouble(COL_RATING));
                    results.add(movie);
                } while (cursor.moveToNext());
                cursor.close();
            }
            return results;
        }

        @Override
        protected ArrayList<MovieDetail> doInBackground(Void... params) {
            Cursor cursor = mContext.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
            return getFavoriteMoviesDataFromCursor(cursor);
        }

        @Override
        protected void onPostExecute(ArrayList<MovieDetail> result) {
            if (result != null) {
                movieAdapter.clear();
                for (MovieDetail info : result) {
                    movieAdapter.add(info);
                }
            }
        }

    }
}

