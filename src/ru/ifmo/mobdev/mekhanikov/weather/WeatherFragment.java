package ru.ifmo.mobdev.mekhanikov.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherFragment extends Fragment {
	private volatile View view;
	private int cityId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.weather_fragment_layout, container,
				false);
		switch (getArguments().getInt("position")) {
		case 0:
			cityId = 27;
			break;
		case 1:
			cityId = 773;
			break;
		case 2:
			cityId = 8750;
			break;
		}
		ForecastUpdater updater = new ForecastUpdater();
		updater.execute(cityId);
		return view;
	}

	private void updateView(Forecast forecast) {
		String packageName = this.getClass().getPackage().getName();
		if (forecast.current.temperature != null) {
			WeatherState w = forecast.current;
			ImageView image = (ImageView) view.findViewById(R.id.imageWeather);
			image.setImageResource(this.getResources().getIdentifier(w.picName,
					"drawable", packageName));
			TextView textTemp = (TextView) view
					.findViewById(R.id.textTemperature);
			textTemp.setText(w.temperature + "°C");
			TextView textWind = (TextView) view.findViewById(R.id.textWind);
			textWind.setText("Wind: " + w.wind + " m/s");
			TextView textHumidity = (TextView) view
					.findViewById(R.id.textHumidity);
			textHumidity.setText("Humidity: " + w.humidity + "%");
			TextView textPressure = (TextView) view
					.findViewById(R.id.textPressure);
			textPressure.setText("Pressure: " + w.pressure + " mmHg");
		}
		for (int day = 0; day != 4; ++day) {
			for (int daypart = 0; daypart != 4; ++daypart) {
				WeatherState w = forecast.forecast[day][daypart];
				if (w.temperature != null) {
					int imageId = this.getResources().getIdentifier(
							"imageWeather" + day + "_" + daypart, "id",
							packageName);
					ImageView image = (ImageView) view.findViewById(imageId);
					image.setImageResource(this.getResources().getIdentifier(
							"s" + w.picName, "drawable", packageName));
					int textTemperatureId = this.getResources().getIdentifier(
							"textTemperature" + day + "_" + daypart, "id",
							packageName);
					TextView textTemperature = (TextView) view
							.findViewById(textTemperatureId);
					Log.i("progress", day + " " + daypart);
					textTemperature.setText(w.temperature + "°C");
				}
			}
		}
	}

	public class ForecastUpdater extends AsyncTask<Integer, Void, Forecast> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Forecast doInBackground(Integer... cityIds) {
			URL url;
			String query = "http://xml.weather.co.ua/1.2/forecast/"
					+ cityIds[0] + "?dayf=4";
			try {
				url = new URL(query);
			} catch (MalformedURLException e) {
				return null;
			}
			StringBuilder source = null;
			try {
				BufferedReader sourceReader = new BufferedReader(
						new InputStreamReader(url.openStream()));
				source = new StringBuilder();
				String line = sourceReader.readLine();
				while (line != null) {
					source.append(line);
					line = sourceReader.readLine();
				}
			} catch (IOException e) {
				return null;
			}
			Forecast result = new ForecastParser().parse(source.toString());
			result.updateTime = System.currentTimeMillis();
			return result;
		}

		@Override
		protected void onPostExecute(Forecast result) {
			super.onPostExecute(result);
			updateView(result);
		}
	}
}