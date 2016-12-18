package com.cleytongoncalves.centralufmt.ui.menuru;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class MenuRuFragment extends Fragment implements MenuRuMvpView {
	@Inject MenuRuPresenter mPresenter;

	@BindView(R.id.menuru_progress_bar) ContentLoadingProgressBar mProgressBar;
	@BindView(R.id.menuru_list) RecyclerView mRecyclerView;
	private Unbinder mUnbinder;

	private View mRootView;
	private Snackbar mSnackbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).activityComponent().inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_menuru, container, false);

		mUnbinder = ButterKnife.bind(this, mRootView);
		mPresenter.attachView(this);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		MenuRuAdapter adapter = new MenuRuAdapter();
		mRecyclerView.setAdapter(adapter);
		mPresenter.attachAdapter(adapter);

		mPresenter.loadMenuRu(false);

		return mRootView;
	}

	@Override
	public void onDestroyView() {
		hideSnackIfShown();
		mPresenter.detachView();
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		mUnbinder.unbind();
		super.onDestroy();
		CentralUfmt.getRefWatcher(getActivity()).watch(this);
	}

	/* MVP Methods */

	@Override
	public void showRecyclerView(boolean enabled) {
		mRecyclerView.setVisibility(enabled ? View.VISIBLE : View.GONE);
	}

	@Override
	public void showProgressBar(boolean enabled) {
		if (enabled) {
			mProgressBar.show();
		} else {
			mProgressBar.hide();
		}
	}

	@Override
	public void showGeneralErrorSnack() {
		mSnackbar = Snackbar.make(mRootView, getString(R.string.snack_error_menuru),
		                          Snackbar.LENGTH_INDEFINITE)
		                    .setAction(getString(R.string.snack_reload_schedule),
		                               v -> mPresenter.loadMenuRu(true));
		mSnackbar.show();
	}

	@Override
	public void hideSnackIfShown() {
		if (mSnackbar != null && mSnackbar.isShownOrQueued()) {
			mSnackbar.dismiss();
		}
	}
}
