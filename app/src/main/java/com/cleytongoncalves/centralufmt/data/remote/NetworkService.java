package com.cleytongoncalves.centralufmt.data.remote;

import android.content.Context;

import com.cleytongoncalves.centralufmt.R;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.cleytongoncalves.centralufmt.data.remote.NetworkOperation.NETWORK_IO_ERROR;

public final class NetworkService {
	public static final String CHARSET_ISO = "ISO-8859-1";
	public static final String CHARSET_UTF8 = "UTF-8";
	private final OkHttpClient mClient;
	
	public static NetworkService.Builder builder() {
		return new Builder();
	}
	
	/**
	 * Generated through NetworkService.Builder
	 */
	private NetworkService(OkHttpClient client) {
		this.mClient = client;
	}
	
	public NetworkOperation get(String url, String charset) {
		Request request = new Request.Builder()
				                  .url(url)
				                  .build();
		
		NetworkOperation result;
		try {
			Response response = mClient.newCall(request).execute();
			
			String responseBody = new String(response.body().bytes(), charset);
			if (response.isSuccessful()) {
				result = new NetworkOperation(responseBody, response.headers().toMultimap());
				Timber.d("Successful GET Operation on %s", url);
			} else {
				result = new NetworkOperation(response.code());
				Timber.w("Failed GET Operation on %s\nStatus Code: %d\nNetwork Error: %s", url,
				         response.code(), responseBody);
			}
			response.close();
		} catch (UnknownHostException e) {
			result = new NetworkOperation(NETWORK_IO_ERROR);
			Timber.i(e, "Failed GET operation on %s - Network Error (Probably not connected)",
			         url);
		} catch (IOException e) {
			result = new NetworkOperation(NETWORK_IO_ERROR);
			Timber.i(e, "Failed GET operation on %s - I/O Error", url);
		} catch (IllegalStateException e) {
			result = new NetworkOperation(NETWORK_IO_ERROR);
			Timber.w(e, "Failed GET operation on %s - IllegalState Error", url);
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
			
			String responseBody = new String(response.body().bytes(), CHARSET_ISO);
			if (response.isSuccessful()) {
				result = new NetworkOperation(responseBody, response.headers().toMultimap());
				Timber.d("Successful POST Operation on %s", url);
			} else {
				result = new NetworkOperation(response.code());
				Timber.w("Failed POST Operation on %s\nStatus Code: %d\nNetwork Error: %s", url,
				         response.code(), responseBody);
			}
			response.close();
		} catch (UnknownHostException e) {
			result = new NetworkOperation(NETWORK_IO_ERROR);
			Timber.i(e, "Failed POST operation on %s - Network Error",
			         url);
		} catch (IOException e) {
			result = new NetworkOperation(NETWORK_IO_ERROR);
			Timber.i(e, "Failed POST operation on %s - I/O Error", url);
		} catch (IllegalStateException e) {
			result = new NetworkOperation(NETWORK_IO_ERROR);
			Timber.w(e, "Failed POST operation on %s - IllegalState Error", url);
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
		public NetworkService build(@ApplicationContext Context context) {
			OkHttpClient.Builder clientBuilder = new OkHttpClient()
					                                     .newBuilder()
					                                     .cookieJar(new MyCookieJar())
					                                     .addNetworkInterceptor(
							                                     new StethoInterceptor())
					                                     .addNetworkInterceptor(chain -> chain.proceed(
							                      chain.request().newBuilder()
							                           .addHeader("Accept-Language", "pt-BR")
							                           .build()));
			
			clientBuilder = addAvaSslCert(clientBuilder, context);
			
			return new NetworkService(clientBuilder.build());
		}
		
		/* TEMPORARY WORKAROUND - Adds the AVA SSL Certificate as trusted within the application */
		private static OkHttpClient.Builder addAvaSslCert(OkHttpClient.Builder clientBuilder,
		                                                  Context context) {
			InputStream cert = null;
			try {
				// loading CAs from an InputStream
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				cert = context.getResources().openRawResource(R.raw.ava_certificate);
				Certificate ca = cf.generateCertificate(cert);
				
				// creating a KeyStore containing our trusted CAs
				String keyStoreType = KeyStore.getDefaultType();
				KeyStore keyStore = KeyStore.getInstance(keyStoreType);
				keyStore.load(null, null);
				keyStore.setCertificateEntry("ca", ca);
				
				// creating a TrustManager that trusts the CAs in our KeyStore
				String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
				tmf.init(keyStore);
				
				// creating an SSLSocketFactory that uses our TrustManager
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, tmf.getTrustManagers(), null);
				
				// Get hold of the default trust manager
				X509TrustManager x509Tm = null;
				for (TrustManager tm : tmf.getTrustManagers()) {
					if (tm instanceof X509TrustManager) {
						x509Tm = (X509TrustManager) tm;
						break;
					}
				}
				
				if (x509Tm == null) {
					//noinspection deprecation
					clientBuilder.sslSocketFactory(sslContext.getSocketFactory()); //Uses
					// reflection
				} else {
					clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), x509Tm);
				}
			} catch (Exception e) {
				Timber.wtf(e, "Failed to load AVA SSL Certificate.");
			}
			
			if (cert != null) {
				try {
					cert.close();
				} catch (IOException e) {
					Timber.i(e, "AVA SSL Certificate InputStream closure error.");
				}
			}
			
			return clientBuilder;
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
