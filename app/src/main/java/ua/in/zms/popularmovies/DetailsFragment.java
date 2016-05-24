package ua.in.zms.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String posterPath = getActivity().getIntent().getStringExtra("image");
        String originalTitle = getActivity().getIntent().getStringExtra("title");
        String overview = getActivity().getIntent().getStringExtra("overview");
        String releaseDate = getActivity().getIntent().getStringExtra("releaseDate");
        String rating = getActivity().getIntent().getStringExtra("rating");

        View v = inflater.inflate(R.layout.fragment_details, container, false);

        TextView titleText = (TextView) v.findViewById(R.id.textTitle);
        titleText.setText(originalTitle);

        TextView overviewText = (TextView) v.findViewById(R.id.textOverview);
        overviewText.setText(overview);

        TextView ratingText = (TextView) v.findViewById(R.id.textRating);
        ratingText.setText("Rating: " + rating);
//
        TextView releaseDateText = (TextView) v.findViewById(R.id.textReleaseDate);
        releaseDateText.setText("Release date: " + releaseDate);

        String base_url = "http://image.tmdb.org/t/p/w342/";
        ImageView poster = (ImageView) v.findViewById(R.id.imageViewPoster);
        Picasso.with(getActivity()).load(base_url + posterPath).into(poster);


        return v;
    }
}
