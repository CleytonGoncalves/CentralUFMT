package com.cleytongoncalves.centralufmt.ui.map;

import android.content.Context;
import android.util.Log;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.KmlLayer;

import javax.inject.Inject;

public final class MapPresenter implements Presenter<MapMvpView> {
	private static final String TAG = MapPresenter.class.getSimpleName();
	private static final double UFMT_LAT = - 15.611124;
	private static final double UFMT_LNG = - 56.066220;
	private static final float ZOOM_LVL = (float) 15.6;
	private static final float BEARING_LVL = 30;
	private static final int POI_MARKERS_ID = R.raw.ufmt_poi_markers;
	private static final int ROUTE_MARKERS_ID = R.raw.ufmt_route_markers;

	private final DataManager mDataManager;
	private MapMvpView mMapMvpView;
	private GoogleMap mGoogleMap;
	private KmlLayer mRouteLayer;
	private KmlLayer mPoiLayer;
	private boolean mIsRouteDisplayed;
	private boolean mIsPoiDisplayed;

	@Inject
	MapPresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(MapMvpView mvpView) {
		mMapMvpView = mvpView;
	}

	@Override
	public void detachView() {
		mMapMvpView = null;
		mGoogleMap = null;
	}

	void onMapReady(GoogleMap googleMap, Context context) {
		mGoogleMap = googleMap;
		setCameraPosition();
		createLayers(context);

		if (mDataManager.getPreferencesHelper().getMapRouteDisplayState()) {
			toggleRoute();
		}
		if (mDataManager.getPreferencesHelper().getMapPoiDisplayState()) {
			togglePointsOfInterest();
		}
	}

	private void setCameraPosition() {
		LatLng ufmtCoord = new LatLng(UFMT_LAT, UFMT_LNG);

		CameraPosition position = new CameraPosition.Builder()
				                          .target(ufmtCoord)
				                          .zoom(ZOOM_LVL)
				                          .bearing(BEARING_LVL)
				                          .build();

		mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
	}

	private void createLayers(Context context) {
		mIsPoiDisplayed = false;
		mIsRouteDisplayed = false;

		try {
			mRouteLayer = new KmlLayer(mGoogleMap, ROUTE_MARKERS_ID, context);
			mPoiLayer = new KmlLayer(mGoogleMap, POI_MARKERS_ID, context);
		} catch (Exception e) {
			Log.e(TAG, "ERROR CREATING LAYER: " + e.getMessage());
		}
	}

	void toggleRoute() {
		//Saves the user preference before realizing the operation,
		//to ensure correct option in case of layer addition failure.
		mDataManager.getPreferencesHelper().putMapRouteDisplayState(! mIsRouteDisplayed);

		if (mIsRouteDisplayed) {
			mRouteLayer.removeLayerFromMap();
			mIsRouteDisplayed = false;
		} else {
			try {
				//If it fails, mIsRouteDisplayed will remain false
				mRouteLayer.addLayerToMap();
				mIsRouteDisplayed = true;
			} catch (Exception e) {
				Log.e(TAG, "ERROR OVERLAYING ROUTE: " + e.getMessage());
			}
		}
		mMapMvpView.setRouteMenuState(mIsRouteDisplayed);
	}

	void togglePointsOfInterest() {
		//Saves the user preference before realizing the operation,
		//to ensure correct option in case of layer addition failure.
		mDataManager.getPreferencesHelper().putMapPoiDisplayState(! mIsPoiDisplayed);

		if (mIsPoiDisplayed) {
			mPoiLayer.removeLayerFromMap();
			mIsPoiDisplayed = false;
		} else {
			try {
				//If it fails, mIsPoiDisplayed will remain false
				mPoiLayer.addLayerToMap();
				mIsPoiDisplayed = true;
			} catch (Exception e) {
				Log.e(TAG, "ERROR OVERLAYING POINTS OF INTEREST: " + e.getMessage());
			}
		}
		mMapMvpView.setPoiMenuState(mIsPoiDisplayed);
	}
}
