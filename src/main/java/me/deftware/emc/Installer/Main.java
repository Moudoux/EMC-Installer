package me.deftware.emc.Installer;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import me.deftware.emc.ui.Progressbar;
import me.deftware.emc.ui.UI;
import me.deftware.emc.utils.WebUtils;

public class Main {

	public static final String versionsURL = "https://raw.githubusercontent.com/Moudoux/EMC/master/versions.json";
	public static JsonObject versionsJson;
	public static JsonObject emcJson;
	public static String name;

	public static void main(String[] args) {
		Progressbar pBar = new Progressbar("Loading installer...", 1);
		new Thread(() -> {
			try {
				String line;
				InputStream in = Main.class.getResourceAsStream("/EMC.json");
				StringBuilder result = new StringBuilder("");
				while ((line = new BufferedReader(new InputStreamReader(in)).readLine()) != null) {
					result.append(line);
				}
				in.close();
				emcJson = new Gson().fromJson(result.toString(), JsonObject.class);
				versionsJson = new Gson().fromJson(WebUtils.get(versionsURL), JsonObject.class);
				name = emcJson.get("name").getAsString();
				pBar.updateBar(1);
				Window w = SwingUtilities.getWindowAncestor(pBar);
				w.setVisible(false);
				new UI(versionsJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

}
