package com.southwaterfront.parkingtracker.client;
import java.io.IOException;
import java.net.URL;

import android.util.Log;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;

//create a request factory
//create a client definition that can send POST requests
//create a wrapper that sends POST requests to upload data to server




public class client {
	private static String SERVER_URL = "http://parking.bitsrc.net/api/v1/blockfaces";	
	private static NetHttpTransport transport = new NetHttpTransport();
	private static HttpRequestFactory RequestFactory = transport.createRequestFactory();
	private static String LOG_TAG = "Client";
	
	
	static public void sendPostRequest(byte[] d) throws IOException{
		GenericUrl url = new GenericUrl(new URL(SERVER_URL)); 
		ByteArrayContent data = new ByteArrayContent(null, d);
		HttpRequest postRequest = RequestFactory.buildPostRequest(url, data);
		HttpResponse response = postRequest.execute();
		if (response.isSuccessStatusCode()){
			Log.i(LOG_TAG, response.getStatusMessage());
			return;
		} else {
			Log.e(LOG_TAG, response.getStatusMessage());
			throw new IOException();
		}		
	}
	
	
}
