package sugar.sunshine.app;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import sugar.sunshine.app.data.WeatherContract;

/**
 * Created by sugar on 29-02-2016.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #Sunshine";

    private ShareActionProvider shareActionProvider;
    private String forecast;

    private static int DETAIL_LOADER = 0;

    public static String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };

    // These constants correspond to the projection defined above and must change if the projection
    // changes
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//        TextView forecastTV = (TextView) rootView.findViewById(R.id.forecast_text);
//        Intent intent = getActivity().getIntent();
//        if( intent != null ) {
//            forecast = intent.getDataString();
//        }
//        if (forecast != null) {
//            forecastTV.setText(forecast);
//        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // inflate the menu, this adds items to the actionBar if present
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem item = menu.findItem(R.id.action_share_item);

        // Get the provider and hold onto it to set/change the share intent
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // if onLoadFinished happens before this, we can go ahead and set share intent
        if (forecast != null){
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "in onCreateLoader");
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        // create and return a cursorLoader that will take care of creating a cursor for the
        // data being displayed
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!cursor.moveToFirst()) { return ;}

        String dateString = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE));
        String weatherDescription = cursor.getString(COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        forecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        TextView forecastTV = (TextView)getView().findViewById(R.id.forecast_text);
        forecastTV.setText(forecast);

        // If onCreateOptionsMenu has already happened, we need to update the share intent
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // we dont need to anythinf here as there is no data we are holding to that need to be cleaned up
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // to return to our application after sharing instead of staying in the app
        // we are using for the intent flag FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }
}

