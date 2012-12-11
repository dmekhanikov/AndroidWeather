package ru.ifmo.mobdev.mekhanikov.weather;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class WeatherProvider extends ContentProvider {
	
	public static final String AUTHORITY = "ru.ifmo.mobdev.mekhanikov.weather";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	private SQLiteDatabase mWeatherDB;
	
	private static final String DB_NAME = "ru.ifmo.mobdev.mekhanikov.weather.db";
	private static final int DB_VERSION = 2;
	private static final String WEATHER_TABLE = "weather";
	
	public static final String KEY_ID = "_id";
	public static final String KEY_CITY_NAME = "cityName";
	public static final String KEY_CITY_ID = "cityID";
	public static final String KEY_REGION_NAME = "regionName";
	public static final String KEY_CURRENT_TEMP = "curTemp";
	public static final String KEY_CURRENT_PIC_NAME = "curPicName";
	public static final String KEY_CURRENT_PRESSURE = "curPres";
	public static final String KEY_CURRENT_WIND = "curWind";
	public static final String KEY_CURRENT_HUM = "curHum";
	public static final String KEY_TEMP = "temp";
	public static final String KEY_PIC_NAME = "picName";
	
	public static final int CITY_NAME_COLUMN = 1;
	public static final int CITY_ID_COLUMN = 2;
	public static final int REGION_NAME_COLUMN = 3;
	public static final int CURRENT_TEMP_COLUMN = 4;
	public static final int CURRENT_PIC_NAME_COLUMN = 5;
	public static final int CURRENT_PRESSURE_COLUMN = 6;
	public static final int CURRENT_WIND_COLUMN = 7;
	public static final int CURRENT_HUM_COLUMN = 8;
	
	private static final int FORECAST = 1;
	private static final int ALL_CITIES = 2;
	private static final int ONE_CITY = 3;
	
	public static final String FORECAST_TYPE = "vnd.android.cursor.item/vnd.weather.forecast";
	public static final String ALL_CITIES_TYPE = "vnd.android.cursor.dir/vnd.weather.cities";
	public static final String ONE_CITY_TYPE = "vnd.android.cursor.item/vnd.weather.city";
	
	private static final UriMatcher mUriMatcher;
	
	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, "forecast/*", FORECAST);
		mUriMatcher.addURI(AUTHORITY, "cities", ALL_CITIES);
		mUriMatcher.addURI(AUTHORITY, "cities/*", ONE_CITY);
	}
	
	private static class WeatherDBHelper extends SQLiteOpenHelper {
		
		private static final String DB_CREATE_TABLE =
				"CREATE TABLE " + WEATHER_TABLE
				+ "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_CITY_NAME + " VARCHAR, " + KEY_CITY_ID + " VARCHAR, " +  KEY_REGION_NAME + " VARCHAR, "
				+ KEY_CURRENT_TEMP + " VARCHAR, " + KEY_CURRENT_PIC_NAME + " VARCHAR, " + KEY_CURRENT_PRESSURE + " VARCHAR, " + KEY_CURRENT_WIND + " VARCHAR, " + KEY_CURRENT_HUM + " VARCHAR, "
				+ KEY_TEMP + "0_0 VARCHAR, " + KEY_PIC_NAME + "0_0 VARCHAR, " + KEY_TEMP + "0_1 VARCHAR, " + KEY_PIC_NAME + "0_1 VARCHAR, " + KEY_TEMP + "0_2 VARCHAR, " + KEY_PIC_NAME + "0_2 VARCHAR, " + KEY_TEMP + "0_3 VARCHAR, " + KEY_PIC_NAME + "0_3 VARCHAR, "
				+ KEY_TEMP + "1_0 VARCHAR, " + KEY_PIC_NAME + "1_0 VARCHAR, " + KEY_TEMP + "1_1 VARCHAR, " + KEY_PIC_NAME + "1_1 VARCHAR, " + KEY_TEMP + "1_2 VARCHAR, " + KEY_PIC_NAME + "1_2 VARCHAR, " + KEY_TEMP + "1_3 VARCHAR, " + KEY_PIC_NAME + "1_3 VARCHAR, "
				+ KEY_TEMP + "2_0 VARCHAR, " + KEY_PIC_NAME + "2_0 VARCHAR, " + KEY_TEMP + "2_1 VARCHAR, " + KEY_PIC_NAME + "2_1 VARCHAR, " + KEY_TEMP + "2_2 VARCHAR, " + KEY_PIC_NAME + "2_2 VARCHAR, " + KEY_TEMP + "2_3 VARCHAR, " + KEY_PIC_NAME + "2_3 VARCHAR, "
				+ KEY_TEMP + "3_0 VARCHAR, " + KEY_PIC_NAME + "3_0 VARCHAR, " + KEY_TEMP + "3_1 VARCHAR, " + KEY_PIC_NAME + "3_1 VARCHAR, " + KEY_TEMP + "3_2 VARCHAR, " + KEY_PIC_NAME + "3_2 VARCHAR, " + KEY_TEMP + "3_3 VARCHAR, " + KEY_PIC_NAME + "3_3 VARCHAR)";
		
		public WeatherDBHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + WEATHER_TABLE);
			onCreate(db);
		}	
	}
	
	@Override
	public boolean onCreate() {
		WeatherDBHelper dbHelper = 
				new WeatherDBHelper(getContext(), DB_NAME, null, DB_VERSION);
		mWeatherDB = dbHelper.getWritableDatabase();
		return mWeatherDB != null;
	}
	
	@Override
	public String getType(Uri uri) {
		switch(mUriMatcher.match(uri)) {
		case FORECAST:
			return FORECAST_TYPE;
		case ALL_CITIES:
			return ALL_CITIES_TYPE;
		case ONE_CITY:
			return ONE_CITY_TYPE;
		default:
			throw new IllegalStateException("Unsupported URI: " + uri);
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(WEATHER_TABLE);
		String[] proj = projection;
		switch(mUriMatcher.match(uri)) {
		case FORECAST:
			if (proj == null) {
				proj = new String[] {
						KEY_CURRENT_TEMP, KEY_CURRENT_PIC_NAME, KEY_CURRENT_PRESSURE, KEY_CURRENT_WIND, KEY_CURRENT_HUM,
						KEY_TEMP + "0_0", KEY_PIC_NAME + "0_0", KEY_TEMP + "0_1", KEY_PIC_NAME + "0_1", KEY_TEMP + "0_2", KEY_PIC_NAME + "0_2", KEY_TEMP + "0_3", KEY_PIC_NAME + "0_3",
						KEY_TEMP + "1_0", KEY_PIC_NAME + "1_0", KEY_TEMP + "1_1", KEY_PIC_NAME + "1_1", KEY_TEMP + "1_2", KEY_PIC_NAME + "1_2", KEY_TEMP + "1_3", KEY_PIC_NAME + "1_3",
						KEY_TEMP + "2_0", KEY_PIC_NAME + "2_0", KEY_TEMP + "2_1", KEY_PIC_NAME + "2_1", KEY_TEMP + "2_2", KEY_PIC_NAME + "2_2", KEY_TEMP + "2_3", KEY_PIC_NAME + "2_3",
						KEY_TEMP + "3_0", KEY_PIC_NAME + "3_0", KEY_TEMP + "3_1", KEY_PIC_NAME + "3_1", KEY_TEMP + "3_2", KEY_PIC_NAME + "3_2", KEY_TEMP + "3_3", KEY_PIC_NAME + "3_3"
				};
			}
			qb.appendWhere(KEY_CITY_ID + "=" + uri.getPathSegments().get(1));
			break;
		case ONE_CITY:
			if (proj == null) {
				proj = new String[] {
						KEY_CITY_NAME, KEY_CITY_ID, KEY_REGION_NAME
				};
			}
			qb.appendWhere(KEY_CITY_ID + "=" + uri.getPathSegments().get(1));
			break;
		}
		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = KEY_ID + " DESC";
		} else {
			orderBy = sort;
		}
		
		Cursor result = qb.query(mWeatherDB, proj, selection, selectionArgs, null, null, orderBy);
		result.setNotificationUri(getContext().getContentResolver(), uri);
		return result;
	}
	
	@Override
	public Uri insert(Uri inuri, ContentValues initValues) {
		long rowId = mWeatherDB.insert(WEATHER_TABLE, "", initValues);
		if (rowId > 0) {
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		throw new SQLException("Failed to insert row into " + inuri);
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count;
		switch(mUriMatcher.match(uri)) {
		case ALL_CITIES:
			count = mWeatherDB.delete(WEATHER_TABLE, where, whereArgs);
			break;
		case FORECAST:
		case ONE_CITY:
			String wh = KEY_CITY_ID + "=" + uri.getPathSegments().get(1);
			if (!TextUtils.isEmpty(where)) {
				wh += " AND (" + where + ")";
			}
			count = mWeatherDB.delete(WEATHER_TABLE, wh, whereArgs);
			break;
		default:
			throw new IllegalStateException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int count;
		switch(mUriMatcher.match(uri)) {
		case ALL_CITIES:
			count = mWeatherDB.update(WEATHER_TABLE, values, where, whereArgs);
			break;
		case FORECAST:
		case ONE_CITY:
			String wh = KEY_CITY_ID + "=" + uri.getPathSegments().get(1);
			if (!TextUtils.isEmpty(where)) {
				wh += " AND (" + where + ")";
			}
			count = mWeatherDB.update(WEATHER_TABLE, values, wh, whereArgs);
			break;
		default:
			throw new IllegalStateException("Unsupported URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
