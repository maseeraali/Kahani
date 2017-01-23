package com.example.maseera.kahani;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.maseera.kahani.Model.MovieDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by maseera on 12/1/17.
 */
public class FetchMovieTask extends AsyncTask<String,Void, ArrayList<MovieDetail>> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    MovieAdapter movieAdapter;
    private final Context mContext;

    public FetchMovieTask(Context context,MovieAdapter movieAdapter) {
        this.mContext = context;
        this.movieAdapter = movieAdapter;
    }


    public static final String[] MOVIE_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_IMAGE,
            MovieContract.MovieEntry.COLUMN_IMAGE2,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_DATE
    };
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_MOVIE_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_IMAGE = 2;
    public static final int COL_IMAGE2 = 3;
    public static final int COL_OVERVIEW = 4;
    public static final int COL_RATING = 5;
    public static final int COL_DATE = 6;


    private ArrayList<MovieDetail> getMovieDataFromJson(String movieJsonStr) throws JSONException {
        ArrayList<MovieDetail> movieList = new ArrayList<MovieDetail>();
            try {
                String imgBaseUrl = "http://image.tmdb.org/t/p/";
                JSONObject jsonObject = new JSONObject(movieJsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    String posterImgUrl = object.getString("poster_path");
                    String backdropImgUrl = object.getString("backdrop_path");
                    String title = object.getString("original_title");
                    String synopsis = object.getString("overview");
                    String releaseDate = object.getString("release_date");
                    double voteAvg = object.getDouble("vote_average");
                    int id = object.getInt("id");

                    posterImgUrl = imgBaseUrl + "w342" + posterImgUrl;

                    if (!backdropImgUrl.equals("null")) {
                        backdropImgUrl = imgBaseUrl + "w500" + backdropImgUrl;
                    }
                    MovieDetail info = new MovieDetail(id,title, posterImgUrl, backdropImgUrl,
                            synopsis, releaseDate, voteAvg);

                    movieList.add(info);
                }

                return movieList;
            } catch (JSONException e) {
                movieList = new ArrayList<MovieDetail>();
            }
        return movieList;
    }


        @Override
        protected ArrayList<MovieDetail> doInBackground (String...params){
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {

                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie";
                //     final String QUERY_PARAM = "sort_by";
                final String APPID_PARAM = "api_key";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());


                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "JsonStr " + movieJsonStr);
           //     getMovieDataFromJson(movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
    @Override
    protected void onPostExecute(ArrayList<MovieDetail> result) {
        if (result != null) {
            movieAdapter.clear();
            for (MovieDetail info : result) {
                movieAdapter.add(info);

            }

        }
        else {
            Toast.makeText(mContext, "Something went wrong, please check your internet connection and try again later! ",Toast.LENGTH_LONG).show();
        }
    }

//
//    public class FetchFavoriteMoviesTask extends AsyncTask<Void, Void, ArrayList<MovieDetail>> {
//
//        private Context mContext;
//
//        public FetchFavoriteMoviesTask(Context context) {
//            mContext = context;
//        }
//
//        private ArrayList<MovieDetail> getFavoriteMoviesDataFromCursor(Cursor cursor) {
//            ArrayList<MovieDetail> results = new ArrayList<>();
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    MovieDetail movie = new MovieDetail(cursor.getInt(COL_MOVIE_ID),
//                            cursor.getString(COL_TITLE),cursor.getString(COL_IMAGE),
//                            cursor.getString(COL_IMAGE2),cursor.getString(COL_OVERVIEW),cursor.getString(COL_DATE),
//                            cursor.getDouble(COL_RATING));
//                    results.add(movie);
//                } while (cursor.moveToNext());
//                cursor.close();
//            }
//            return results;
//        }
//
//        @Override
//        protected ArrayList<MovieDetail> doInBackground(Void... params) {
//            Cursor cursor = mContext.getContentResolver().query(
//                    MovieContract.MovieEntry.CONTENT_URI,
//                    MOVIE_COLUMNS,
//                    null,
//                    null,
//                    null
//            );
//            return getFavoriteMoviesDataFromCursor(cursor);
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<MovieDetail> result) {
//            if (result != null) {
//                movieAdapter.clear();
//                for (MovieDetail info : result) {
//                    movieAdapter.add(info);
//                }
//            }
//        }
//
//    }
}