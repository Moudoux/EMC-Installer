package me.deftware.emc.Installer;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import me.deftware.emc.ui.Installer;
import me.deftware.emc.ui.Progressbar;

public class Main {

	public static final String versionsURL = "https://raw.githubusercontent.com/Moudoux/EMC-Installer/master/versions.json";
	public static JsonObject versionsJson;
	public static String name;

	public static void main(String[] args) {
		if (args.length != 0) {
			if (args[0].equals("-g") || args[0].equals("--gen")) {
				if (args.length != 4) {
					Utils.error("Invalid syntax, please use \"--gen <Source> <Modified File> <Output>\"");
				}
				File original = new File(args[1]);
				File modified = new File(args[2]);
				File output = new File(args[3]);
				if (!original.exists() || !modified.exists()) {
					Utils.error("Could not find files");
				}
				Utils.log("Generating patch file...");
				Utils.genPatch(original, modified, output);
			} else if (args[0].equals("-a") || args[0].equals("--apply")) {
				if (args.length != 4) {
					Utils.error("Invalid syntax, please use \"--apply <Source> <Patch file> <Output>\"");
				}
				File original = new File(args[1]);
				File patchfile = new File(args[2]);
				File output = new File(args[3]);
				if (!original.exists() || !patchfile.exists()) {
					Utils.error("Could not find files");
				}
				Utils.log("Applying patch file...");
				Utils.applyPatch(original, patchfile, output);
			} else {
				Utils.error("Unknown action \"" + args[0] + "\"");
			}
		} else {
			try {
				InputStream in = Main.class.getResourceAsStream("/EMC.json");
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				StringBuilder result = new StringBuilder("");
				String line;
				while ((line = reader.readLine()) != null) {
					result.append(line);
				}
				in.close();
				JsonObject jsonObject = new Gson().fromJson(result.toString(), JsonObject.class);
				name = jsonObject.get("name").getAsString();
			} catch (Exception ex) {
				Utils.errorBox("Failed to read installer config", "Corrupt installer");
				return;
			}
			// Display progressbar while downloading all files required
			Progressbar pBar = new Progressbar("Loading installer...", 1);
			new Thread(() -> {
				try {
					String json = WebUtils.get(versionsURL);
					if (json.equals("null") || json.isEmpty()) {
						throw new InstallException("Could not fetch Minecraft versions");
					}
					versionsJson = new Gson().fromJson(json, JsonObject.class);
					pBar.updateBar(1);
					Window w = SwingUtilities.getWindowAncestor(pBar);
					w.setVisible(false);
					Installer installer = new Installer(versionsJson);
				} catch (InstallException ex) {
					Utils.errorBox("Failed to load installer: " + ex.getMessage(), "Install failed");
					ex.printStackTrace();
				}
			}).start();
		}
	}

}
