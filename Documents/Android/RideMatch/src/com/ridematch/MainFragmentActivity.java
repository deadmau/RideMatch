package com.ridematch;


import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.FrameLayout;

import com.parse.ParseAnalytics;

public class MainFragmentActivity extends FragmentActivity implements ActionBar.TabListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	ActionBar actionBar;
	
	FragmentTransaction mainFragmentTransaction;
	FragmentManager mainFragmentManager;
	InboxTab inboxFragment;
	
	private BroadcastReceiver resultReceiver;
	private BroadcastReceiver offerReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new Application();
		setContentView(R.layout.activity_mainfragment);
		ParseAnalytics.trackAppOpened(getIntent());

		// Set up the action bar.
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mainFragmentManager = getSupportFragmentManager();
		mSectionsPagerAdapter = new SectionsPagerAdapter(mainFragmentManager);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		inboxFragment = new InboxTab(this);
		FrameLayout cursor = (FrameLayout) findViewById(R.id.listFragment);

		if (mainFragmentManager.findFragmentByTag("inbox") == null) {
			mainFragmentTransaction = mainFragmentManager.beginTransaction();
			mainFragmentTransaction.add(cursor.getId(), inboxFragment, "inbox");
			mainFragmentTransaction.commit();
		}
	}
	
	@Override 
	protected void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter("com.ridematch.UPDATE_STATUS");
		resultReceiver = new BroadcastReceiver() {
				final String TAG = "RideMatchReceiver";
				@Override
				public void onReceive(Context context, Intent intent) {
					Bundle received = intent.getExtras();
					if(received == null || received.getString("com.parse.Data") == null) {
					} else{
						try {
							JSONObject json = new JSONObject(received.getString("com.parse.Data"));
							String driverinfo = "Driver: " + json.getString("driver");
							String userinfo = json.getString("attendee");
							ArrayList<String> arr = new ArrayList<String>();
							arr.add(driverinfo);
							String str = "Name: ";
							for(int i = 0; i < userinfo.length(); i++){
								if(userinfo.charAt(i) == '.') {
									arr.add(str);
									str = "Name: ";
								} else{
									str += userinfo.charAt(i);
								}
							}
							InboxTab.arr = arr;
							
						} catch (JSONException e) {
							Log.d(TAG, "JSONException: " + e.getMessage());
						}
					}
				}
		};
		IntentFilter intentFilter2 = new IntentFilter("com.ridematch.MAIN");
		offerReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {}
		};
		registerReceiver(resultReceiver, intentFilter);
		registerReceiver(offerReceiver, intentFilter2);
		if(ResultReceiver.matchResult) {
			actionBar.setSelectedNavigationItem(2);
		} else if(OfferReceiver.matchOffer) {
			actionBar.setSelectedNavigationItem(1);
		} else {
			actionBar.setSelectedNavigationItem(0);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(resultReceiver);
		unregisterReceiver(offerReceiver);
		ResultReceiver.matchResult = false;
		OfferReceiver.matchOffer = false;
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction arg1) {
		
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction arg1) {
		mViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction arg1) {
		
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		Context mContext;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			switch(position) {
			case 0:
				return new ChatTab(mContext);
			case 1:
				return new RequestTab(mContext);
			case 2:
				return new InboxTab(mContext);
			case 3:
				return new SettingsTab(mContext);
			}
			return null;
		}

		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.chat_tab).toUpperCase(l);
			case 1:
				return getString(R.string.request_tab).toUpperCase(l);
			case 2:
				return getString(R.string.inbox_tab).toUpperCase(l);
			case 3:
				return getString(R.string.action_settings).toUpperCase(l);
			}
			return null;
		}
	}
}
