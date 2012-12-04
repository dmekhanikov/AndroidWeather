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
	private static final int DATABASE_VERSION = 4;
	private static final String TABLE_NAME = "cities";
	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt, updateStmt;

	public WeatherDataHelper(Context context, String name) {
		DATABASE_NAME = name + ".db";
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement("INSERT INTO " + TABLE_NAME
				+ "(cityName, cityID, regionName) VALUES(?, ?, ?)");
		this.updateStmt = this.db.compileStatement("UPDATE " + TABLE_NAME + " SET "
				+ "curTemp=?, curPicName=?, curPres=?, curWind=?, curHum=?, "
				+ "temp0_0=?, picName0_0=?, temp0_1=?, picName0_1=?, temp0_2=?, picName0_2=?, temp0_3=?, picName0_3=?, "
				+ "temp1_0=?, picName1_0=?, temp1_1=?, picName1_1=?, temp1_2=?, picName1_2=?, temp1_3=?, picName1_3=?, "
				+ "temp2_0=?, picName2_0=?, temp2_1=?, picName2_1=?, temp2_2=?, picName2_2=?, temp2_3=?, picName2_3=?, "
				+ "temp3_0=?, picName3_0=?, temp3_1=?, picName3_1=?, temp3_2=?, picName3_2=?, temp3_3=?, picName3_3=? "
				+ "WHERE cityId=?");
	}

	public long insertCity(City city) {
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
	
	public Forecast selectForecastByCityId(String cityId) {
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { 
				"curTemp", "curPicName", "curPres", "curWind", "curHum",
				"temp0_0", "picName0_0", "temp0_1", "picName0_1", "temp0_2", "picName0_2", "temp0_3", "picName0_3",
				"temp1_0", "picName1_0", "temp1_1", "picName1_1", "temp1_2", "picName1_2", "temp1_3", "picName1_3",
				"temp2_0", "picName2_0", "temp2_1", "picName2_1", "temp2_2", "picName2_2", "temp2_3", "picName2_3",
				"temp3_0", "picName3_0", "temp3_1", "picName3_1", "temp3_2", "picName3_2", "temp3_3", "picName3_3",
				}, "cityID=" + cityId, null, null, null,
				null);
		Forecast result = null;
		if (cursor.moveToFirst()) {
			result = new Forecast();
			result.curTemp = cursor.getString(0);
			result.curPicName = cursor.getString(1);
			result.curPres = cursor.getString(2);
			result.curWind = cursor.getString(3);
			result.curHum = cursor.getString(4);
			for (int i = 0; i != 4; ++i) {
				for (int j = 0; j != 4; ++j) {
					result.forecast[i][j].temperature = cursor.getString(5 + i * 8 + j * 2);
					result.forecast[i][j].picName = cursor.getString(5 + i * 8 + j * 2 + 1);
				}
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return result;
	}
	
	public void updateForecast(String cityId, Forecast forecast) {
		if (forecast.curTemp != null) {
			updateStmt.bindString(1, forecast.curTemp);
		}
		if (forecast.curPicName != null) {
			updateStmt.bindString(2, forecast.curPicName);
		}
		if (forecast.curPres != null) {
			updateStmt.bindString(3, forecast.curPres);
		}
		if (forecast.curWind != null) {
			updateStmt.bindString(4, forecast.curWind);
		}
		if (forecast.curHum != null) {
			updateStmt.bindString(5, forecast.curHum);
		}
		for (int i = 0; i != 4; ++i) {
			for (int j = 0; j != 4; ++j) {
				if (forecast.forecast[i][j].temperature != null) {
					updateStmt.bindString(5 + i * 8 + j * 2 + 1, forecast.forecast[i][j].temperature);
				} else {
					updateStmt.bindString(5 + i * 8 + j * 2 + 1, "");
				}
				if (forecast.forecast[i][j].picName != null) {
					updateStmt.bindString(5 + i * 8 + j * 2 + 2, forecast.forecast[i][j].picName);
				} else {
					updateStmt.bindString(5 + i * 8 + j * 2 + 2, "");
				}
			}
		}
		updateStmt.bindString(38, cityId);
		updateStmt.execute();
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
					+ "(id INTEGER PRIMARY KEY AUTOINCREMENT, cityName VARCHAR, cityID VARCHAR, regionName VARCHAR, "
					+ "curTemp VARCHAR, curPicName VARCHAR, curPres VARCHAR, curWind VARCHAR, curHum VARCHAR, "
					+ "temp0_0 VARCHAR, picName0_0 VARCHAR, temp0_1 VARCHAR, picName0_1 VARCHAR, temp0_2 VARCHAR, picName0_2 VARCHAR, temp0_3 VARCHAR, picName0_3 VARCHAR,"
					+ "temp1_0 VARCHAR, picName1_0 VARCHAR, temp1_1 VARCHAR, picName1_1 VARCHAR, temp1_2 VARCHAR, picName1_2 VARCHAR, temp1_3 VARCHAR, picName1_3 VARCHAR,"
					+ "temp2_0 VARCHAR, picName2_0 VARCHAR, temp2_1 VARCHAR, picName2_1 VARCHAR, temp2_2 VARCHAR, picName2_2 VARCHAR, temp2_3 VARCHAR, picName2_3 VARCHAR,"
					+ "temp3_0 VARCHAR, picName3_0 VARCHAR, temp3_1 VARCHAR, picName3_1 VARCHAR, temp3_2 VARCHAR, picName3_2 VARCHAR, temp3_3 VARCHAR, picName3_3 VARCHAR)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}
