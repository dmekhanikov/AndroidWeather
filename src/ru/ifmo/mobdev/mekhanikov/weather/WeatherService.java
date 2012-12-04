package ru.ifmo.mobdev.mekhanikov.weather;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class WeatherService extends Service {
	private static String PREF_NAME = "WeatherPrefs";
	private static String PREF_UPDATE_PERIOD = "UpdatePeriod";
	public static long period = 0;
	public static boolean running = false;
	Timer timer = null;
	TimerTask tTask = null;
	ForecastUpdater forecastUpdater = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		timer = new Timer();
		forecastUpdater = new ForecastUpdater(this);
	}

	@Override
	public void onStart(Intent intent, int startid) {
		SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
		period = settings.getInt(PREF_UPDATE_PERIOD, 10);
		if (tTask != null) {
			tTask.cancel();
		}
		tTask = new TimerTask() {
			public void run() {
				forecastUpdater.update();
			}
		};
		timer.schedule(tTask, period * 60 * 1000, period * 60 * 1000);
		running = true;
	}

	@Override
	public void onDestroy() {
		if (tTask != null) {
			tTask.cancel();
		}
		running = false;
	}
}