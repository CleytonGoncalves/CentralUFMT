package com.cleytongoncalves.centralufmt.data.remote;

import java.util.List;
import java.util.Map;

public final class NetworkOperation {
	static final int NETWORK_IO_ERROR = -1;

	private String mResponseBody;
	private Map<String, List<String>> mResponseHeaders;

	private int mErrorCode;

	NetworkOperation(String responseBody, Map<String, List<String>> responseHeaders) {
		mResponseBody = responseBody;
		mResponseHeaders = responseHeaders;
	}

	NetworkOperation(int errorCode) {
		this.mErrorCode = errorCode;
	}

	public boolean isSuccessful() {
		return mResponseBody != null;
	}

	public String getResponseBody() {
		return mResponseBody;
	}

	public Map<String, List<String>> getResponseHeaders() {
		return mResponseHeaders;
	}
	
	public int getErrorCode() {
		return mErrorCode;
	}
}
