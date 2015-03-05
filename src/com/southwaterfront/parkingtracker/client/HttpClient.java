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
import javax.json.JsonObjectBuilder;

import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.jsonify.Jsonify;

//create a request factory
//create a client definition that can send POST requests
//create a wrapper that sends POST requests to upload data to server

public class HttpClient {
	private static final String authTokenName = "X-Auth-Token";
	private static final File authTokenFile = AssetManager.getInstance().getAuthToken();
	private static String LOG_TAG = "HttpClient";
	private static String authToken = readAuthFile(authTokenFile);

	private static final String SERVER_URL = "http://parking.bitsrc.net/api/v1/blockfaces";
	private static final String LOGIN_URL = "http://bend.encs.vancouver.wsu.edu/~jason_moss/api/v1/login";
	private static final NetHttpTransport transport = new NetHttpTransport();
	private static final HttpRequestFactory requestFactory = transport.createRequestFactory();


	static public void sendPostRequest(byte[] d) throws RequestFailedException {
		if (d == null)
			throw new IllegalArgumentException("Bytes to send cannot be null");

		GenericUrl url = null;
		try {
			url = new GenericUrl(new URL(SERVER_URL));
		} catch (MalformedURLException e) {
			// Not possible
		} 
		ByteArrayContent data = new ByteArrayContent(null, d);
		HttpRequest postRequest;
		HttpResponse response;
		try {
			postRequest = requestFactory.buildPostRequest(url, data);
			//postRequest.setHeaders();
			HttpHeaders headers = postRequest.getHeaders();
			headers.set(authTokenName, authToken);
			postRequest.setHeaders(headers);
			response = postRequest.execute();	
		} catch (IOException e) {
			Log.e(LOG_TAG, "The request could not execute Error message: " + e.getMessage(), e);
			throw new RequestFailedException("The request failed to execute", e);
		}

		Log.i(LOG_TAG, "The POST request response code is " + response.getStatusCode() + " with message " + response.getStatusMessage());

		if (response.getStatusCode() == 401){
			// send message to app that users auth token is not valid


		} else if (!response.isSuccessStatusCode())
			throw new RequestFailedException("The request was not successfull, the response code is " + response.getStatusCode());
	}

	public static void sendPostRequest(JsonObject obj) throws RequestFailedException {
		if (obj == null)
			throw new IllegalArgumentException("JsonObject cannot be null");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Jsonify.writeJsonObjectToStream(obj, out);
		HttpClient.sendPostRequest(out.toByteArray());
	}

	public static void SendLoginRequest(String username, String password) throws RequestFailedException{
		JsonObject credentials = Json.createObjectBuilder().add("Username", username).add("Password", password).build();
		GenericUrl url = null;
		HttpRequest loginRequest;
		HttpResponse response;
		try {
			url = new GenericUrl(new URL(LOGIN_URL));
		} catch (MalformedURLException e) {
			// Not possible
		} 
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		Jsonify.writeJsonObjectToStream(credentials, temp);
		ByteArrayContent out = new ByteArrayContent(null, temp.toByteArray()); 
		try {
			loginRequest = requestFactory.buildPostRequest(url, out);
			response = loginRequest.execute();
		} catch (IOException e) {
			Log.e(LOG_TAG, "The  login request could not execute Error message: " + e.getMessage(), e);
			throw new RequestFailedException("The request failed to execute", e);
		}
		if (!response.isSuccessStatusCode()){
			throw new RequestFailedException("Login/Password incorrect");
		} else {
			JsonObject js;
			try {
				InputStream in = response.getContent();
				js = Jsonify.createJsonObjectFromStream(in);
				String token = js.getString(authTokenName);
				// TODO this string needs to be put into the authTokenFile




			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static String readAuthFile(File authTokenFile){
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

		public RequestFailedException(String string, IOException e) {
			super(string, e);
		}

		public RequestFailedException(String string) {
			super(string);
		}

		public RequestFailedException() {
			super();
		}

		private static final long serialVersionUID = -4728904407478563082L;

	}

}
