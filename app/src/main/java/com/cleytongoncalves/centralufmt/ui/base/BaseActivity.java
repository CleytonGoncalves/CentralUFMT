package com.cleytongoncalves.centralufmt.ui.base;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;

import com.cleytongoncalves.centralufmt.CentralUfmt;
import com.cleytongoncalves.centralufmt.injection.component.ActivityComponent;
import com.cleytongoncalves.centralufmt.injection.component.DaggerActivityComponent;
import com.cleytongoncalves.centralufmt.injection.module.ActivityModule;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
	private ActivityComponent mActivityComponent;

	public ActivityComponent activityComponent() {
		if (mActivityComponent == null) {
			mActivityComponent = DaggerActivityComponent.builder()
			                                            .activityModule(new ActivityModule(this))
			                                            .applicationComponent(CentralUfmt.get(this)
			                                                                             .getComponent())
			                                            .build();
		}

		return mActivityComponent;
	}

}
