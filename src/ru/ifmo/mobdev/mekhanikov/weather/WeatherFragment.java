package ru.ifmo.mobdev.mekhanikov.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherFragment extends Fragment {
	private volatile View view;
	private String cityId;
	private static final String[] states = { "Clear", "Cloudy", "Cloudy",
			"Clouds", "Short Rain", "Rain", "Thunderstorm", "Hail", "Sleet",
			"Snow", "Heavy Snow" };
	private ForecastUpdater forecastUpdater = null;
	private WeatherDataHelper dataHelper = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.weather_fragment_layout, container,
				false);
		cityId = getArguments().getString("cityId");
		forecastUpdater = new ForecastUpdater(getActivity());
		dataHelper = new WeatherDataHelper(getActivity());
		try {
			updateView(dataHelper.selectForecastByCityId(cityId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		FragmentUpdater fragmentUpdater = new FragmentUpdater();
		fragmentUpdater.execute();
		return view;
	}
	
	public void update() {
		FragmentUpdater fragmentUpdater = new FragmentUpdater();
		fragmentUpdater.execute();
	}

	private void updateView(Forecast forecast) throws Exception {
		String packageName = this.getClass().getPackage().getName();
		if (!forecast.curTemp.equals("")) {
			ImageView image = (ImageView) view.findViewById(R.id.imageWeather);
			image.setImageResource(this.getResources().getIdentifier(forecast.curPicName,
					"drawable", packageName));
			TextView textTemp = (TextView) view
					.findViewById(R.id.textTemperature);
			textTemp.setText(forecast.curTemp + "°C");
			TextView textWeather = (TextView) view
					.findViewById(R.id.textWeather);
			int state = forecast.curPicName.charAt(1) - 48;
			textWeather.setText(states[state]);
			TextView textWind = (TextView) view.findViewById(R.id.textWind);
			textWind.setText("Wind: " + forecast.curWind + " m/s");
			TextView textHumidity = (TextView) view
					.findViewById(R.id.textHumidity);
			textHumidity.setText("Humidity: " + forecast.curHum + "%");
			TextView textPressure = (TextView) view
					.findViewById(R.id.textPressure);
			textPressure.setText("Pressure: " + forecast.curPres + " mmHg");
		}
		for (int day = 0; day != 4; ++day) {
			for (int daypart = 0; daypart != 4; ++daypart) {
				WeatherState w = forecast.forecast[day][daypart];
				if (!w.temperature.equals("")) {
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
					textTemperature.setText(w.temperature + "°C");
				}
			}
		}
	}

	private class FragmentUpdater extends AsyncTask<Void, Void, Forecast> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Forecast doInBackground(Void... data) {
			if (forecastUpdater.update()) {
				return dataHelper.selectForecastByCityId(cityId);
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Forecast result) {
			super.onPostExecute(result);
			try {
				if (result != null) {
					updateView(result);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}