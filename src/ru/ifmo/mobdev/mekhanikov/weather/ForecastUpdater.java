package ru.ifmo.mobdev.mekhanikov.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.content.Context;

public class ForecastUpdater {
	private static volatile boolean running = false;
	private WeatherDataHelper dataHelper = null;

	public ForecastUpdater(Context context) {
		dataHelper = new WeatherDataHelper(context);
	}

	public boolean update() {
		if (running) {
			return false;
		}
		running = true;
		boolean updated = false;
		List<City> cities = dataHelper.selectAllCities();
		for (int i = 0; i != cities.size(); ++i) {
			if (downloadForecast(cities.get(i).cityId)) {
				updated = true;
			}
		}
		running = false;
		return updated;
	}

	private boolean downloadForecast(String cityId) {
		URL url;
		URLConnection urlCon;
		String query = "http://xml.weather.co.ua/1.2/forecast/"
				+ cityId + "?dayf=4";
		try {
			url = new URL(query);
			urlCon = url.openConnection();
			urlCon.setConnectTimeout(5000);
			urlCon.connect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		StringBuilder source = null;
		try {
			BufferedReader sourceReader = new BufferedReader(new InputStreamReader(
					urlCon.getInputStream()));
			source = new StringBuilder();
			String line = sourceReader.readLine();
			while (line != null) {
				source.append(line);
				line = sourceReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		Forecast result = new ForecastParser().parse(source.toString());
		if (result != null) {
			dataHelper.updateForecast(cityId, result);
			return true;
		} else {
			return false;
		}
	}
}
