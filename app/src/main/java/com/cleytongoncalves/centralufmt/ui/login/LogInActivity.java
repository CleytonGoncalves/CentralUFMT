package com.cleytongoncalves.centralufmt.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cleytongoncalves.centralufmt.BuildConfig;
import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.ui.base.BaseActivity;
import com.cleytongoncalves.centralufmt.ui.main.MainActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

//TODO: FIX COPY/PASTE BUG ON LOGIN
public final class LogInActivity extends BaseActivity implements LogInMvpView {
	//TODO: FIND IF THIS MIN LENGTH IS REASONABLE
	private static final int MIN_PASSWORD_LENGTH = 4;
	private static final int MIN_RGA_LENGTH = 8;

	@Inject LogInPresenter mPresenter;
	@BindView(R.id.login_rga_edit_text) EditText mRgaView;
	@BindView(R.id.login_auth_edit_text) EditText mPasswordView;
	@BindView(R.id.login_button_login) Button mLogInButton;
	@BindView(R.id.login_button_anonymous_login) TextView mAnonymousLogInButton;
	
	@BindView(R.id.login_form_layout) LinearLayout mFormView;
	@BindView(R.id.login_progress_bar) ProgressBar mProgressView;
	
	
	private boolean mShouldFinishOnStop;

	/**
	 * Creates the necessary intent to generate this activity.
	 *
	 * @return LogInActivity Intent
	 */
	public static Intent getStartIntent(Context context, boolean clearPreviousActivities) {
		Intent intent = new Intent(context, LogInActivity.class);
		if (clearPreviousActivities) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		}
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		activityComponent().inject(this);
		ButterKnife.bind(this);
		mPresenter.attachView(this);

		mShouldFinishOnStop = false;
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (BuildConfig.DEBUG) {
			//TODO: Remove on final version
			//Auto complete with my credentials for easy login
			mRgaView.setText(BuildConfig.DEFAULT_LOGIN_RGA);
			mPasswordView.setText(BuildConfig.DEFAULT_LOGIN_PASS);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mShouldFinishOnStop) {
			finish();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPresenter.detachView();
	}

	@Override
	public void onBackPressed() {
		if (mPresenter.isLogInHappening()) {
			mPresenter.cancelLogIn();
		} else {
			super.onBackPressed();
		}
	}
	
	@OnEditorAction(R.id.login_auth_edit_text)
	boolean onPasswordAction(int id) {
		if (id == R.id.action_login || id == EditorInfo.IME_NULL) {
			triggerLogIn();
			return true;
		}
		return false;
	}
	
	@OnClick(R.id.login_button_login)
	protected void onLogInClick() {
		triggerLogIn();
	}

	private void triggerLogIn() {
		//TODO: FIX ERROR MESSAGES/ICONS BUG
		if (mPresenter.isLogInHappening()) {
			return;
		}

		//Reset errors
		mRgaView.setError(null);
		mPasswordView.setError(null);

		String rga = mRgaView.getText().toString();
		char[] password = mPasswordView.getText().toString().toCharArray();

		boolean cancel = false;
		View focusView = null;

		if (password.length == 0) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (! isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		if (TextUtils.isEmpty(rga)) {
			mRgaView.setError(getString(R.string.error_field_required));
			focusView = mRgaView;
			cancel = true;
		} else if (! isRgaValid(rga)) {
			mRgaView.setError(getString(R.string.error_invalid_rga));
			focusView = mRgaView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			mPresenter.logIn(rga, password);
		}
	}
	
	@OnClick(R.id.login_button_anonymous_login)
	protected void onAnonymousLogInClick() {
		mPresenter.anonymousLogIn();
	}

	/* MVP Methods */

	@Override
	public void onLogInSuccessful() {
		Intent intent = MainActivity.getStartIntent(this, true);
		startActivity(intent);

		// We need this flag because if we call finish() here, the activity transition won't work
		mShouldFinishOnStop = true;
	}

	@Override
	public void onUserCanceled() {
		Toast.makeText(this, R.string.error_canceled_log_in, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showProgressBar(boolean show) {
		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
	@Override
	public void showLoginForm(boolean show) {
		mFormView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public void showAccessDenied() {
		Toast.makeText(this, R.string.error_access_denied, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showGeneralLogInError() {
		Toast.makeText(this, R.string.error_generic_log_in, Toast.LENGTH_SHORT).show();
	}

	/* Private Helper Methods */

	private boolean isPasswordValid(char[] password) {
		return password.length >= MIN_PASSWORD_LENGTH;
	}

	private boolean isRgaValid(String rga) {
		return TextUtils.isDigitsOnly(rga) && rga.length() >= MIN_RGA_LENGTH;
	}
}
