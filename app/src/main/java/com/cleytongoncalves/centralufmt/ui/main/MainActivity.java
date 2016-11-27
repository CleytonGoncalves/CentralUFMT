package com.cleytongoncalves.centralufmt.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;
import com.cleytongoncalves.centralufmt.ui.map.MapFragment;
import com.cleytongoncalves.centralufmt.ui.moodle.MoodleFragment;
import com.cleytongoncalves.centralufmt.ui.schedule.ScheduleFragment;
import com.cleytongoncalves.centralufmt.util.NetworkUtil;
import com.google.android.gms.maps.MapView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static android.support.v4.app.FragmentManager.OnBackStackChangedListener;

public class MainActivity extends BaseActivity
		implements OnNavigationItemSelectedListener, OnBackStackChangedListener {

	@Inject DataManager mDataManager;
	@BindView(R.id.drawer_layout) DrawerLayout mDrawer;
	@BindView(R.id.nav_view) NavigationView mNavigationView;

	private ActionBarDrawerToggle mDrawerToggle;
	private FragmentManager mFragmentManager;
	private Handler mHandler;
	private Runnable mPendingRunnable;

	private Unbinder mUnbinder;

	public static Intent getStartIntent(Context context, boolean clearPreviousActivites) {
		Intent intent = new Intent(context, MainActivity.class);
		if (clearPreviousActivites) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		}
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		activityComponent().inject(this);
		mUnbinder = ButterKnife.bind(this);

		setUpDrawer(toolbar);
		mHandler = new Handler();

		mFragmentManager = getSupportFragmentManager();
		mFragmentManager.addOnBackStackChangedListener(this);
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();

		//Hacky way to pre-load the Play Services and Map data
		new Thread(() -> {
			try {
				MapView mapView = new MapView(getApplicationContext());
				mapView.onCreate(null);
				mapView.onPause();
				mapView.onDestroy();
			} catch (RuntimeException ignored) {
				//Exception: Can't create handler inside thread that has not called Looper
				// .prepare()
				//This hack works regardless
			}
		}).start();

		if (NetworkUtil.isNetworkConnected(this)) {
			//Sign in ahead of time. Webview is already slow enough by itself.
			mDataManager.triggerMoodleLogIn();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		//TODO: SHOW "PRESS BACK AGAIN CLOSES APP" MESSAGE
		if (mDrawer.isDrawerOpen(GravityCompat.START)) {
			mDrawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onBackStackChanged() {
		Fragment currFragment = mFragmentManager.findFragmentById(R.id.container_main);

		//TODO: ADD REMAINING FRAGMENTS TO CHANGE TITLE/SELECTION
		String title;
		if (currFragment != null) {
			String fragTag = currFragment.getTag();

			if (fragTag.equals(MapFragment.class.getName())) {
				title = getString(R.string.title_fragment_map);
				mNavigationView.getMenu().findItem(R.id.nav_map).setChecked(true);
			} else if (fragTag.equals(MoodleFragment.class.getName())) {
				title = getString(R.string.title_fragment_moodle);
				mNavigationView.getMenu().findItem(R.id.nav_moodle).setChecked(true);
			} else if (fragTag.equals(ScheduleFragment.class.getName())) {
				title = getString(R.string.title_fragment_schedule);
				mNavigationView.getMenu().findItem(R.id.nav_schedule).setChecked(true);
			} else {
				title = getString(R.string.title_activity_main);
			}
		} else {
			title = getString(R.string.title_activity_main);
		}

		setTitle(title);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();

		Class fragmentClass;
		switch (id) {
			case R.id.nav_schedule:
				fragmentClass = ScheduleFragment.class;
				break;
			case R.id.nav_student:
				fragmentClass = null;
				break;
			case R.id.nav_curriculum:
				fragmentClass = null;
				break;
			case R.id.nav_moodle:
				fragmentClass = MoodleFragment.class;
				break;
			case R.id.nav_map:
				fragmentClass = MapFragment.class;
				break;
			case R.id.nav_manage:
				fragmentClass = null;
				break;
			case R.id.nav_share:
				fragmentClass = null;
				break;
			//TODO: IMPLEMENT APP SHARE
			default:
				fragmentClass = null; //TODO: SET THE MAIN FRAGMENT AS DEFAULT
		}

		if (fragmentClass != null) {
			mPendingRunnable = () -> {
				try {
					Fragment fragment = (Fragment) fragmentClass.newInstance();
					goToFragment(fragment);
				} catch (Exception e) {
					Timber.e(e, "Error on fragment instantiation");
				}
			};
		}

		mDrawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPendingRunnable != null) { mHandler.removeCallbacks(mPendingRunnable); }
		mUnbinder.unbind();
	}

	private void setUpDrawer(Toolbar toolbar) {
		mDrawerToggle = new MyActionBarDrawerToggle(this, mDrawer, toolbar,
		                                            R.string.navigation_drawer_open, R.string
						                                                           .navigation_drawer_close);

		mDrawer.addDrawerListener(mDrawerToggle);

		View headerView = mNavigationView.getHeaderView(0);
		TextView mainText = (TextView) headerView.findViewById(R.id.nav_header_name);
		TextView secondaryText = (TextView) headerView.findViewById(R.id.nav_header_rga);
		TextView tertiaryText = (TextView) headerView.findViewById(R.id.nav_header_curso);

		if (mDataManager.isLoggedInSiga()) {
			Student student = mDataManager.getStudent();
			mainText.setText(student.getFirstName() + " " + student.getLastName());
			secondaryText.setText(student.getRga());
			tertiaryText.setText(student.getCourse().getTitle());
		} else {
			mainText.setText(getString(R.string.not_logged_in));
			secondaryText.setVisibility(View.GONE);
			tertiaryText.setVisibility(View.GONE);

			mNavigationView.getMenu().setGroupEnabled(R.id.nav_group_logged_only, false);
		}

		if (! NetworkUtil.isNetworkConnected(this)) {
			mNavigationView.getMenu().findItem(R.id.nav_moodle).setEnabled(false);
			Toast.makeText(this, getString(R.string.toast_offline_main), Toast.LENGTH_LONG)
			     .show();
		}

		mNavigationView.setNavigationItemSelectedListener(this);
		//mNavigationView.getMenu().getItem(0).setChecked(true);
	}

	private void goToFragment(Fragment fragment) {
		String fragmentTag = fragment.getClass().getName();
		boolean fragmentPopped = mFragmentManager.popBackStackImmediate(fragmentTag, 0);

		if (! fragmentPopped && mFragmentManager.findFragmentByTag(fragmentTag) == null) {
			mFragmentManager.beginTransaction()
			                .replace(R.id.container_main, fragment, fragmentTag)
			                .addToBackStack(fragmentTag)
			                .commitAllowingStateLoss();
		}
	}

	private class MyActionBarDrawerToggle extends ActionBarDrawerToggle {

		MyActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
		                        Toolbar toolbar,
		                        @StringRes int openDrawerContentDescRes,
		                        @StringRes int closeDrawerContentDescRes) {
			super(activity, drawerLayout, toolbar, openDrawerContentDescRes,
			      closeDrawerContentDescRes);
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			if (mPendingRunnable != null) {
				mHandler.post(mPendingRunnable);
				mPendingRunnable = null;
			}
			super.onDrawerClosed(drawerView);
		}
	}
}
