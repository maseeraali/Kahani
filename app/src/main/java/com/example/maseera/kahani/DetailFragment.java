package com.example.maseera.kahani;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maseera.kahani.model.Review;
import com.example.maseera.kahani.model.Trailer;
import com.linearlistview.LinearListView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.maseera.kahani.FetchMovieTask.COL_DATE;
import static com.example.maseera.kahani.FetchMovieTask.COL_IMAGE;
import static com.example.maseera.kahani.FetchMovieTask.COL_IMAGE2;
import static com.example.maseera.kahani.FetchMovieTask.COL_MOVIE_ID;
import static com.example.maseera.kahani.FetchMovieTask.COL_OVERVIEW;
import static com.example.maseera.kahani.FetchMovieTask.COL_RATING;
import static com.example.maseera.kahani.FetchMovieTask.COL_TITLE;
import static com.example.maseera.kahani.FetchMovieTask.MOVIE_COLUMNS;
import static com.example.maseera.kahani.MovieFragment.POPULAR_COLUMNS;
import static com.example.maseera.kahani.MovieFragment.RATED_COLUMNS;

/**
 * Created by maseera on 23/1/17.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private Uri mUri;
    private String[] COLUMNS;
    private int id;
    private String title;
    private String posterImgUrl;
    private String backdropImgUrl;
    private String synopsis;
    private String releaseDate;
    private double voteAvg;
    private static final String POPULAR = "popular";
    private static final String RATED = "top_rated";
    private static final int DETAIL_LOADER = 0;

    @BindView(R.id.detail_backdrop_image_view)
    ImageView mBackDropImageView;
    @BindView(R.id.detail_poster_image_view)
    ImageView mPosterImageView;
    @BindView(R.id.detail_rating_text_view)
    TextView mRatingTextView;
    @BindView(R.id.detail_rating_bar)
    RatingBar mRatingBar;
    @BindView(R.id.detail_release_date_text_view)
    TextView mReleaseDateTextView;
    @BindView(R.id.detail_synopsis_text_view)
    TextView mSynopsisTextView;
    @BindView(R.id.detail_trailers)
    LinearListView mTrailersView;
    @BindView(R.id.detail_reviews)
    LinearListView mReviewsView;
    @BindView(R.id.detail_reviews_cardview)
    CardView mReviewsCardview;
    @BindView(R.id.detail_trailers_cardview)
    CardView mTrailersCardview;


    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private ScrollView mDetailLayout;
    private Toast mToast;
    private MenuItem action_favorite;
    private ShareActionProvider mShareActionProvider;

    private Trailer mTrailer;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mUri != null) {
            inflater.inflate(R.menu.menu_fragment_detail, menu);

            action_favorite = menu.findItem(R.id.action_favorite);
            MenuItem action_share = menu.findItem(R.id.action_share);

            action_favorite.setIcon(Utility.isFavorited(getActivity(),id) == 1 ?
                    R.drawable.abc_btn_rating_star_on_mtrl_alpha :
                    R.drawable.abc_btn_rating_star_off_mtrl_alpha);
            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    return Utility.isFavorited(getActivity(),id);
                }

                @Override
                protected void onPostExecute(Integer isFavorited) {
                    action_favorite.setIcon(isFavorited == 1 ?
                            R.drawable.abc_btn_rating_star_on_mtrl_alpha :
                            R.drawable.abc_btn_rating_star_off_mtrl_alpha);
                }
            }.execute();

            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_share);

            if (mTrailer != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int movie_id = item.getItemId();
        switch (movie_id) {
            case R.id.action_favorite:
                if (mUri != null) {
                    // check if movie is in favorites or not
                    new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... params) {
                            return Utility.isFavorited(getActivity(), id);
                        }

                        @Override
                        protected void onPostExecute(Integer isFavorited) {
                            // if it is in favorites
                            if (isFavorited == 1) {
                                // delete from favorites
                                new AsyncTask<Void, Void, Integer>() {
                                    @Override
                                    protected Integer doInBackground(Void... params) {
                                        return getActivity().getContentResolver().delete(
                                                MovieContract.MovieEntry.CONTENT_URI,
                                                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                                                new String[]{Integer.toString(id)}
                                        );
                                    }

                                    @Override
                                    protected void onPostExecute(Integer rowsDeleted) {
                                        item.setIcon(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
                                        if (mToast != null) {
                                            mToast.cancel();
                                        }
                                        mToast = Toast.makeText(getActivity(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT);
                                        mToast.show();
                                    }
                                }.execute();
                            }
                            // if it is not in favorites
                            else {
                                // add to favorites
                                new AsyncTask<Void, Void, Uri>() {
                                    @Override
                                    protected Uri doInBackground(Void... params) {
                                        ContentValues values = new ContentValues();

                                        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, id);
                                        values.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                                        values.put(MovieContract.MovieEntry.COLUMN_IMAGE, posterImgUrl);
                                        values.put(MovieContract.MovieEntry.COLUMN_IMAGE2, backdropImgUrl);
                                        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, synopsis);
                                        values.put(MovieContract.MovieEntry.COLUMN_RATING, voteAvg);
                                        values.put(MovieContract.MovieEntry.COLUMN_DATE, releaseDate);

                                        return getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                                values);
                                    }

                                    @Override
                                    protected void onPostExecute(Uri returnUri) {
                                        item.setIcon(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
                                        if (mToast != null) {
                                            mToast.cancel();
                                        }
                                        mToast = Toast.makeText(getActivity(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT);
                                        mToast.show();
                                    }
                                }.execute();
                            }
                        }
                    }.execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public String parseDateString(String date) {
        String[] parts = date.split("-");
        int year = Integer.valueOf(parts[0]);
        int month = Integer.valueOf(parts[1]);
        int day = Integer.valueOf(parts[2]);

        return new DateFormatSymbols().getMonths()[month - 1] + " " + day + ", " + year;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onOrderChanged() {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " +
                "http://www.youtube.com/watch?v=" + mTrailer.getKey());
        return shareIntent;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            String movie_id;
            Uri updatedUri;
            String orderBy = Utility.getPreferredOrder(getActivity());
            switch (orderBy) {
                case POPULAR:
                    movie_id = MovieContract.PopularEntry.getIdFromUri(mUri);
                    COLUMNS = POPULAR_COLUMNS;
                    updatedUri = MovieContract.PopularEntry.CONTENT_URI;
                    break;
                case RATED:
                    movie_id = MovieContract.RatedEntry.getIdFromUri(mUri);
                    COLUMNS = RATED_COLUMNS;
                    updatedUri = MovieContract.RatedEntry.CONTENT_URI;
                    break;
                default:
                    movie_id = MovieContract.MovieEntry.getIdFromUri(mUri);
                    COLUMNS = MOVIE_COLUMNS;
                    updatedUri = MovieContract.MovieEntry.CONTENT_URI;
            }
            mUri = updatedUri;
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    COLUMNS,
                    COLUMNS[1] + "=?",
                    new String[]{movie_id},
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {

            id = cursor.getInt(COL_MOVIE_ID);
            new FetchTrailersTask().execute(Integer.toString(id));
            new FetchReviewsTask().execute(Integer.toString(id));
            title = cursor.getString(COL_TITLE);
            posterImgUrl = cursor.getString(COL_IMAGE);
            backdropImgUrl = cursor.getString(COL_IMAGE2);
            synopsis = cursor.getString(COL_OVERVIEW);
            releaseDate = cursor.getString(COL_DATE);
            voteAvg = cursor.getDouble(COL_RATING);

            mDetailLayout = (ScrollView) getView().findViewById(R.id.fragment_detail);
            ButterKnife.bind(this, mDetailLayout);
            mDetailLayout.setVisibility(View.VISIBLE);

            mTrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());
            mTrailersView.setAdapter(mTrailerAdapter);

            mTrailersView.setOnItemClickListener(new LinearListView.OnItemClickListener() {
                @Override
                public void onItemClick(LinearListView linearListView, View view,
                                        int position, long id) {
                    Trailer trailer = mTrailerAdapter.getItem(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));

                    // Verify it resolves
                    PackageManager packageManager = getActivity().getPackageManager();
                    List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                    boolean isIntentSafe = activities.size() > 0;

                    // Start an activity if it's safe
                    if (isIntentSafe) {
                        startActivity(intent);
                    }
                }
            });

            mReviewAdapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
            mReviewsView.setAdapter(mReviewAdapter);

            if (backdropImgUrl.equals("null")) {
                mBackDropImageView.setImageResource(R.drawable.placeholder_null);
            } else {
                Picasso.with(getContext())
                        .load(backdropImgUrl)
                        .placeholder(R.drawable.placeholder_backdrop)
                        .into(mBackDropImageView);
            }

            Picasso.with(getContext())
                    .load(posterImgUrl)
                    .placeholder(R.drawable.placeholder_poster)
                    .into(mPosterImageView);

            // Avoid showing floats when the value is a whole number ie. 7.0 / 10
            if (voteAvg == (int) voteAvg) {
                mRatingTextView.setText((int) voteAvg + " / 10");
            } else {
                mRatingTextView.setText(voteAvg + " / 10");
            }

            mRatingBar.setRating((float) voteAvg / 2);
            mReleaseDateTextView.setText(parseDateString(releaseDate));
            mSynopsisTextView.setText(synopsis);
        }
        cursor.close();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject trailerJson = new JSONObject(jsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray("results");

            List<Trailer> results = new ArrayList<>();

            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                // Only show Trailers which are on Youtube
                if (trailer.getString("site").contentEquals("YouTube")) {
                    Trailer trailerModel = new Trailer(trailer);
                    results.add(trailerModel);
                }
            }

            return results;
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
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
                return getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null) {
                if (trailers.size() > 0) {
                    mTrailersCardview.setVisibility(View.VISIBLE);
                    if (mTrailerAdapter != null) {
                        mTrailerAdapter.clear();
                        for (Trailer trailer : trailers) {
                            mTrailerAdapter.add(trailer);
                        }
                    }

                    mTrailer = trailers.get(0);
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareMovieIntent());
                    }
                }
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private List<Review> getReviewsDataFromJson(String jsonStr) throws JSONException {
            JSONObject reviewJson = new JSONObject(jsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray("results");

            List<Review> results = new ArrayList<>();

            for (int i = 0; i < reviewArray.length(); i++) {
                JSONObject review = reviewArray.getJSONObject(i);
                results.add(new Review(review));
            }

            return results;
        }

        @Override
        protected List<Review> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
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
                return getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null) {
                if (reviews.size() > 0) {
                    mReviewsCardview.setVisibility(View.VISIBLE);
                    if (mReviewAdapter != null) {
                        mReviewAdapter.clear();
                        for (Review review : reviews) {
                            mReviewAdapter.add(review);
                        }
                    }
                }
            }
        }
    }
}