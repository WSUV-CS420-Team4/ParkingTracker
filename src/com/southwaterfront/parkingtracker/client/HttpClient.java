package com.southwaterfront.parkingtracker.client;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.json.JsonObject;

import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.southwaterfront.parkingtracker.jsonify.Jsonify;

//create a request factory
//create a client definition that can send POST requests
//create a wrapper that sends POST requests to upload data to server




public class HttpClient {
	private static String LOG_TAG = "HttpClient";
	
	private static final String SERVER_URL = "http://parking.bitsrc.net/api/v1/blockfaces";	
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
			response = postRequest.execute();
		} catch (IOException e) {
			Log.e(LOG_TAG, "The request could not execute Error message: " + e.getMessage(), e);
			throw new RequestFailedException("The request failed to execute", e);
		}
		
		Log.i(LOG_TAG, "The POST request response code is " + response.getStatusCode() + " with message " + response.getStatusMessage());
		
		if (!response.isSuccessStatusCode())
			throw new RequestFailedException("The request was not successfull, the response code is " + response.getStatusCode());
	}
	
	public static void sendPostRequest(JsonObject obj) throws RequestFailedException {
		if (obj == null)
			throw new IllegalArgumentException("JsonObject cannot be null");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Jsonify.writeJsonObjectToStream(obj, out);
		HttpClient.sendPostRequest(out.toByteArray());
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
