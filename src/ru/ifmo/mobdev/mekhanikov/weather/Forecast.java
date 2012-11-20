package ru.ifmo.mobdev.mekhanikov.weather;

public class Forecast {
	public long updateTime = 0;
	public WeatherState current = null;
	public WeatherState[][] forecast = null;
	
	public Forecast() {
		current = new WeatherState();
		forecast = new WeatherState[4][4];
		for (int i = 0; i != 4; ++i) {
			for (int j = 0; j != 4; ++j) {
				forecast[i][j] = new WeatherState();
			}
		}
	}
}