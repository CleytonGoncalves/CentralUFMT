package com.cleytongoncalves.centralufmt.data.remote;

import android.util.Log;

import com.cleytongoncalves.centralufmt.data.events.NetworkOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class NetworkService {

	private static final String TAG = "NetworkService";
	private static final String CHARSET = "ISO-8859-1";
	private final OkHttpClient mClient;

	/**
	 * Generated through this class Factory
	 */
	private NetworkService(OkHttpClient client) {
		this.mClient = client;
	}

	NetworkOperation get(String url) {
		Request request = new Request.Builder()
				                  .url(url)
				                  .cacheControl(CacheControl.FORCE_NETWORK) //TODO: ADDED BECAUSE
				                  // OF AVA LOGIN BUG, EVALUATE
				                  .build();

		NetworkOperation result;
		try {
			Response response = mClient.newCall(request).execute();

			if (response.code() == 200) {
				result = new NetworkOperation(new String(response.body().bytes(), CHARSET),
						                             response.headers().toMultimap());
			} else {
				result = new NetworkOperation(NetworkOperation.NETWORK_FAILURE);
			}
			response.close();
		} catch (IOException e) {
			Log.e(TAG, "HTTP \'GET\' OPERATION ERROR: " + e.getMessage());
			result = new NetworkOperation(NetworkOperation.IO_ERROR);
		}

		return result;
	}

	NetworkOperation post(String url, FormBody params) {
		Request request = new Request.Builder()
				                  .url(url)
				                  .cacheControl(CacheControl.FORCE_NETWORK) //TODO: ADDED BECAUSE
				                  // OF AVA LOGIN BUG, EVALUATE
				                  .post(params)
				                  .build();

		NetworkOperation result;
		try {
			Response response = mClient.newCall(request).execute();

			if (response.code() == 200) {
				result = new NetworkOperation(new String(response.body().bytes(), CHARSET),
						                             response.headers().toMultimap());
			} else {
				result = new NetworkOperation(NetworkOperation.NETWORK_FAILURE);
			}
			response.close();
		} catch (IOException e) {
			Log.e(TAG, "HTTP \'POST\' OPERATION ERROR: " + e.getMessage());
			result = new NetworkOperation(NetworkOperation.IO_ERROR);
		}

		return result;
	}

	List<Cookie> getCookieFromJar(String baseUrl) {
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
			return cookies != null ? cookies : new ArrayList<Cookie>();
		}
	}

}
