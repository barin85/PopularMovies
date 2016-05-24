package ua.in.zms.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DiscoveryFragment extends Fragment {

    private MovieInfoAdapter movieInfoAdapter;

    public DiscoveryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);
        movieInfoAdapter = new MovieInfoAdapter(getActivity(), new ArrayList<MovieInfo>());

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);
        gridView.setAdapter(movieInfoAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextView dayForecast = (TextView) view;
//                Toast.makeText(getActivity(),dayForecast.getText(), Toast.LENGTH_SHORT).show();

                MovieInfo movieInfo = movieInfoAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("title", movieInfo.title);
                intent.putExtra("overview", movieInfo.overview);
                intent.putExtra("image", movieInfo.image);
                intent.putExtra("rating", movieInfo.rating);
                intent.putExtra("releaseDate", movieInfo.releaseDate);

                startActivity(intent);

            }
        });

        loadMovies();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onStart();
        loadMovies();
    }

    private void loadMovies() {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String sortOrder = pref.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        new FetchMoviesTask().execute(sortOrder);
    }

    class FetchMoviesTask extends AsyncTask<String, Void, List<MovieInfo>> {

        String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected List<MovieInfo> doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String apiKey = "";
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", strings[0])
                        .appendQueryParameter("api_key", apiKey);

                String forecastUrl = builder.build().toString();
                URL url = new URL(forecastUrl);

                // Create the request to OpenWeatherMap, and open the connection
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
                forecastJsonStr = buffer.toString();

                //Log.d(LOG_TAG,forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
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
            try {
                return getMoviesDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MovieInfo> movies) {
            if (movies != null) {
                movieInfoAdapter.clear();
                for (MovieInfo movie : movies) {
                    movieInfoAdapter.add(movie);
                }
                //mForecastAdapter.notifyDataSetChanged();
            }
        }

        private String getReadableDateString(String date) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date newDate = null;
            try {
                newDate = format.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("dd MMM yyyy");
            return shortenedDateFormat.format(newDate);
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private List<MovieInfo> getMoviesDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_TITLE = "original_title";
            final String OWM_ID = "id";
            final String OWM_OVERVIEW = "overview";
            final String OWM_RATING = "vote_average";
            final String OWM_RELEASE_DATE = "release_date";

            List<MovieInfo> resultMovies = new ArrayList<MovieInfo>();

            JSONObject forecastJson = new JSONObject(movieJsonStr);
            JSONArray resultArray = forecastJson.getJSONArray(OWM_RESULTS);

            for (int i = 0; i < resultArray.length(); i++) {
                String movieId;
                String posterPath;
                String originalTitle;
                String overview;
                String releaseDate;
                String rating;

                // Get the JSON object representing the day
                JSONObject movieObject = resultArray.getJSONObject(i);

                movieId = movieObject.getString(OWM_ID);
                posterPath = movieObject.getString(OWM_POSTER_PATH);
                originalTitle = movieObject.getString(OWM_TITLE);
                overview = movieObject.getString(OWM_OVERVIEW);
                rating = movieObject.getString(OWM_RATING);
                releaseDate = getReadableDateString(movieObject.getString(OWM_RELEASE_DATE));

                MovieInfo movieInfo = new MovieInfo(originalTitle, movieId, posterPath,
                        overview, rating, releaseDate);
                resultMovies.add(movieInfo);
            }
            return resultMovies;
        }
    }
}
