package com.example.maseera.kahani;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MovieDetailActivity extends AppCompatActivity {
    private MovieDetail mMovieInfo = new MovieDetail("title", "url", "url", "synopsis", "date", 5);


    @InjectView(R.id.detail_backdrop_image_view) ImageView mBackDropImageView;
    @InjectView(R.id.detail_poster_image_view) ImageView mPosterImageView;
    @InjectView(R.id.detail_rating_text_view) TextView mRatingTextView;
    @InjectView(R.id.detail_rating_bar) RatingBar mRatingBar;
    @InjectView(R.id.detail_release_date_text_view) TextView mReleaseDateTextView;
    @InjectView(R.id.detail_synopsis_text_view) TextView mSynopsisTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ButterKnife.inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            mMovieInfo = intent.getParcelable("EXTRA_TEXT");
        }
        getSupportActionBar().setTitle(mMovieInfo.getTitle());
        // If we don't get useable backdrop value, show a colored background matching the primary
        // app color.


        String backdropImgUrl = mMovieInfo.getBackdropImgUrl();
        if (backdropImgUrl.equals("null")) {
            mBackDropImageView.setImageResource(R.drawable.placeholder_null);
        } else {
            Picasso.with(this)
                    .load(backdropImgUrl)
                    .placeholder(R.drawable.placeholder_backdrop)
                    .into(mBackDropImageView);
        }

        Picasso.with(this)
                .load(mMovieInfo.getPosterImgUrl())
                .placeholder(R.drawable.placeholder_poster)
                .into(mPosterImageView);

        // Avoid showing floats when the value is a whole number ie. 7.0 / 10
        double rating = mMovieInfo.getVoteAvg();
        if (rating == (int) rating) {
            mRatingTextView.setText((int) rating + " / 10");
        } else {
            mRatingTextView.setText(rating + " / 10");
        }

        mRatingBar.setRating((float) mMovieInfo.getVoteAvg()/2);
        mReleaseDateTextView.setText(parseDateString(mMovieInfo.getReleaseDate()));
        mSynopsisTextView.setText(mMovieInfo.getSynopsis());

    }

    public String parseDateString(String date) {
        String[] parts = date.split("-");
        int year = Integer.valueOf(parts[0]);
        int month = Integer.valueOf(parts[1]);
        int day = Integer.valueOf(parts[2]);

        return new DateFormatSymbols().getMonths()[month - 1] + " " + day + ", " + year;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

