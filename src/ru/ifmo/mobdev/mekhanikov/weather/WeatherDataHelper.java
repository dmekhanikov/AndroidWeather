package ru.ifmo.mobdev.mekhanikov.weather;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class WeatherDataHelper {
	private static String DATABASE_NAME;
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "cities";
	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt;

	public WeatherDataHelper(Context context, String name) {
		DATABASE_NAME = name + ".db";
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement("insert into " + TABLE_NAME
				+ "(cityName, cityID, regionName, ) values(?, ?, ?)");
	}

	public long insert(City city) {
		this.insertStmt.bindString(1, city.cityName);
		this.insertStmt.bindString(2, city.cityId);
		this.insertStmt.bindString(3, city.regionName);
		return this.insertStmt.executeInsert();
	}

	public void deleteAll() {
		this.db.delete(TABLE_NAME, null, null);
	}

	public List<City> selectAllCities() {
		List<City> list = new ArrayList<City>();
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "cityName",
				"cityId", "regionName" }, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.cityName = cursor.getString(0);
				city.cityId = cursor.getString(1);
				city.regionName = cursor.getString(2);
				list.add(city);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	public City selectCityById(String id) {
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { "cityName",
				"cityId", "regionName" }, "cityID=" + id, null, null, null,
				null);
		City result = null;
		if (cursor.moveToFirst()) {
			result = new City();
			result.cityName = cursor.getString(0);
			result.cityId = cursor.getString(1);
			result.regionName = cursor.getString(2);
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return result;
	}

	public void deleteCityById(String id) {
		db.delete(TABLE_NAME, "cityID=" + id, null);
	}

	private static class OpenHelper extends SQLiteOpenHelper {
		public OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE "
					+ TABLE_NAME
					+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, cityName VARCHAR, cityID VARCHAR, regionName VARCHAR)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXIST " + TABLE_NAME);
			onCreate(db);
		}
	}
}
