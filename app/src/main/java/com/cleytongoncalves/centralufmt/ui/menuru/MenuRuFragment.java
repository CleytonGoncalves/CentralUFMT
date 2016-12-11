package com.cleytongoncalves.centralufmt.ui.menuru;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class MenuRuFragment extends Fragment {

	private Unbinder mUnbinder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((BaseActivity) getActivity()).activityComponent().inject(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_map, container, false);

		mUnbinder = ButterKnife.bind(this, rootView);
		//mPresenter.attachView(this);

		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//mPresenter.detachView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUnbinder.unbind();
		CentralUfmt.getRefWatcher(getActivity()).watch(this);
	}

	/* MVP Methods */

}
