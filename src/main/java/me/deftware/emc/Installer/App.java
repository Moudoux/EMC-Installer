package me.deftware.emc.Installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.deftware.emc.Installer.Patch.JBDiff;
import me.deftware.emc.Installer.Patch.JBPatch;

public class App {
	
	public static String name = "", mcVersion = "", clientName = "", targetPatch = "";

	public static void log(String message) {
		System.out.println("Installer >> " + message);
	}

	public static void error(String message) {
		log(message);
		errorBox(message, "Install failed");
		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			InputStream in = App.class.getResourceAsStream("/EMC.json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder result = new StringBuilder("");

			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
			in.close();

			JsonObject jsonObject = new Gson().fromJson(result.toString(), JsonObject.class);

			name = jsonObject.get("name").getAsString();
			mcVersion = jsonObject.get("mc_vesion").getAsString();
			clientName = jsonObject.get("name").getAsString() + "_" + mcVersion;
			targetPatch = jsonObject.get("patch").getAsString();

			if (mcVersion.equals("") || clientName.equals("") || targetPatch.equals("") || name.equals("")) {
				throw new Exception("Invalid json values");
			}

			log(jsonObject.get("name").getAsString() + " installer for Minecraft " + mcVersion);
		} catch (Exception ex) {
			error("Failed to read installer config");
		}
		if (args.length != 0) {
			if (args[0].equals("-g") || args[0].equals("--gen")) {
				if (args.length != 4) {
					error("Invalid syntax, please use \"--gen <Source> <Modified File> <Output>\"");
				}
				File original = new File(args[1]);
				File modified = new File(args[2]);
				File output = new File(args[3]);
				if (!original.exists() || !modified.exists()) {
					error("Could not find files");
				}
				log("Generating patch file...");
				App.genPatch(original, modified, output);
			} else if (args[0].equals("-a") || args[0].equals("--apply")) {
				if (args.length != 4) {
					error("Invalid syntax, please use \"--apply <Source> <Patch file> <Output>\"");
				}
				File original = new File(args[1]);
				File patchfile = new File(args[2]);
				File output = new File(args[3]);
				if (!original.exists() || !patchfile.exists()) {
					error("Could not find files");
				}
				log("Applying patch file...");
				App.applyPatch(original, patchfile, output);
			} else {
				error("Unknown action \"" + args[0] + "\"");
			}
		} else {
			log("Installing EMC...");
			new Thread() {
				@Override
				public void run() {
					infoBox("Installing " + name + "...", "Installing...");
				}
			}.start();
			install();
		}
	}

	/**
	 * Installs EMC
	 */
	public static void install() {
		File minecraft = getMinecraft();
		// Copy Minecraft jar
		File clientDir = new File(
				minecraft.getParent().replace(File.separatorChar + mcVersion, File.separator + clientName));
		// Delete existing EMC install
		if (clientDir.exists()) {
			clientDir.delete();
		}
		clientDir.mkdir();
		File clientFile = new File(clientDir.getAbsolutePath() + File.separator + mcVersion + ".jar");
		App.copyFile(minecraft, clientFile);
		// Apply patch
		File pFile = new File(clientDir.getAbsolutePath() + File.separator + "emc.patch");
		log("Downloading patch...");
		try {
			getPatch(pFile);
		} catch (Throwable t) {
			error("Could not get patch. " + t.getMessage());
		}
		log("Patching...");
		App.applyPatch(clientFile, pFile, new File(clientDir.getAbsolutePath() + File.separator + clientName + ".jar"));
		// Delete files
		clientFile.delete();
		pFile.delete();
		// Copy json
		log("Copying json...");
		File json = new File(minecraft.getParent() + File.separator + mcVersion + ".json");
		if (!json.exists()) {
			error("Could not find \"" + mcVersion + ".json\"");
		}
		clientFile = new File(clientDir.getAbsolutePath() + File.separator + clientName + ".json");
		App.copyFile(json, clientFile);
		// Edit json
		JsonObject jsonObject = new Gson().fromJson(readFile(clientFile), JsonObject.class);
		if (!jsonObject.has("id")) {
			error("Invalid json file");
		}
		// Update id
		jsonObject.remove("id");
		jsonObject.add("id", new JsonPrimitive(clientName));
		// Remove things
		jsonObject.remove("logging");
		jsonObject.remove("downloads");
		// Save
		saveJson(jsonObject.toString(), clientFile);
		// Optional: Install Client.jar
		clientFile = new File(clientDir.getAbsolutePath() + File.separator + "Client.jar");
		log("Extracting assets....");
		App.extractAsset("/assets/Client.jar", clientFile);
		log("Done");
		infoBox(name + " was successfully installed, open your Minecraft launcher and select \"release " + clientName
				+ "\"", "Installation done");
		System.exit(0);
	}

	/*
	 * Tools
	 */

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
			InputStream in = App.class.getResourceAsStream(asset);
			OutputStream out = new FileOutputStream(output);
			IOUtils.copy(in, out);
			in.close();
			out.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	public static void getPatch(File output) throws IOException {
		URL url = new URL("https://github.com/Moudoux/EMC-Installer/blob/master/Patches/" + targetPatch + "?raw=true");
		FileUtils.copyURLToFile(url, output);
	}

	public static File getMinecraft() {
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
					+ File.separator + mcVersion
					+ File.separator + mcVersion + ".jar");
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
		JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void errorBox(String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.ERROR_MESSAGE);
	}

	/*
	 * Patching/Gen
	 */
	
	public static void genPatch(File minecraft, File modifiedMinecraft, File output) {
		try {
			JBDiff.bsdiff(minecraft, modifiedMinecraft, output);
			log("Done, file saved to " + output.getAbsolutePath());
		} catch (Exception ex) {
			log("Failed to generate patch file");
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public static void applyPatch(File minecraft, File patchFile, File output) {
		try {
			JBPatch.bspatch(minecraft, output, patchFile);
		} catch (Exception ex) {
			log("Failed to apply patch file");
			ex.printStackTrace();
			System.exit(0);
		}
	}

}
