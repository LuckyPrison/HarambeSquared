package com.intellectualcrafters.plot.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HastebinUtility {

	public static final String BIN_URL = "http://hastebin.com/documents";
	public static final String USER_AGENT = "Mozilla/5.0";
	public static final Pattern PATTERN = Pattern.compile("\\{\"key\":\"([\\S\\s]*)\"\\}");

	private HastebinUtility() {}

	public static String upload(String string) throws IOException
	{
		URL url = new URL(BIN_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setDoOutput(true);

		try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream()))
		{
			outputStream.write(string.getBytes());
			outputStream.flush();
		}

		StringBuilder response;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())))
		{
			response = new StringBuilder();

			String inputLine;
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
		}

		Matcher matcher = PATTERN.matcher(response.toString());
		if (matcher.matches())
		{
			return "http://hastebin.com/" + matcher.group(1);
		}
		else
		{
			throw new RuntimeException("Couldn't read response!");
		}
	}

	public static String upload(File file) throws IOException
	{
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			int i = 0;
			while ((line = reader.readLine()) != null && i++ < 1000)
			{
				content.append(line).append('\n');
			}
		}
		return upload(content.toString());
	}
}
