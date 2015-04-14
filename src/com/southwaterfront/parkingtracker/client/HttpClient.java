package com.southwaterfront.parkingtracker.client;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.dialog.LoginDialogFragment;
import com.southwaterfront.parkingtracker.jsonify.Jsonify;
import com.southwaterfront.parkingtracker.util.LogUtils;
import com.southwaterfront.parkingtracker.util.Utils;

//create a request factory
//create a client definition that can send POST requests
//create a wrapper that sends POST requests to upload data to server

public class HttpClient {

	private static String LOG_TAG = "HttpClient";
	private static final File authTokenFile = AssetManager.getInstance().getAuthTokenFile();
	private static final String authTokenKeyName = "X-Auth-Token";
	private static final String TOKEN_GET_KEY = "Token";
	private static String authToken = readAuthFile(authTokenFile);

	private static final String POST_BLOCKFACE_DATA_URL = "https://bend.encs.vancouver.wsu.edu/~jason_moss/api/v1/blockfaces";
	private static GenericUrl postBlockFaceDataUrl;
	private static final String LOGIN_URL = "https://bend.encs.vancouver.wsu.edu/~jason_moss/api/v1/login";
	private static GenericUrl loginUrl;
	private static final String GET_STREET_MODEL_URL = "https://bend.encs.vancouver.wsu.edu/~jason_moss/api/v1/streetmodel";
	private static GenericUrl getStreetModelUrl;
	private static final NetHttpTransport transport = new NetHttpTransport();
	private static final HttpRequestFactory requestFactory = transport.createRequestFactory();
	private static final int UNAUTHORIZED_STATUS_CODE = 401;

	static {
		try {
			postBlockFaceDataUrl = new GenericUrl(new URL(POST_BLOCKFACE_DATA_URL));
		} catch (MalformedURLException e) {
		} 
		
		try {
			loginUrl = new GenericUrl(new URL(LOGIN_URL));
		} catch (MalformedURLException e) {
		} 
		
		try {
			getStreetModelUrl = new GenericUrl(new URL(GET_STREET_MODEL_URL));
		} catch (MalformedURLException e) {
		} 
	}
	
	//private static final Activity main = AssetManager.getInstance().getMainActivity();

	private static final String ERROR_LOGIN = "Failed to login";

	private static void initBlockingUILogin() throws RequestFailedException {
		final LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
		loginDialogFragment.show(AssetManager.getInstance().getMainActivity().getFragmentManager(), "Login");
		boolean loggedIn = false;
		try {
			loggedIn = loginDialogFragment.waitOnResult();
		} catch (InterruptedException e) {
		}
		if (!loggedIn)
			throw new RequestFailedException(ERROR_LOGIN);
	}

	public static InputStream getStreetModel() throws RequestFailedException, IOException {
		

		if (authToken == null)
			initBlockingUILogin();

		InputStream data = null;
		HttpRequest getRequest;
		HttpResponse response;
		getRequest = requestFactory.buildGetRequest(getStreetModelUrl);
		HttpHeaders headers = getRequest.getHeaders();
		headers.set(authTokenKeyName, authToken);
		getRequest.setHeaders(headers);
		response = getRequest.execute();	

		if (response.getStatusCode() == UNAUTHORIZED_STATUS_CODE)
			initBlockingUILogin();
		else if (!response.isSuccessStatusCode()) {
			int status = response.getStatusCode();
			String error = status + ": " + response.getStatusMessage();
			throw new RequestFailedException(error);
		}

		data = response.getContent();

		return data;

	}


	static public void postBlockFaceData(byte[] d) throws RequestFailedException, IOException {
		if (d == null)
			throw new IllegalArgumentException("Bytes to send cannot be null");

		if (authToken == null)
			initBlockingUILogin();

		ByteArrayContent data = new ByteArrayContent(null, d);
		HttpRequest postRequest;
		HttpResponse response = null;

		postRequest = requestFactory.buildPostRequest(postBlockFaceDataUrl, data);
		HttpHeaders headers = postRequest.getHeaders();
		headers.set(authTokenKeyName, authToken);
		postRequest.setHeaders(headers);
		try {
			response = postRequest.execute();	
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == UNAUTHORIZED_STATUS_CODE) {
				initBlockingUILogin();
				response = postRequest.execute();
			} else if (!e.isSuccessStatusCode()) {
				int status = e.getStatusCode();
				String error = status + ": " + e.getStatusMessage();
				throw new RequestFailedException(error);
			}
		}

		LogUtils.i(LOG_TAG, "The POST request response code is " + response.getStatusCode() + " with message " + response.getStatusMessage());

		if (response.getStatusCode() == UNAUTHORIZED_STATUS_CODE)
			initBlockingUILogin();
		else if (!response.isSuccessStatusCode()) {
			int status = response.getStatusCode();
			String error = status + ": " + response.getStatusMessage();
			throw new RequestFailedException(error);
		}
	}

	public static void postBlockFaceData(JsonObject obj) throws RequestFailedException, IOException {
		if (obj == null)
			throw new IllegalArgumentException("JsonObject cannot be null");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Jsonify.writeJsonObjectToStream(obj, out);
		HttpClient.postBlockFaceData(out.toByteArray());
	}

	/**
	 * Sends a login request to the server. Two things to be aware of. First, 
	 * this cannot be run on the UI thread. Second, if the login was correct the result
	 * is true, otherwise it is false. However, other errors may occur and these are
	 * denoted by a thrown {@link RequestFailedException}.
	 * 
	 * @param usernam Username
	 * @param password Password
	 * @return True is logged in, false if incorrect login credentials
	 * @throws RequestFailedException Thrown if there is an IO error or server error
	 * @throws IOException 
	 */
	public static boolean sendLoginRequest(String username, String password) throws RequestFailedException, IOException {
		if (username == null || password == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		JsonObject credentials = Json.createObjectBuilder().add("Username", username).add("Password", password).build();
		HttpRequest loginRequest;
		HttpResponse response;

		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		Jsonify.writeJsonObjectToStream(credentials, temp);
		ByteArrayContent out = new ByteArrayContent(null, temp.toByteArray()); 

		loginRequest = requestFactory.buildPostRequest(loginUrl, out);
		try {
			response = loginRequest.execute();
		}	catch (HttpResponseException e) {
			int status = e.getStatusCode();
			String error;
			if (status == UNAUTHORIZED_STATUS_CODE)
				return false;
			else
				error = status + ": " + e.getStatusMessage();
			throw new RequestFailedException(error);
		}

		if (!response.isSuccessStatusCode()){
			int status = response.getStatusCode();
			String error;
			if (status == UNAUTHORIZED_STATUS_CODE)
				return false;
			else
				error = status + ": " + response.getStatusMessage();
			throw new RequestFailedException(error);
		} else {
			JsonObject js;
			InputStream in = null;
			try {
				in = response.getContent();
				js = Jsonify.createJsonObjectFromStream(in);
				String token = js.getString(TOKEN_GET_KEY, null);
				if (token != null && token.length() > 0) {
					authToken = token;
					Utils.asyncFileDelete(authTokenFile);
					Utils.asyncFileWrite(authToken.getBytes(), authTokenFile);
				} else
					return false;

			} catch (Exception e) {
				throw new RequestFailedException("Failed to get content from server", e);
			} finally {
				try {
					if (in != null)
						in.close();
				} catch(Exception e) {}
			}
		}
		return true;
	}

	private static String readAuthFile(File authTokenFile){
		if (authTokenFile == null || !authTokenFile.exists())
			return null;
		RandomAccessFile in = null;
		try {
			in = new RandomAccessFile(authTokenFile, "r");
			byte[] temp = new byte[(int) in.length()];
			in.readFully(temp);
			String token = new String(temp);
			return token;
		} catch(Exception e) {
			return null;
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}

	public static class RequestFailedException extends Exception {

		public RequestFailedException(String string, Exception e) {
			super(string, e);
		}

		public RequestFailedException(String string) {
			super(string);
		}

		private static final long serialVersionUID = -4728904407478563082L;

	}
	
	public static boolean isLoggedIn() {
		return authToken != null;
	}

	public static void logout() {
		authToken = null;
		Utils.asyncFileDelete(authTokenFile);
	}
}
