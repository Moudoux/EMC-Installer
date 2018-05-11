package me.deftware.emc.installer.utils;

import me.deftware.emc.installer.Main;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

	public static String getMinecraftRoot() {
		if (OSUtils.isWindows()) {
			return System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator;
		} else if (OSUtils.isLinux()) {
			return System.getProperty("user.home") + File.separator + ".minecraft" + File.separator;
		} else if (OSUtils.isMac()) {
			return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support"
					+ File.separator + "minecraft" + File.separator;
		}
		return "";
	}

	public static boolean extractAsset(String asset, File output) {
		try {
			if (output.exists()) {
				output.delete();
			}
			InputStream in = Main.class.getResourceAsStream(asset);
			OutputStream out = new FileOutputStream(output);
			IOUtils.copy(in, out);
			in.close();
			out.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
