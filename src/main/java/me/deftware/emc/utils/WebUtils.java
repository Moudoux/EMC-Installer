package me.deftware.emc.utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class WebUtils {

	public static void download(String uri, String fileName) throws Exception {
		URL url = new URL(uri);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		((URLConnection) connection).setRequestProperty("User-Agent", "EMC Installer");
		((HttpURLConnection) connection).setRequestMethod("GET");
		InputStream in = null;
		FileOutputStream out = new FileOutputStream(fileName);
		in = connection.getInputStream();
		int read = -1;
		byte[] buffer = new byte[4096];
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
		in.close();
		out.close();
	}

	public static String get(String url) throws Exception {
		URL url1 = new URL(url);
		Object connection = (url.startsWith("https://") ? (HttpsURLConnection) url1.openConnection()
				: (HttpURLConnection) url1.openConnection());
		((URLConnection) connection).setConnectTimeout(8 * 1000);
		((URLConnection) connection).setRequestProperty("User-Agent", "EMC Installer");
		((HttpURLConnection) connection).setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(((URLConnection) connection).getInputStream()));
		String result = "", text;
		while ((text = in.readLine()) != null) {
			result = result + text;
		}
		in.close();
		return result;
	}

}
