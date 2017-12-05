package me.deftware.emc.Installer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class WebUtils {

	public static String get(String url) throws InstallException {
		try {
			URL url1 = new URL(url);
			Object connection = (url.startsWith("https://") ? (HttpsURLConnection) url1.openConnection()
					: (HttpURLConnection) url1.openConnection());
			((URLConnection) connection).setConnectTimeout(8 * 1000);
			((URLConnection) connection).setRequestProperty("User-Agent", "EMC Installer");

			((HttpURLConnection) connection).setRequestMethod("GET");
			BufferedReader in = new BufferedReader(
					new InputStreamReader(((URLConnection) connection).getInputStream()));
			String text;
			String result = "";
			while ((text = in.readLine()) != null) {
				result = result + text;
			}
			in.close();
			return result;
		} catch (Exception e) {
			return "null";
		}
	}

}
