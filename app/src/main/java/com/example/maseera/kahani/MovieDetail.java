package com.example.maseera.kahani;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by maseera on 28/12/16.
 */


public class MovieDetail implements Parcelable {
    protected String title;
    protected String posterImgUrl;
    protected String backdropImgUrl;
    protected String synopsis;
    protected String releaseDate;
    protected double voteAvg;

    public MovieDetail(String title, String posterImgUrl, String backdropImgUrl,
                     String synopsis, String releaseDate, double voteAvg) {
        this.title = title;
        this.posterImgUrl = posterImgUrl;
        this.backdropImgUrl = backdropImgUrl;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.voteAvg = voteAvg;
    }

    public MovieDetail(Parcel in) {
        String[] strings = new String[5];
        in.readStringArray(strings);

        title = strings[0];
        posterImgUrl = strings[1];
        backdropImgUrl = strings[2];
        synopsis = strings[3];
        releaseDate = strings[4];

        double[] doubles = new double[1];
        in.readDoubleArray(doubles);

        voteAvg = doubles[0];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                title,
                posterImgUrl,
                backdropImgUrl,
                synopsis,
                releaseDate,
        });

        dest.writeDoubleArray(new double[] {
                voteAvg,
        });
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public double getVoteAvg() {
        return voteAvg;
    }

    public void setVoteAvg(float voteAvg) {
        this.voteAvg = voteAvg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterImgUrl() {
        return posterImgUrl;
    }

    public void setPosterImgUrl(String posterImgUrl) {
        this.posterImgUrl = posterImgUrl;
    }

    public String getBackdropImgUrl() {
        return backdropImgUrl;
    }

    public void setBackdropImgUrl(String backdropImgUrl) {
        this.backdropImgUrl = backdropImgUrl;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public static final Parcelable.Creator<MovieDetail> CREATOR = new Parcelable.Creator<MovieDetail>() {
        public MovieDetail createFromParcel(Parcel in) {
            return new MovieDetail(in);
        }

        public MovieDetail[] newArray(int size) {
            return new MovieDetail[size];
        }
    };
}
