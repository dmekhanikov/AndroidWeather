package ru.ifmo.mobdev.mekhanikov.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CityAddActivity extends Activity {
	private List<City> cities = null;
	private CitySearchListAdapter listAdapter = null;
	private CitySearcher searcher = null;
	private WeatherDataHelper dataHelper = null;
	private static String PREF_NAME = "WeatherPrefs";
	private static String PREF_CUR_POS = "CurrentPosition";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_city_add);

		dataHelper = new WeatherDataHelper(this);

		listAdapter = new CitySearchListAdapter();
		ListView citiesListView = (ListView) findViewById(R.id.citiesListView);
		citiesListView.setAdapter(listAdapter);
		citiesListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						if (cities.get(position).cityId != null
								&& dataHelper.selectCityById(cities
										.get(position).cityId) == null) {
							dataHelper.insertCity(cities.get(position));
							
							SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
							SharedPreferences.Editor settingsEditor = settings.edit();
							settingsEditor.putInt(PREF_CUR_POS, 0);
							settingsEditor.commit();
							
							Intent intent = new Intent(CityAddActivity.this,
									MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					}
				});

		EditText cityNameEdit = (EditText) findViewById(R.id.cityNameEdit);
		cityNameEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (searcher != null) {
					searcher.cancel(true);
					searcher = null;
				}
				String cityName = s.toString();
				cities = null;
				listAdapter.notifyDataSetChanged();
				searcher = new CitySearcher();
				searcher.execute(cityName);
			}
		});
	}

	private class CitySearchListAdapter extends ArrayAdapter<City> {

		CitySearchListAdapter() {
			super(CityAddActivity.this, R.layout.city_search_list_element);
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
			row = inflater.inflate(R.layout.city_search_list_element, parent,
					false);
			TextView cityName = (TextView) row.findViewById(R.id.cityName);
			TextView countryName = (TextView) row
					.findViewById(R.id.countryName);
			City curCity = cities.get(position);
			if (curCity != null) {
				if (curCity.cityName != null) {
					cityName.setText(cities.get(position).cityName);
				}
				if (curCity.regionName != null) {
					countryName.setText(curCity.regionName);
				}
			}
			return row;
		}
	}

	public class CitySearcher extends AsyncTask<String, Void, List<City>> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
			pb.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<City> doInBackground(String... cityNames) {
			URL url;
			URLConnection urlCon;
			String query = null;
			try {
				query = "http://xml.weather.co.ua/1.2/city/?search="
						+ URLEncoder.encode(cityNames[0], "UTF-8") + "&lang=en";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			try {
				url = new URL(query);
				urlCon = url.openConnection();
				urlCon.setConnectTimeout(5000);
				urlCon.connect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			StringBuilder source = null;
			try {
				BufferedReader sourceReader = new BufferedReader(
						new InputStreamReader(urlCon.getInputStream()));
				source = new StringBuilder();
				String line = sourceReader.readLine();
				while (line != null) {
					source.append(line);
					line = sourceReader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			List<City> result = new CitySearchParser().parse(source.toString());
			return result;
		}

		@Override
		protected void onPostExecute(List<City> result) {
			super.onPostExecute(result);
			ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
			pb.setVisibility(View.INVISIBLE);
			try {
				if (result != null) {
					cities = result;
					listAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
