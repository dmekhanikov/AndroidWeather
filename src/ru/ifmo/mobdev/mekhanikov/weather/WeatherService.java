package ru.ifmo.mobdev.mekhanikov.weather;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

public class WeatherService extends IntentService {
	
	private static final String PREF_NAME = "WeatherPrefs";
	private static final String PREF_UPDATE_PERIOD = "UpdatePeriod";
	private static final String SERVICE_NAME = "WeatherService";
	public static long period = 0;
	public static boolean running = false;
	ForecastUpdater forecastUpdater = null;
	
	public WeatherService() {
		super(SERVICE_NAME);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		forecastUpdater = new ForecastUpdater(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		forecastUpdater.update();
		Log.i("progress", "updated");
		scheduleNextUpdate();
	}
	
	private void scheduleNextUpdate() {
		SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
		period = settings.getInt(PREF_UPDATE_PERIOD, 10);
		
		Intent intent = new Intent(this, this.getClass());
	    PendingIntent pendingIntent =
	        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	    long currentTimeMillis = System.currentTimeMillis();
	    long nextUpdateTimeMillis = currentTimeMillis + period * DateUtils.MINUTE_IN_MILLIS;
	    Time nextUpdateTime = new Time();
	    nextUpdateTime.set(nextUpdateTimeMillis);

	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
	}
	
	public static class Autostarter extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			context.startService(new Intent(context, WeatherService.class));
		}
	}
}