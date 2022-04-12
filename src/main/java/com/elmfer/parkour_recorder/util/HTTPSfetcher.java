package com.elmfer.parkour_recorder.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

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
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
				status = "Connected, Recieving Data";
				connection.setRequestMethod("GET");
				
				statusCode = connection.getResponseCode();
				status = String.format("Recieved Data: %d", statusCode);
				
				InputStream inputStream = connection.getInputStream();
				
				ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
				
				byte contentChunk[] = new byte[4098];
				int bytesRead = 0;
				
				while((bytesRead = inputStream.read(contentChunk)) > 0)
				{
					contentStream.write(contentChunk, 0, bytesRead);
				}
				
				parent.contentData = contentStream.toByteArray();
				
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
