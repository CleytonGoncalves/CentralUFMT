package com.cleytongoncalves.centralufmt.ui.moodle;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.Toast;

import com.cleytongoncalves.centralufmt.BuildConfig;
import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

@SuppressWarnings({"deprecation", "FieldCanBeLocal"})
public final class MoodleFragment extends Fragment implements MoodleMvpView {
	private static final String FRONT_PAGE_URL =
			"https://www.ava.ufmt.br/index.php?pag=ambientevirtual";
	
	//AVA Front Page Host
	private static final String AVA_FRONT_HOST = "www.ava.ufmt.br";
	
	//AVA Discipline Pages Host 200.129.241.xxx
	private static final String AVA_DISCIPLINE_HOST = "200.129.241";
	
	@Inject MoodlePresenter mPresenter;
	
	@BindView(R.id.moodle_progress_bar) ContentLoadingProgressBar mProgressBar;
	@BindView(R.id.moodle_web_view) WebView mWebView;
	private Unbinder mUnbinder;
	
	private View mRootView;
	private Snackbar mSnackbar;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).activityComponent().inject(this);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_moodle, container, false);
		
		mUnbinder = ButterKnife.bind(this, mRootView);
		
		//Webview Chrome Debugging
		if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		
		mPresenter.attachView(this);
		mPresenter.init();
		
		showProgressBar(true); //Will stop at MyBrowser.onPageFinished() or onLogInFailure()
		return mRootView;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setUpWebViewConfig();
		setUpCookieConfig();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_moodle, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh_page:
				mWebView.reload();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mWebView.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mWebView.onPause();
	}
	
	@Override
	public void onDestroyView() {
		hideSnackIfShown();
		mPresenter.detachView();
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		mWebView.destroy();
		mUnbinder.unbind();
		super.onDestroy();
		CentralUfmt.getRefWatcher(getActivity()).watch(this);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void setUpWebViewConfig() {
		mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setJavaScriptEnabled(true);
		
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		
		mWebView.setWebViewClient(new MyBrowser());
		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.setDownloadListener(new MyDownloadListener());
		mWebView.setOnKeyListener((v, keyCode, event) -> {
			if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
				mWebView.goBack();
				return true;
			}
			return false;
		});
		
		if (Build.VERSION.SDK_INT >= 19) {
			mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}
	
	private void setUpCookieConfig() {
		CookieManager cookieManager = CookieManager.getInstance();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.createInstance(mWebView.getContext());
		}
		cookieManager.setAcceptCookie(true);
		//cookieManager.removeSessionCookie();
	}
	
	public void showDownloadStart() {
		Toast.makeText(getActivity(), getString(R.string.toast_download_moodle), Toast.LENGTH_LONG)
		     .show();
	}

	/* MVP Methods */
	
	@Override
	public void setCookieString(String cookieString) {
		CookieManager.getInstance().setCookie(AVA_FRONT_HOST, cookieString);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().sync();
		}
	}
	
	@Override
	public void loadStartPage() {
		mWebView.loadUrl(FRONT_PAGE_URL);
	}
	
	@Override
	public void showProgressBar(boolean enabled) {
		boolean isShown = mProgressBar.isShown();
		if (enabled && ! isShown) {
			mProgressBar.show();
		} else if (! enabled && isShown) {
			mProgressBar.hide();
		}
	}
	
	@Override
	public void showWebView(boolean enabled) {
		mWebView.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
	}
	
	@Override
	public void showGeneralLogInError() {
		mSnackbar = Snackbar.make(mRootView, getString(R.string.error_generic_log_in),
		                          Snackbar.LENGTH_INDEFINITE)
		                    .setAction(getString(R.string.snack_retry_login_moodle),
		                               v -> mPresenter.logIn());
		mSnackbar.show();
	}
	
	@Override
	public void hideSnackIfShown() {
		if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
			mSnackbar.dismiss();
		}
	}
	
	private class MyBrowser extends WebViewClient {
		private static final String ASSET_PATH = "moodle";
		private static final String FRONT_PATH = "/front";
		private static final String DISCIPLINE_PATH = "/discipline";
		
		private static final String MIME_JS = "text/javascript";
		private static final String MIME_CSS = "text/css";
		private static final String MIME_PNG = "image/png";
		
		//Used on the blacklist to substitute resources that should not load
		private static final String EMPTY_RESOURCE = "empty.js";
		
		private AssetManager mAssetManager;
		private String[] mFrontAssetList;
		private String[] mDisciplineAssetList;
		private String[] mResourceBlackList = {"barra.js"};
		
		private MyBrowser() {
			mAssetManager = getActivity().getAssets();
			
			try {
				mFrontAssetList = mAssetManager.list(ASSET_PATH + FRONT_PATH);
				mDisciplineAssetList = mAssetManager.list(ASSET_PATH + DISCIPLINE_PATH);
			} catch (IOException e) {
				mFrontAssetList = new String[0];
				mDisciplineAssetList = new String[0];
				Timber.w(e, "Error loading asset path: %s", ASSET_PATH);
			}
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return handleUrl(Uri.parse(url));
		}
		
		@TargetApi(Build.VERSION_CODES.N)
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			return handleUrl(request.getUrl());
		}
		
		private boolean handleUrl(Uri url) {
			if (mPresenter == null) { return false; }
			
			if (! url.getHost().startsWith(AVA_FRONT_HOST) && ! url.getHost().startsWith(
					AVA_DISCIPLINE_HOST)) {
				//Opens pages that aren't from Moodle in the browser
				Intent intent = new Intent(Intent.ACTION_VIEW, url);
				startActivity(intent);
				return true;
			}
			
			mWebView.loadUrl(url.toString());
			return false;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (mPresenter == null) { return; }
			showProgressBar(true);
			super.onPageStarted(view, url, favicon);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			if (mPresenter == null) { return; }
			showProgressBar(false);
			super.onPageFinished(view, url);
		}
		
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
			return loadFromAssetsIfAvailable(url);
		}
		
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view,
		                                                  WebResourceRequest request) {
			String url = request.getUrl().toString();
			return loadFromAssetsIfAvailable(url);
		}
		
		private WebResourceResponse loadFromAssetsIfAvailable(String url) {
			WebResourceResponse response = isResourceBlackListed(url);
			if (response != null) { return response; }
			
			
			if (url.contains(AVA_FRONT_HOST)) {
				response = searchAssets(url, mFrontAssetList, ASSET_PATH + FRONT_PATH);
			} else {
				response = searchAssets(url, mDisciplineAssetList, ASSET_PATH + DISCIPLINE_PATH);
			}
			
			return response;
		}
		
		/**
		 * @param url url containing the resource
		 * @return if blacklisted, a resource to use instead of it. Otherwise, null.
		 */
		private WebResourceResponse isResourceBlackListed(String url) {
			WebResourceResponse response = null;
			
			for (String resource : mResourceBlackList) {
				if (url.endsWith(resource)) {
					try {
						InputStream emptyInput =
								mAssetManager.open(EMPTY_RESOURCE);
						response = new WebResourceResponse(MIME_JS, "", emptyInput);
						Timber.d("Resource '%s' prevented from loading", resource);
					} catch (IOException e) {
						Timber.w(e, "Error loading 'empty.js' from assets");
					}
				}
			}
			
			return response;
		}
		
		/**
		 * Looks if the resource being loaded is available on disk
		 * @param url URL of the resource
		 * @param assetList Array containing the assets available
		 * @param path Path of the assets
		 * @return The resource loaded from the disk, or null if not available.
		 */
		private WebResourceResponse searchAssets(String url, String[] assetList, String path) {
			WebResourceResponse response = null;
			boolean found = false;
			for (int i = 0, length = assetList.length; i < length && ! found; i++) {
				String asset = assetList[i];
				
				if (url.endsWith(asset)) {
					found = true;
					String mimeType;
					if (asset.endsWith(".js")) {
						mimeType = MIME_JS;
					} else if (asset.endsWith(".png")) {
						mimeType = MIME_PNG;
					} else if (asset.endsWith(".css") || asset.endsWith(".php")) {
						mimeType = MIME_CSS; //it might have some php files that actually are css
					} else {
						Timber.wtf("Asset type not found: %s", url);
						break;
					}
					
					try {
						InputStream input = mAssetManager.open(path + "/" + asset);
						response = new WebResourceResponse(mimeType, "", input);
						Timber.d("Resource '%s' loaded from assets", asset);
					} catch (IOException e) {
						Timber.w(e, "Error loading %s from assets", asset);
					}
				}
			}
			
			return response;
		}
		
		@Override
		/* TEMPORARY WORKAROUND - Adds the AVA SSL Certificate as trusted on the webview */
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			Timber.d("Received SSL Error %s.", error.getPrimaryError());
			
			InputStream certStream = null;
			try {
				certStream = getContext().getResources().openRawResource(R.raw.ava_certificate);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(certStream);
				SslCertificate sslCert = new SslCertificate(x509Cert);
				
				if (error.getCertificate().toString().equals(sslCert.toString())) {
					handler.proceed();
				} else {
					handler.cancel();
				}
			} catch (Exception e) {
				Timber.wtf(e, "Failed to load AVA SSL Certificate.");
			}
			
			if (certStream != null) {
				try {
					certStream.close();
				} catch (IOException e) {
					Timber.i(e, "AVA SSL Certificate InputStream closure error.");
				}
			}
		}
	}
	
	private class MyDownloadListener implements DownloadListener {
		@Override
		public void onDownloadStart(String url, String userAgent, String contentDisposition,
		                            String mimetype, long contentLength) {
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			
			request.setMimeType(mimetype);
			request.setVisibleInDownloadsUi(true);
			request.allowScanningByMediaScanner();
			
			String cookies = CookieManager.getInstance().getCookie(url);
			request.addRequestHeader("cookie", cookies);
			request.addRequestHeader("User-Agent", userAgent);
			
			String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
			request.setTitle(fileName);
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
			                                          fileName);
			request.setNotificationVisibility(DownloadManager.Request
					                                  .VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			
			DownloadManager dm = (DownloadManager) getActivity()
					                                       .getSystemService(Context
							                                                         .DOWNLOAD_SERVICE);
			dm.enqueue(request);
			
			if (mPresenter != null) { showDownloadStart(); }
		}
	}
	
}
