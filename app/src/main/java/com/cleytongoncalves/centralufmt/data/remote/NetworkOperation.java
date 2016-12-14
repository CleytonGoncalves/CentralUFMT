package com.cleytongoncalves.centralufmt.data.remote;

import java.util.List;
import java.util.Map;

public final class NetworkOperation {
	static final String NETWORK_ERROR = "Network Error";
	static final String IO_ERROR = "I/O Error";

	private String mResponseBody;
	private Map<String, List<String>> mResponseHeaders;

	private String mFailureReason;

	NetworkOperation(String responseBody, Map<String, List<String>> responseHeaders) {
		mResponseBody = responseBody;
		mResponseHeaders = responseHeaders;
	}

	NetworkOperation(String failureReason) {
		this.mFailureReason = failureReason;
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

	public String getFailureReason() {
		return mFailureReason;
	}
}
