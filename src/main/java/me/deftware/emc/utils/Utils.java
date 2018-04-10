package me.deftware.emc.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import me.deftware.emc.Installer.Main;

public class Utils {

	public static void log(String message) {
		System.out.println("Installer >> " + message);
	}

	public static void error(String message) {
		log(message);
		errorBox(message, "Install failed");
		System.exit(0);
	}

	public synchronized static void saveJson(String jsonContent, File to) {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(jsonContent);
			jsonContent = gson.toJson(je);
			PrintWriter writer = new PrintWriter(to, "UTF-8");
			writer.println(jsonContent);
			writer.close();
		} catch (Exception ex) {
			error("Failed to save json");
		}
	}

	public static synchronized String readFile(File file) {
		try {
			String output = "";
			for (String s : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
				output += s;
			}
			return output;
		} catch (Exception ex) {
			error("Failed to read file");
		}
		return null;
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

	public static String getMinecraftRoot() {
		if (OSUtils.isWindows()) {
			return System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator;
		} else if (OSUtils.isLinux()) {
			return System.getProperty("user.home") + File.separator + ".minecraft" + File.separator;
		} else if (OSUtils.isMac()) {
			return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support"
					+ File.separator + "minecraft" + File.separator;
		} else {
			error("Unsupported OS, please use Windows, macOS or Linux");
		}
		return "";
	}

	public static File getMinecraft(String mcVersion) {
		log("Locating Minecraft " + mcVersion + "...");
		File minecraft = null;
		if (OSUtils.isWindows()) {
			minecraft = new File(System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator + "versions"
					+ File.separator + mcVersion + File.separator + mcVersion + ".jar");
		} else if (OSUtils.isLinux()) {
			minecraft = new File(System.getProperty("user.home") + File.separator + ".minecraft" + File.separator
					+ "versions" + File.separator + mcVersion + File.separator + mcVersion + ".jar");
		} else if (OSUtils.isMac()) {
			minecraft = new File(System.getProperty("user.home") + File.separator + "Library" + File.separator
					+ "Application Support" + File.separator + "minecraft" + File.separator + "versions"
					+ File.separator + mcVersion + File.separator + mcVersion + ".jar");
		} else {
			error("Unsupported OS, please use Windows, macOS or Linux");
		}
		if (!minecraft.exists()) {
			error("Could not find Minecraft " + mcVersion + " in versions, is it installed ?");
		}
		return minecraft;
	}

	public static void copyFile(File one, File two) {
		try {
			Files.copy(one.toPath(), two.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			error("Failed to copy files");
		}
	}

	public static void infoBox(String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void errorBox(String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMessage, "Error: " + titleBar, JOptionPane.ERROR_MESSAGE);
	}

}
