package sugar.sunshine.app.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import sugar.sunshine.app.R;
import sugar.sunshine.app.data.WeatherContract;
import sugar.sunshine.app.Utility;
import sugar.sunshine.app.ForecastFragment;

/**
 * Created by sugar on 27-02-2016.
 */
public class ForecastAdapter extends CursorAdapter {

    private Context mContext;
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.mContext = context;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow ;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /**
     * This is where we'll bind the view with the contents of the cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}
