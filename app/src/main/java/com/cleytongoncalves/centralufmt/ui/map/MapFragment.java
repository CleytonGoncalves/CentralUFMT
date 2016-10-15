package com.cleytongoncalves.centralufmt.ui.map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class MapFragment extends Fragment implements MapMvpView {
	@Inject MapPresenter mMapPresenter;

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
		mMapPresenter.attachView(this);

		mMapView.onCreate(savedInstanceState);
		mMapView.onResume();
		MapsInitializer.initialize(CentralUfmt.get(getActivity()));

		mMapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				mMapPresenter.onMapReady(googleMap, CentralUfmt.get(getActivity()));
			}
		});

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
		mMapView.onSaveInstanceState(outState);
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
		mMapPresenter.detachView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
		mUnbinder.unbind();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_map, menu);
		mOptionsMenu = menu;
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		//Hides the Student update refresh button
		menu.findItem(R.id.menu_update_student).setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.menu_show_route):
				mMapPresenter.toggleRoute();
				return true;
			case (R.id.menu_show_poi):
				mMapPresenter.togglePointsOfInterest();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void setPoiMenuState(boolean state) {
		mOptionsMenu.findItem(R.id.menu_show_poi).setChecked(state);
	}

	@Override
	public void setRouteMenuState(boolean state) {
		mOptionsMenu.findItem(R.id.menu_show_route).setChecked(state);
	}
}
