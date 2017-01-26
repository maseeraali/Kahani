package com.example.maseera.kahani;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import static com.example.maseera.kahani.FetchMovieTask.COL_MOVIE_ID;
import static com.example.maseera.kahani.FetchMovieTask.MOVIE_COLUMNS;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private MovieAdapter2 movieAdapter;
    private static final String FAVORITE = "favorite";
    private static final String POPULAR = "popular";
    private static final String RATED = "top_rated";
    private GridView gridView;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final int MOVIE_LOADER = 0;
    private Uri uri;
    private  String[] COLUMNS;
    public static final String[] POPULAR_COLUMNS = {

            MovieContract.PopularEntry.TABLE_NAME + "." + MovieContract.PopularEntry._ID,
            MovieContract.PopularEntry.COLUMN_MOVIE_ID,
            MovieContract.PopularEntry.COLUMN_TITLE,
            MovieContract.PopularEntry.COLUMN_IMAGE,
            MovieContract.PopularEntry.COLUMN_IMAGE2,
            MovieContract.PopularEntry.COLUMN_OVERVIEW,
            MovieContract.PopularEntry.COLUMN_RATING,
            MovieContract.PopularEntry.COLUMN_DATE
    };
    public static final String[] RATED_COLUMNS = {

            MovieContract.RatedEntry.TABLE_NAME + "." + MovieContract.RatedEntry._ID,
            MovieContract.RatedEntry.COLUMN_MOVIE_ID,
            MovieContract.RatedEntry.COLUMN_TITLE,
            MovieContract.RatedEntry.COLUMN_IMAGE,
            MovieContract.RatedEntry.COLUMN_IMAGE2,
            MovieContract.RatedEntry.COLUMN_OVERVIEW,
            MovieContract.RatedEntry.COLUMN_RATING,
            MovieContract.RatedEntry.COLUMN_DATE
    };

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        updateList();
        super.onCreate(savedInstanceState);
    }



    public interface Callback {
        void onItemSelected(Uri uri);
    }

    private void updateList() {
        String orderBy = Utility.getPreferredOrder(getActivity());
        if (!orderBy.contentEquals(FAVORITE)) {
            FetchMovieTask movieTask = new FetchMovieTask(getActivity(),movieAdapter);
            movieTask.execute(orderBy);
        } else {
            new FetchSavedMoviesTask(getActivity()).execute(orderBy);
        }




    }

    void onOrderChanged( ) {
        updateList();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter2(getActivity(),null,0);
        // Get a reference to the ListView, and attach this adapter to it.
        gridView = (GridView) rootView.findViewById(R.id.gridview_movie);

        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener((new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String orderBy = Utility.getPreferredOrder(getActivity());
                    switch (orderBy) {
                        case POPULAR:
                            ((Callback) getActivity())
                                    .onItemSelected(MovieContract.PopularEntry.buildPopularMovieUri(cursor.getLong(COL_MOVIE_ID)
                                    ));
                            break;
                        case RATED:
                            ((Callback) getActivity())
                                    .onItemSelected(MovieContract.RatedEntry.buildRatedMovieUri(cursor.getLong(COL_MOVIE_ID)
                                    ));
                            break;
                        default:
                            ((Callback) getActivity())
                                    .onItemSelected(MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID)
                                    ));
                    }
                }
                mPosition = position;

            }
        }));

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {

            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String orderBy = Utility.getPreferredOrder(getActivity());
        switch (orderBy) {
            case POPULAR: uri = MovieContract.PopularEntry.CONTENT_URI;
                COLUMNS = POPULAR_COLUMNS;
                break;
            case RATED: uri = MovieContract.RatedEntry.CONTENT_URI;
                COLUMNS = RATED_COLUMNS;
                break;
            default:uri = MovieContract.MovieEntry.CONTENT_URI;
                COLUMNS = MOVIE_COLUMNS;
        }

        return new CursorLoader(getActivity(),
                uri,
                COLUMNS,
                null,
                null,
                null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null) {
            movieAdapter.swapCursor(data);
            if (mPosition != GridView.INVALID_POSITION) {

                gridView.smoothScrollToPosition(mPosition);
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);
    }

    public class FetchSavedMoviesTask extends AsyncTask<String , Void, Cursor> {


        private Context mContext;

        public FetchSavedMoviesTask(Context context) {
            mContext = context;
        }

        @Override
        protected Cursor doInBackground(String... params) {
            switch (params[0]){
                case POPULAR: uri = MovieContract.PopularEntry.CONTENT_URI;
                        COLUMNS = POPULAR_COLUMNS;
                break;
                case RATED: uri = MovieContract.RatedEntry.CONTENT_URI;
                    COLUMNS = RATED_COLUMNS;
                    break;
                default:uri = MovieContract.MovieEntry.CONTENT_URI;
                    COLUMNS = MOVIE_COLUMNS;
            }

            return  mContext.getContentResolver().query(
                    uri,
                    COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        protected void onPostExecute( Cursor data) {
            if (data != null) {
                movieAdapter.swapCursor(data);
                if (mPosition != GridView.INVALID_POSITION) {

                    gridView.smoothScrollToPosition(mPosition);
                }
            }
        }
    }
}

