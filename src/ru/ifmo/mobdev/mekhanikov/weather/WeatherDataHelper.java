package ru.ifmo.mobdev.mekhanikov.weather;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class WeatherDataHelper {
	private Context mContext;

	public WeatherDataHelper(Context context) {
		mContext = context;
	}

	public void insertCity(City city) {
		ContentResolver cr = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(WeatherProvider.KEY_CITY_NAME, city.cityName);
		values.put(WeatherProvider.KEY_CITY_ID, city.cityId);
		values.put(WeatherProvider.KEY_REGION_NAME, city.regionName);
		cr.insert(Uri.parse("content://" + WeatherProvider.AUTHORITY + "/cities/"), values);
	}

	public List<City> selectAllCities() {
		List<City> list = new ArrayList<City>();
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr
				.query(Uri.parse("content://" + WeatherProvider.AUTHORITY
						+ "/cities"), new String[] {
						WeatherProvider.KEY_CITY_NAME,
						WeatherProvider.KEY_CITY_ID,
						WeatherProvider.KEY_REGION_NAME }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.cityName = cursor
						.getString(0);
				city.cityId = cursor.getString(1);
				city.regionName = cursor
						.getString(2);
				list.add(city);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	public City selectCityById(String id) {
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(
				Uri.parse("content://" + WeatherProvider.AUTHORITY + "/cities/"
						+ id), new String[] { WeatherProvider.KEY_CITY_NAME,
						WeatherProvider.KEY_CITY_ID,
						WeatherProvider.KEY_REGION_NAME }, null, null, null);
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
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(
				Uri.parse("content://" + WeatherProvider.AUTHORITY
						+ "/forecast/" + cityId), null, null, null, null);
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
					result.forecast[i][j].temperature = cursor.getString(5 + i
							* 8 + j * 2);
					result.forecast[i][j].picName = cursor.getString(5 + i * 8
							+ j * 2 + 1);
				}
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return result;
	}

	public void updateForecast(String cityId, Forecast forecast) {
		ContentValues values = new ContentValues();
		if (forecast.curTemp != null) {
			values.put(WeatherProvider.KEY_CURRENT_TEMP, forecast.curTemp);
		}
		if (forecast.curPicName != null) {
			values.put(WeatherProvider.KEY_CURRENT_PIC_NAME,
					forecast.curPicName);
		}
		if (forecast.curPres != null) {
			values.put(WeatherProvider.KEY_CURRENT_PRESSURE, forecast.curPres);
		}
		if (forecast.curWind != null) {
			values.put(WeatherProvider.KEY_CURRENT_WIND, forecast.curWind);
		}
		if (forecast.curHum != null) {
			values.put(WeatherProvider.KEY_CURRENT_HUM, forecast.curHum);
		}
		for (int i = 0; i != 4; ++i) {
			for (int j = 0; j != 4; ++j) {
				if (forecast.forecast[i][j].temperature != null) {
					values.put(WeatherProvider.KEY_TEMP + i + "_" + j,
							forecast.forecast[i][j].temperature);
				} else {
					values.put(WeatherProvider.KEY_TEMP + i + "_" + j, "");
				}
				if (forecast.forecast[i][j].picName != null) {
					values.put(WeatherProvider.KEY_PIC_NAME + i + "_" + j,
							forecast.forecast[i][j].picName);
				} else {
					values.put(WeatherProvider.KEY_PIC_NAME + i + "_" + j, "");
				}
			}
		}
		values.put(WeatherProvider.KEY_CITY_ID, cityId);
		ContentResolver cr = mContext.getContentResolver();
		cr.update(Uri.parse("content://" + WeatherProvider.AUTHORITY + "/forecast/" + cityId), values, null, null);
	}

	public void deleteCityById(String id) {
		ContentResolver cr = mContext.getContentResolver();
		cr.delete(Uri.parse("content://" + WeatherProvider.AUTHORITY + "/forecast/" + id), null, null);
	}
}
