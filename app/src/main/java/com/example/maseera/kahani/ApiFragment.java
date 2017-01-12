package com.example.maseera.kahani;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
 * A placeholder fragment containing a simple view.
 */
public class ApiFragment extends Fragment {

    int length = 20;
    private MovieDetailAdapter movieAdapter;
    private ArrayList<MovieDetail> mCurrentMovieList = new ArrayList<>();

    public ApiFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieDetailAdapter(getActivity(), R.layout.list_item_movie, mCurrentMovieList);

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MovieDetail movie = movieAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class).putExtra("EXTRA_TEXT", movie);
                startActivity(intent);
            }
        });
        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void updateList() {
        FetchMovieTask movieTask = new FetchMovieTask();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String orderBy = pref.getString(getString(R.string.order), getString(R.string.default_order));
        movieTask.execute(orderBy);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<MovieDetail>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected ArrayList<MovieDetail> doInBackground(String... params) {
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
                Log.v(LOG_TAG, "Movie JSON String" + movieJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
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

            if (movieJsonStr == null) {
                return new ArrayList<MovieDetail>();
            } else {
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

                        posterImgUrl = imgBaseUrl + "w342" + posterImgUrl;

                        if (!backdropImgUrl.equals("null")) {
                            backdropImgUrl = imgBaseUrl + "w500" + backdropImgUrl;
                        }

                        MovieDetail info = new MovieDetail(title, posterImgUrl, backdropImgUrl,
                                synopsis, releaseDate, voteAvg);

                        movieList.add(info);
                    }

                    return movieList;
                } catch (JSONException e) {
                    movieList = new ArrayList<MovieDetail>();
                }

                return movieList;
            }
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

