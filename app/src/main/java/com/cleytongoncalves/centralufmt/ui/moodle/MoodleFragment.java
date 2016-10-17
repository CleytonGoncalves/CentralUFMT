package com.cleytongoncalves.centralufmt.ui.moodle;


import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.Toast;

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

	@Inject MoodlePresenter mMoodlePresenter;

	@BindView(R.id.moodle_web_view) WebView mWebView;
	@BindView(R.id.progress_moodle) ProgressBar mProgressBar;
	private Unbinder mUnbinder;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).activityComponent().inject(this);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_moodle, container, false);

		mUnbinder = ButterKnife.bind(this, rootView);
		mMoodlePresenter.attachView(this);

		setUpWebViewDefaults();

		CookieManager cookieManager = CookieManager.getInstance();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.createInstance(mWebView.getContext());
		}
		cookieManager.setAcceptCookie(true);
		//cookieManager.removeSessionCookie();

		mMoodlePresenter.onLoadingPage();
		return rootView;
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
		super.onDestroyView();
		mMoodlePresenter.detachView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUnbinder.unbind();
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

	private void setUpWebViewDefaults() {
		mWebView.getSettings().setLoadsImagesAutomatically(true);
		mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setDisplayZoomControls(false);

		mWebView.setWebViewClient(new MyBrowser());
		mWebView.setWebChromeClient(new MyWebChromeClient());
		mWebView.setDownloadListener(new MyDownloadListener());
		mWebView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
					mWebView.goBack();
					return true;
				}
				return false;
			}
		});
	}

	public void onLogInSuccessful(String cookieString) {
		CookieManager.getInstance().setCookie("www.ava.ufmt.br", cookieString);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.getInstance().sync();
		}
		mWebView.loadUrl(FRONT_PAGE_URL);
	}

	@Override
	public void showProgressBar(boolean enabled) {
		mProgressBar.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void showWebView(boolean enabled) {
		mWebView.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void showLoadingTitle() {
		getActivity().setTitle(getString(R.string.title_fragment_moodle_loading));
	}

	@Override
	public void showDefaultTitle() {
		getActivity().setTitle(getString(R.string.title_fragment_moodle));
	}

	@Override
	public void showDownloadStart() {
		Toast.makeText(getActivity(), getString(R.string.download_started), Toast.LENGTH_LONG)
		     .show();
	}

	@Override
	public void showGeneralLogInError() {
		Toast.makeText(getActivity(), R.string.error_generic_log_in, Toast.LENGTH_SHORT).show();
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
			if (url.getHost().equals(AVA_BASE_URL) || url.getHost().equals(ALT_AVA_BASE_URL)) {
				mWebView.loadUrl(url.toString());
				return false;
			}

			Intent intent = new Intent(Intent.ACTION_VIEW, url);
			startActivity(intent);
			return true;
		}
	}

	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			mMoodlePresenter.onLoadingPage();

			if (newProgress == 100) {
				mMoodlePresenter.onLoadComplete();
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

			mMoodlePresenter.onDownloadStart();
		}
	}

}
