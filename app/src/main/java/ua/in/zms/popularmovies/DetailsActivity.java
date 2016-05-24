package ua.in.zms.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class DetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
