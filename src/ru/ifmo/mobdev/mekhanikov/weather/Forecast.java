package ru.ifmo.mobdev.mekhanikov.weather;

public class Forecast {
	public String curTemp = null;
	public String curPicName = null;
	public String curPres = null;
	public String curWind = null;
	public String curHum = null;
	public WeatherState[][] forecast = null;
	
	public Forecast() {
		forecast = new WeatherState[4][4];
		for (int i = 0; i != 4; ++i) {
			for (int j = 0; j != 4; ++j) {
				forecast[i][j] = new WeatherState();
			}
		}
	}
}