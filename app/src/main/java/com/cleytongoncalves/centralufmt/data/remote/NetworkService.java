package com.cleytongoncalves.centralufmt.data.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public final class NetworkService {
	private static final String CHARSET = "ISO-8859-1";
	private final OkHttpClient mClient;

	/**
	 * Generated through this class Factory
	 */
	private NetworkService(OkHttpClient client) {
		this.mClient = client;
	}

	public NetworkOperation get(String url) {
		Request request = new Request.Builder()
				                  .url(url)
				                  .build();

		NetworkOperation result;
		try {
			Response response = mClient.newCall(request).execute();

			if (response.code() == 200) {
				result = new NetworkOperation(new String(response.body().bytes(), CHARSET),
						                             response.headers().toMultimap());
			} else {
				result = new NetworkOperation(NetworkOperation.NETWORK_ERROR);
			}
			response.close();
		} catch (IOException e) {
			Timber.i(e, "Get operation failed on %s failed.", url);
			result = new NetworkOperation(NetworkOperation.IO_ERROR);
		}

		Timber.d("Get Operation on %s - Successful: %s", url, ! result.hasFailed());
		return result;
	}

	public NetworkOperation post(String url, FormBody params) {
		Request request = new Request.Builder()
				                  .url(url)
				                  .post(params)
				                  .build();

		NetworkOperation result;
		try {
			Response response = mClient.newCall(request).execute();

			if (response.code() == 200) {
				result = new NetworkOperation(new String(response.body().bytes(), CHARSET),
						                             response.headers().toMultimap());
			} else {
				result = new NetworkOperation(NetworkOperation.NETWORK_ERROR);
			}
			response.close();
		} catch (IOException e) {
			Timber.i(e, "Post operation failed on %s failed.", url);
			result = new NetworkOperation(NetworkOperation.IO_ERROR);
		}

		Timber.d("Post Operation on %s - Successful: %s", url, ! result.hasFailed());
		return result;
	}

	public List<Cookie> getCookieFromJar(String baseUrl) {
		return mClient.cookieJar().loadForRequest(HttpUrl.parse(baseUrl));
	}

	/********
	 * Factory class that sets up a new network service
	 *******/
	public static class Factory {
		public static NetworkService make() {
			OkHttpClient client = new OkHttpClient()
					                      .newBuilder()
					                      .cookieJar(new MyCookieJar())
					                      .build();

			return new NetworkService(client);
		}
	}

	/********
	 * Class that manages OKHTTP Client cookies
	 *******/
	private static class MyCookieJar implements CookieJar {
		private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

		@Override
		public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
			cookieStore.put(url.host(), cookies);
		}

		@Override
		public List<Cookie> loadForRequest(HttpUrl url) {
			List<Cookie> cookies = cookieStore.get(url.host());
			return cookies != null ? cookies : new ArrayList<>();
		}
	}

}
