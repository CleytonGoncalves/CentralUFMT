package com.cleytongoncalves.centralufmt.ui.map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;
import com.google.android.gms.maps.MapView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class MapFragment extends Fragment implements MapMvpView {
	private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

	@Inject MapPresenter mPresenter;

	@BindView(R.id.map_view) MapView mMapView;
	private Unbinder mUnbinder;
	
	private Menu mOptionsMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		((BaseActivity) getActivity()).activityComponent().inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_map, container, false);

		mUnbinder = ButterKnife.bind(this, rootView);
		mPresenter.attachView(this);

		Bundle mapViewBundle = null;
		if (savedInstanceState != null) {
			mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
		}

		mMapView.onCreate(mapViewBundle);
		mMapView.getMapAsync(gMap -> mPresenter.onMapReady(gMap, getActivity()));

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		mMapView.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
		if (mapViewBundle == null) {
			mapViewBundle = new Bundle();
			outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
		}

		mMapView.onSaveInstanceState(mapViewBundle);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mMapView.onStop();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.detachView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		mUnbinder.unbind();
		CentralUfmt.getRefWatcher(getActivity()).watch(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_map, menu);
		mOptionsMenu = menu;
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.menu_show_route):
				mPresenter.toggleBusRoute();
				return true;
			case (R.id.menu_show_poi):
				mPresenter.togglePointsOfInterest();
				return true;
			default:
				return false;
		}
	}

	/* MVP Methods */

	@Override
	public void setPoiMenuState(boolean state) {
		mOptionsMenu.findItem(R.id.menu_show_poi).setChecked(state);
	}

	@Override
	public void setBusRouteMenuState(boolean state) {
		mOptionsMenu.findItem(R.id.menu_show_route).setChecked(state);
	}

	@Override
	public void showBusRouteError() {
		Toast.makeText(getActivity(),
		               getString(R.string.toast_layer_error_map,
		                         getString(R.string.title_route_map)),
		               Toast.LENGTH_SHORT)
		     .show();
	}

	@Override
	public void showPoiError() {
		Toast.makeText(getActivity(),
		               getString(R.string.toast_layer_error_map, getString(R.string
				                                                                   .title_poi_map)),

		               Toast.LENGTH_SHORT)
		     .show();
	}
}
