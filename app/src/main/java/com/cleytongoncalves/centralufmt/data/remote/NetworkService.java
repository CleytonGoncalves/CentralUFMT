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

	public static NetworkService.Builder builder() {
		return new Builder();
	}

	/**
	 * Generated through this class Builder
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

			String responseBody = new String(response.body().bytes(), CHARSET);
			if (response.isSuccessful()) {
				result = new NetworkOperation(responseBody, response.headers().toMultimap());
				Timber.d("Successful GET Operation on %s", url);
			} else {
				result = new NetworkOperation(NetworkOperation.NETWORK_ERROR);
				Timber.d("Failed GET Operation on %s - Network Error: %s", url, responseBody);
			}
			response.close();
		} catch (IOException e) {
			result = new NetworkOperation(NetworkOperation.IO_ERROR);
			Timber.i(e, "Failed GET operation on %s - I/O Error", url);
		}

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

			String responseBody = new String(response.body().bytes(), CHARSET);
			if (response.isSuccessful()) {
				result = new NetworkOperation(responseBody, response.headers().toMultimap());
				Timber.d("Successful POST Operation on %s", url);
			} else {
				result = new NetworkOperation(NetworkOperation.NETWORK_ERROR);
				Timber.d("Failed POST Operation on %s - Network Error: %s", url, responseBody);
			}
			response.close();
		} catch (IOException e) {
			result = new NetworkOperation(NetworkOperation.IO_ERROR);
			Timber.i(e, "Failed POST operation on %s - I/O Error", url);
		}

		return result;
	}

	public List<Cookie> getCookieFromJar(String baseUrl) {
		return mClient.cookieJar().loadForRequest(HttpUrl.parse(baseUrl));
	}


	/********
	 * Builder class that sets up a new network service
	 *******/
	public static class Builder {
		public NetworkService build() {
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
