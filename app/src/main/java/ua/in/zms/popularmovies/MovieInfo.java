package ua.in.zms.popularmovies;

/**
 * Created by poornima-udacity on 6/26/15.
 */
public class MovieInfo {
    String title;
    String id;
    String image; // drawable reference id
    String overview;
    String rating;
    String releaseDate;

    public MovieInfo(String title, String id, String image,
                     String overview, String rating, String releaseDate)
    {
        this.title = title;
        this.id = id;
        this.image = image;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

}