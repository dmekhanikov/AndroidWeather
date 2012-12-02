package ru.ifmo.mobdev.mekhanikov.weather;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class CityListActivity extends Activity {
	CitiesListAdapter listAdapter = null;
	List<City> cities = null;
	WeatherDataHelper dataHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_city_list);

		dataHelper = new WeatherDataHelper(this, this.getClass().getPackage()
				.getName());
		cities = dataHelper.selectAllCities();

		listAdapter = new CitiesListAdapter();
		ListView listView = (ListView) findViewById(R.id.citiesList);
		listView.setAdapter(listAdapter);
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(CityListActivity.this,
				MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void addButtonClicked(View view) {
		Intent intent = new Intent(CityListActivity.this, CityAddActivity.class);
		startActivity(intent);
	}

	private class CitiesListAdapter extends ArrayAdapter<City> {

		CitiesListAdapter() {
			super(CityListActivity.this, R.layout.city_list_element);
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

}
