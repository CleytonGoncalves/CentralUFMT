package com.cleytongoncalves.centralufmt.data.remote;

import java.util.List;
import java.util.Map;

public final class NetworkOperation {
	public static final String NETWORK_ERROR = "Network Error";
	public static final String IO_ERROR = "I/O Error";

	private String mResponseBody;
	private Map<String, List<String>> mResponseHeaders;

	private String mFailureReason;

	NetworkOperation(String responseBody, Map<String, List<String>> responseHeaders) {
		mResponseBody = responseBody;
		mResponseHeaders = responseHeaders;
	}

	NetworkOperation(String reason) {
		this.mFailureReason = reason;
	}

	public String getResponseBody() {
		return mResponseBody;
	}

	public Map<String, List<String>> getResponseHeaders() {
		return mResponseHeaders;
	}

	public boolean hasFailed() {
		return mFailureReason != null;
	}

	public String getFailureReason() {
		return mFailureReason;
	}
}
