package ru.ifmo.mobdev.mekhanikov.weather;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	static final String PREF_NAME = "WeatherPrefs";
	static final String PREF_CUR_POS = "CurrentPosition";
	static String PREF_UPDATE_PERIOD = "UpdatePeriod";
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	WeatherDataHelper dataHelper = null;
	List<City> cities = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		dataHelper = new WeatherDataHelper(this);
		cities = dataHelper.selectAllCities();
		
		SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
		int position = settings.getInt(PREF_CUR_POS, 0);
		mViewPager.setCurrentItem(position);
		if (WeatherService.running) {
			stopService(new Intent(MainActivity.this, WeatherService.class));
		}
		int updatePeriod = settings.getInt(PREF_UPDATE_PERIOD, 10);
		if (!WeatherService.running) {
			startService(new Intent(MainActivity.this, WeatherService.class));
		} else if (WeatherService.running && updatePeriod != WeatherService.period) {
			stopService(new Intent(MainActivity.this, WeatherService.class));
			startService(new Intent(MainActivity.this, WeatherService.class));
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor settingsEditor = settings.edit();
		settingsEditor.putInt(PREF_CUR_POS, mViewPager.getCurrentItem());
		settingsEditor.commit();
	}
	
	public void goToCityChoose(View view) {
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void update(View view) {
		try {
			((WeatherFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem())).update();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = new WeatherFragment();
			Bundle args = new Bundle();
			args.putString("cityId", cities.get(position).cityId);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return cities.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (cities.size() > position) {
				return cities.get(position).cityName;
			} else { 
				return null;
			}
		}
	}

}
