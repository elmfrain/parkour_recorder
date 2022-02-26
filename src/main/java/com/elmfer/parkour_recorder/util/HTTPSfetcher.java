package com.elmfer.parkour_recorder.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HTTPSfetcher
{
	private FetcherWorker worker;
	private byte[] contentData = null;
	private String stringData = null;
	
	public HTTPSfetcher(String url)
	{
		try
		{
			worker = new FetcherWorker(this, new URL(url));
			worker.start();
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean fetchComplete()
	{
		return worker.complete;
	}
	
	public String stringContent()
	{
		if(worker.complete)
		{
			if(stringData == null)
				stringData = new String(contentData, StandardCharsets.UTF_8);
			return stringData;
		}
		
		return worker.status;
	}
	
	public byte[] byteContent()
	{
		return contentData;
	}
	
	public boolean hasFailed()
	{
		return worker.statusCode != HttpURLConnection.HTTP_OK;
	}
	
	public int statusCode()
	{
		return worker.statusCode;
	}
	
	/** Internal fetcher worker. **/
	public static class FetcherWorker extends Thread
	{
		URL url;
		boolean complete = false;
		int statusCode = 200;
		String status = "Connecting";
		HTTPSfetcher parent;
		
		FetcherWorker(HTTPSfetcher parent, URL url)
		{
			this.url = url;
			this.parent = parent;
		}
		
		@Override
		public void run()
		{
			try
			{
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				status = "Connected, Recieving Data";
				connection.setRequestMethod("GET");
				
				BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
				statusCode = connection.getResponseCode();
				status = String.format("Recieved Data: %d", statusCode);
				
				parent.contentData = new byte[is.available()];
				is.read(parent.contentData);
				
				is.close();
				connection.disconnect();
				
				complete = true;
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				statusCode = 0;
				status = String.format("Fetch Failed: %s", e.toString());
			}
		}
	}
}
