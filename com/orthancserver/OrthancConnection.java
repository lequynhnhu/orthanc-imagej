/**
 * Orthanc - A Lightweight, RESTful DICOM Store
 * Copyright (C) 2012-2014 Medical Physics Department, CHU of Liege,
 * Belgium
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/


package com.orthancserver;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class OrthancConnection
{
	private String baseUrl_;
	private String authentication_;
  private String name_;
	
	OrthancConnection()
	{
		baseUrl_ = "http://localhost:8042/";
		authentication_ = null;
    name_ = "Default Orthanc server";
	}


  private static InputStream OpenUrl(String urlString) throws IOException
	{
		URL url = new URL(urlString);

    try
    {
      return url.openStream();
    }
    catch (ConnectException e)
    {
      throw new IOException();
    }
	}

	
	private static InputStream OpenUrl(String urlString,
																		 String authentication) throws IOException
	{
    try
    {
      URL url = new URL(urlString);
      // http://blogs.deepal.org/2008/01/sending-basic-authentication-using-url.html
      URLConnection uc = url.openConnection();
      uc.setRequestProperty("Authorization", "Basic " + authentication);
      return uc.getInputStream();
    }
    catch (ConnectException e)
    {
      throw new IOException();
    }
	}


  private static String ComputeAuthentication(String username,
                                              String password)
  {
		String auth = (new StringBuffer(username).append(":").append(password)).toString();
		// http://stackoverflow.com/a/14413290/881731
		return DatatypeConverter.printBase64Binary(auth.getBytes());
  }

  
  public InputStream OpenStream(String uri) throws IOException
  {
    String url = baseUrl_ + uri;
    return authentication_ == null ? OpenUrl(url) : OpenUrl(url, authentication_);
  }


	public void SetCredentials(String username,
                             String password)
	{
		authentication_ = ComputeAuthentication(username, password);
	}

  public void SetBaseUrl(String url)
  {
    if (url.endsWith("/"))
    {
      baseUrl_ = url;
    }
    else
    {
      baseUrl_ = url + "/";
    }
  }

  public String GetBaseUrl()
  {
    return baseUrl_;
  }

  public String ReadString(String uri) throws IOException
  {
    InputStream stream = OpenStream(uri);

    BufferedReader reader = null;
    try 
		{
			reader = new BufferedReader(new InputStreamReader(stream));

      StringBuffer buffer = new StringBuffer();
      int read;
      char[] chars = new char[1024 * 16];
      while ((read = reader.read(chars)) != -1)
      {
        buffer.append(chars, 0, read); 
      }

      return buffer.toString();
    }
		finally 
		{
			try 
			{
				if (reader != null)
        {
					reader.close();
        }
			}
			catch (IOException e)
			{
			}
    }
  }

  public Object ReadJson(String uri) throws IOException
  {
    String content = ReadString(uri);
    Object json = JSONValue.parse(content);

    if (json == null)
    {
      throw new IOException();
    }
    else
    {
      return json;
    }
  }

  public BufferedImage ReadImage(String uri) throws IOException
  {
    return ImageIO.read(OpenStream(uri));
  }

  public String GetName()
  {
    return name_;
  }

  public void SetName(String name)
  {
    name_ = name;
  }

  public JSONObject Serialize()
  {
    JSONObject json = new JSONObject();
    json.put("Name", name_);
    json.put("Url", baseUrl_);

    if (authentication_ != null)
    {
      json.put("Authentication", authentication_);
    }

    return json;
  }

  public static OrthancConnection Unserialize(JSONObject json)
  {
    OrthancConnection c = new OrthancConnection();
    c.SetName((String) json.get("Name"));
    c.SetBaseUrl((String) json.get("Url"));

    if (json.containsKey("Authentication"))
    {
      c.authentication_ = (String) json.get("Authentication");
    }
    else
    {
      c.authentication_ = null;
    }

    return c;
  }
}
