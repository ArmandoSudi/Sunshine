package sugar.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import sugar.sunshine.app.data.WeatherContract.LocationEntry;
import sugar.sunshine.app.data.WeatherContract.WeatherEntry;

import java.util.HashSet;

/**
 * Created by sugar on 15-02-2016.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherEntry.TABLE_NAME);
        tableNameHashSet.add(LocationEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
         assertTrue("Error: this means that the databse has not been created correctly",
                 c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext());

        // if this fails, it means that databse doesnt contain both the location entry
        // ans weather entry
        assertTrue("Error: The database was created witouht both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns ?
        c = db.rawQuery("PRAGMA table_info(" + LocationEntry.TABLE_NAME + ")", null);

        assertTrue("Error: this means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesnt contain all the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testLocationTable() {
        insertLocation();
    }

    public void testWeatherTable() {

        long locationRowId = insertLocation(); // serves as foreign key while creating weather values
        // Make sure we have a valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", locationRowId == -1L);

        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

        // verify we got a row back from weather
        Log.d(LOG_TAG, "value of weatherRowID : " + weatherRowId);
        assertTrue(weatherRowId != -1);

        Cursor weatherCursor = db.query(
                WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // move the cursor to a valid database row and check if we got ant record back from query
        assertTrue("Error: No record returned from weather query", weatherCursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Weather Query Validation Failed",
                weatherCursor, weatherValues);

        // move the cursor to demonstrate that there is only one record in the database
        assertFalse("error: more than one record returned from location query", weatherCursor.moveToNext());

        // close Cursor and Database
        weatherCursor.close();
        db.close();
    }

    public long insertLocation() {
        // 1st step: getting the reference to the writable database
        // any error in the SQL table creation string will be thrown here
        WeatherDbHelper dbHeper = new WeatherDbHelper(mContext);
        SQLiteDatabase sqLiteDatabase = dbHeper.getWritableDatabase();

        // creating the test values and insert it
        // 2nd and 3rd step: create content values, insert in the db and get a row ID back
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
        long locationRowId = sqLiteDatabase.insert(LocationEntry.TABLE_NAME, null, testValues);

        // verify we got a row back
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.
        // 4th step: Query the database and receive a Cursor back
        Cursor cursor = sqLiteDatabase.query(
                WeatherContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // move the cursor to a valid database row and check to see if we got any records back from query
        assertTrue("Error: No records returned from location query", cursor.moveToFirst());

        // 5th Step: Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("error: more than one record returned from location query", cursor.moveToNext());

        // 6th step: close Cursor and Database
        cursor.close();
        sqLiteDatabase.close();

        return locationRowId;
    }
}

