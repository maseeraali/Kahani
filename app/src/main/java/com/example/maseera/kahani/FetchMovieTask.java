package com.example.maseera.kahani;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by maseera on 12/1/17.
 */
public class FetchMovieTask extends AsyncTask<String,Void, Void> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    MovieAdapter2 movieAdapter;
    private final Context mContext;

    public FetchMovieTask(Context context,MovieAdapter2 movieAdapter) {
        this.mContext = context;
        this.movieAdapter = movieAdapter;
    }


    public static final String[] MOVIE_COLUMNS = {

            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_IMAGE,
            MovieContract.MovieEntry.COLUMN_IMAGE2,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_DATE
    };

    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_IMAGE = 3;
    public static final int COL_IMAGE2 = 4;
    public static final int COL_OVERVIEW = 5;
    public static final int COL_RATING = 6;
    public static final int COL_DATE = 7;


    private void getMovieDataFromJson(String movieJsonStr, String orderBy) throws JSONException {

            try {
                String imgBaseUrl = "http://image.tmdb.org/t/p/";
                JSONObject jsonObject = new JSONObject(movieJsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                Vector<ContentValues> cVVector = new Vector<ContentValues>(jsonArray.length());

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

                    ContentValues values = new ContentValues();

                    if (orderBy.equalsIgnoreCase("popular")) {

                        values.put(MovieContract.PopularEntry.COLUMN_MOVIE_ID, id);
                        values.put(MovieContract.PopularEntry.COLUMN_TITLE,title);
                        values.put(MovieContract.PopularEntry.COLUMN_IMAGE, posterImgUrl);
                        values.put(MovieContract.PopularEntry.COLUMN_IMAGE2, backdropImgUrl);
                        values.put(MovieContract.PopularEntry.COLUMN_OVERVIEW,synopsis);
                        values.put(MovieContract.PopularEntry.COLUMN_RATING, voteAvg);
                        values.put(MovieContract.PopularEntry.COLUMN_DATE, releaseDate);

                    } else {
                        values.put(MovieContract.RatedEntry.COLUMN_MOVIE_ID, id);
                        values.put(MovieContract.RatedEntry.COLUMN_TITLE, title);
                        values.put(MovieContract.RatedEntry.COLUMN_IMAGE, posterImgUrl);
                        values.put(MovieContract.RatedEntry.COLUMN_IMAGE2, backdropImgUrl);
                        values.put(MovieContract.RatedEntry.COLUMN_OVERVIEW, synopsis);
                        values.put(MovieContract.RatedEntry.COLUMN_RATING, voteAvg);
                        values.put(MovieContract.RatedEntry.COLUMN_DATE, releaseDate);

                    }

                    cVVector.add(values);
                }
                int inserted = 0;
                    // add to database
                    if ( cVVector.size() > 0 ) {

                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        if(orderBy.equalsIgnoreCase("popular")) {
                            inserted = mContext.getContentResolver().bulkInsert(MovieContract.PopularEntry.CONTENT_URI, cvArray);
                            Log.v(LOG_TAG, "insert " + inserted);
                        }
                        else {
                            inserted = mContext.getContentResolver().bulkInsert(MovieContract.RatedEntry.CONTENT_URI, cvArray);
                            Log.v(LOG_TAG, "insert " + inserted);
                        }
                    }

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            }

        @Override
        protected Void doInBackground (String...params){
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            try {

                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie";

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

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "JsonStr " + movieJsonStr);
                getMovieDataFromJson(movieJsonStr,params[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

            } catch (JSONException e) {
                e.printStackTrace();
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

            return null;
        }

}