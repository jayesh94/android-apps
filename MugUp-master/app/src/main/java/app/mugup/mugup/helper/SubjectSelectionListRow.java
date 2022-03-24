package app.mugup.mugup.helper;

import java.util.ArrayList;

public class SubjectSelectionListRow
{
    public String title, thumbnailUrl, genre, rating, id;
public boolean year;
    public SubjectSelectionListRow() {
    }

    public SubjectSelectionListRow(String name, String thumbnailUrl, boolean year, String rating, String genre, String id) {
        this.title = name;
        this.thumbnailUrl = thumbnailUrl;
        this.year = year;
        this.rating = rating;
        this.genre = genre;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String name) {
        this.title = name;
    }

    public String getBookId() {
        return id;
    }
    public void setBookId(String id) {
        this.id = id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean getYear() {
        return year;
    }

    public void setYear(boolean year) {
        this.year = year;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

}