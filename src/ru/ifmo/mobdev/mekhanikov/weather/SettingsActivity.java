package ru.ifmo.mobdev.mekhanikov.weather;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	CitiesListAdapter listAdapter = null;
	List<City> cities = null;
	WeatherDataHelper dataHelper = null;
	private static String PREF_NAME = "WeatherPrefs";
	private static String PREF_UPDATE_PERIOD = "UpdatePeriod";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_settings);

		dataHelper = new WeatherDataHelper(this);
		cities = dataHelper.selectAllCities();

		listAdapter = new CitiesListAdapter();
		ListView listView = (ListView) findViewById(R.id.citiesList);
		listView.setAdapter(listAdapter);

		EditText editPeriod = (EditText) findViewById(R.id.editPeriod);
		SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
		int period = settings.getInt(PREF_UPDATE_PERIOD, 10);
		editPeriod.setText(String.valueOf(period));
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void addButtonClicked(View view) {
		Intent intent = new Intent(SettingsActivity.this, CityAddActivity.class);
		startActivity(intent);
	}

	private class CitiesListAdapter extends ArrayAdapter<City> {

		CitiesListAdapter() {
			super(SettingsActivity.this, R.layout.city_list_element);
		}

		@Override
		public int getCount() {
			if (cities != null) {
				return cities.size();
			} else {
				return 0;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = null;
			LayoutInflater inflater = getLayoutInflater();
			row = inflater.inflate(R.layout.city_list_element, parent, false);
			if (cities.size() < position
					|| cities.get(position).cityName == null
					|| cities.get(position).cityId == null) {
				return row;
			}
			TextView cityName = (TextView) row.findViewById(R.id.cityName);

			cityName.setText(cities.get(position).cityName);
			ImageButton deleteButton = (ImageButton) row
					.findViewById(R.id.deleteButton);
			deleteButton.setContentDescription(String.valueOf(position));
			deleteButton.setOnClickListener(new ImageButton.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = Integer.parseInt(v.getContentDescription()
							.toString());
					dataHelper.deleteCityById(cities.get(position).cityId);
					cities.remove(position);
					listAdapter.notifyDataSetChanged();
				}
			});
			return row;
		}
	}

	public void saveUpdatePeriod(View view) {
		SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor settingsEditor = settings.edit();
		EditText editPeriod = (EditText) findViewById(R.id.editPeriod);
		settingsEditor.putInt(PREF_UPDATE_PERIOD,
				Integer.parseInt(editPeriod.getText().toString()));
		settingsEditor.commit();
	}

}
