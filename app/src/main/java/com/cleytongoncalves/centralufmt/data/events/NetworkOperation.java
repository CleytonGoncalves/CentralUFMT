package com.cleytongoncalves.centralufmt.data.events;

import java.util.List;
import java.util.Map;

public final class NetworkOperation {
	public static final int NETWORK_FAILURE = - 1;
	public static final int IO_ERROR = - 2;

	private String mResponseBody;
	private Map<String, List<String>> mResponseHeaders;

	private Integer mFailureReason;


	public NetworkOperation(String responseBody, Map<String, List<String>> responseHeaders) {
		mResponseBody = responseBody;
		mResponseHeaders = responseHeaders;
	}

	public NetworkOperation(int reason) {
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

	public int getFailureReason() {
		return mFailureReason;
	}
}
