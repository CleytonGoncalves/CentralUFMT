package com.cleytongoncalves.centralufmt.ui.map;

import android.content.Context;
import android.os.AsyncTask;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.data.DataManager;
import com.cleytongoncalves.centralufmt.data.local.PreferencesHelper;
import com.cleytongoncalves.centralufmt.ui.base.Presenter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.kml.KmlLayer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import timber.log.Timber;

//Beware: Currently the Google Maps has a bug where it issues several StrictMode Warning.c
public final class MapPresenter implements Presenter<MapMvpView> {
	private static final double UFMT_LAT = - 15.611124;
	private static final double UFMT_LNG = - 56.066220;
	private static final float ZOOM_LVL = (float) 15.6;
	private static final float BEARING_LVL = 30;
	private static final int POI_MARKERS_ID = R.raw.ufmt_poi_markers;
	private static final int ROUTE_MARKERS_ID = R.raw.ufmt_route_markers;

	private final DataManager mDataManager;
	private MapMvpView mView;

	private GoogleMap mGoogleMap;
	private KmlLayerManager mKmlLayerManager;

	@Inject
	MapPresenter(DataManager dataManager) {
		mDataManager = dataManager;
	}

	@Override
	public void attachView(MapMvpView mvpView) {
		mView = mvpView;
	}

	@Override
	public void detachView() {
		mView = null;
		mGoogleMap = null;

		if (EventBus.getDefault().isRegistered(this)) { EventBus.getDefault().unregister(this); }
	}

	void onMapReady(GoogleMap googleMap, final Context context) {
		mGoogleMap = googleMap;
		centerCameraPosition();

		EventBus.getDefault().register(this);
		AsyncTask.execute(() -> {
			KmlLayerManager kmlLayerManager = createKmlLayers(context);
			EventBus.getDefault().post(kmlLayerManager);
		});
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMarkersLoadedEvent(KmlLayerManager kmlLayerManager) {
		EventBus.getDefault().unregister(this);

		mKmlLayerManager = kmlLayerManager;

		PreferencesHelper prefsHelper = mDataManager.getPreferencesHelper();

		if (prefsHelper.getMapRouteDisplayState()) {
			toggleBusRoute();
		}
		if (prefsHelper.getMapPoiDisplayState()) {
			togglePointsOfInterest();
		}
	}

	void toggleBusRoute() {
		if (mKmlLayerManager == null) {
			mView.showBusRouteError();
			return;
		}

		//Saves the user preference before realizing the operation,
		//to ensure correct option in case of layer addition failure.
		boolean newState = ! mKmlLayerManager.isBusRouteDisplayed();
		mDataManager.getPreferencesHelper().putMapBusRouteDisplayState(newState);

		boolean actualState = mKmlLayerManager.toggleBusRoute();
		mView.setBusRouteMenuState(actualState);
	}

	void togglePointsOfInterest() {
		if (mKmlLayerManager == null) {
			mView.showPoiError();
			return;
		}

		//Saves the user preference before realizing the operation,
		//to ensure correct option in case of layer addition failure.
		boolean newState = ! mKmlLayerManager.isPoiDisplayed();
		mDataManager.getPreferencesHelper().putMapPoiDisplayState(newState);

		boolean actualState = mKmlLayerManager.togglePointsOfInterest();
		mView.setPoiMenuState(actualState);
	}

	/* Private Helper Methods */

	private void centerCameraPosition() {
		LatLng ufmtCoord = new LatLng(UFMT_LAT, UFMT_LNG);

		CameraPosition position = new CameraPosition.Builder()
				                          .target(ufmtCoord)
				                          .zoom(ZOOM_LVL)
				                          .bearing(BEARING_LVL)
				                          .build();

		mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
	}

	private KmlLayerManager createKmlLayers(Context context) {
		KmlLayer routeLayer = null;
		KmlLayer poiLayer = null;

		try {
			routeLayer = new KmlLayer(mGoogleMap, ROUTE_MARKERS_ID, context);
			poiLayer = new KmlLayer(mGoogleMap, POI_MARKERS_ID, context);
		} catch (Exception e) {
			Timber.e(e, "Error on KmlLayer creation.");
		}

		return new KmlLayerManager(routeLayer, poiLayer);
	}

	private static class KmlLayerManager {
		private final KmlLayer mBusRouteLayer;
		private final KmlLayer mPoiLayer;

		private boolean mIsRouteDisplayed;
		private boolean mIsPoiDisplayed;

		KmlLayerManager(KmlLayer busRouteLayer, KmlLayer poiLayer) {
			mBusRouteLayer = busRouteLayer;
			mPoiLayer = poiLayer;
		}

		boolean toggleBusRoute() {
			if (mBusRouteLayer == null) {
				Timber.w("Route layer toggled when null.");
				return false;
			}

			if (mIsRouteDisplayed) {
				mBusRouteLayer.removeLayerFromMap();
				mIsRouteDisplayed = false;
			} else {
				try {
					//If it fails, mIsRouteDisplayed will remain false
					mBusRouteLayer.addLayerToMap();
					mIsRouteDisplayed = true;
				} catch (Exception e) {
					Timber.e(e, "Error overlaying route.");
				}
			}

			return mIsRouteDisplayed;
		}

		boolean togglePointsOfInterest() {
			if (mPoiLayer == null) {
				Timber.w("POI layer toggled when null.");
				return false;
			}

			if (mIsPoiDisplayed) {
				mPoiLayer.removeLayerFromMap();
				mIsPoiDisplayed = false;
			} else {
				try {
					//If it fails, mIsPoiDisplayed will remain false
					mPoiLayer.addLayerToMap();
					mIsPoiDisplayed = true;
				} catch (Exception e) {
					Timber.e(e, "Error overlaying points of interest");
				}
			}

			return mIsPoiDisplayed;
		}

		boolean isBusRouteDisplayed() {
			return mIsRouteDisplayed;
		}

		boolean isPoiDisplayed() {
			return mIsPoiDisplayed;
		}
	}
}
