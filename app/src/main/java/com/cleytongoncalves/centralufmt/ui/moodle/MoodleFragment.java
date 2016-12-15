package com.cleytongoncalves.centralufmt.ui.moodle;


import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@SuppressWarnings({"deprecation", "FieldCanBeLocal"})
public final class MoodleFragment extends Fragment implements MoodleMvpView {
	private static final String FRONT_PAGE_URL = "http://www.ava.ufmt.br/index.php?pag=ambientevirtual";
	private static final String AVA_BASE_URL = "www.ava.ufmt.br";
	private static final String ALT_AVA_BASE_URL = "200.129.241.132";

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
		mPresenter.attachView(this);

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

	private void setUpWebViewConfig() {
		mWebView.getSettings().setLoadsImagesAutomatically(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDisplayZoomControls(false);

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
		CookieManager.getInstance().setCookie(AVA_BASE_URL, cookieString);
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
		                               v -> mPresenter.doLogIn());
		mSnackbar.show();
	}

	@Override
	public void hideSnackIfShown() {
		if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
			mSnackbar.dismiss();
		}
	}

	private class MyBrowser extends WebViewClient {
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

			if (url.getHost().equals(AVA_BASE_URL) || url.getHost().equals(ALT_AVA_BASE_URL)) {
				mWebView.loadUrl(url.toString());
				return false;
			}

			Intent intent = new Intent(Intent.ACTION_VIEW, url);
			startActivity(intent);
			return true;
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
