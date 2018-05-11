package me.deftware.emc.installer.ui;

import com.google.gson.*;
import me.deftware.emc.installer.Main;
import me.deftware.emc.installer.utils.JSONGenerator;
import me.deftware.emc.installer.utils.Utils;
import me.deftware.emc.installer.utils.WebUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;

public class InstallerUI {

	private JButton installButton;
	private JButton cancelButton;
	private JComboBox comboBox1;
	private JRadioButton withOptiFineRadioButton;
	private JRadioButton forForgeRadioButton;
	private JPanel mainPanel;
	private JsonObject json, internalJson;
	private String clientName;

	public InstallerUI(JsonObject json, JFrame frame) {
		try {
			String line;
			InputStream in = Main.class.getResourceAsStream("/EMC.json");
			StringBuilder result = new StringBuilder("");
			while ((line = new BufferedReader(new InputStreamReader(in)).readLine()) != null) {
				result.append(line);
			}
			in.close();
			internalJson = new Gson().fromJson(result.toString(), JsonObject.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		clientName = internalJson.get("name").getAsString();
		frame.setTitle(clientName + " installer");
		this.json = json;
		ActionListener listener = ((e) -> {
			withOptiFineRadioButton.setSelected(!e.getActionCommand().equals("forge"));
			forForgeRadioButton.setSelected(e.getActionCommand().equals("forge"));
		});
		withOptiFineRadioButton.addActionListener(listener);
		forForgeRadioButton.addActionListener(listener);
		forForgeRadioButton.setActionCommand("forge");
		withOptiFineRadioButton.setActionCommand("opti");
		installButton.addActionListener((e) -> install());
		cancelButton.addActionListener((e) -> System.exit(0));
		json.entrySet().forEach((entry) -> comboBox1.addItem(entry.getKey()));
	}

	public static JFrame create(JsonObject json) {
		JFrame frame = new JFrame("");
		InstallerUI ui = new InstallerUI(json, frame);
		JPanel panel = ui.mainPanel;
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setMinimumSize(new Dimension(400, 0));
		frame.setResizable(false);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
		return frame;
	}

	private void install() {
		String mcVersion = comboBox1.getSelectedItem().toString();
		String[] jsonValue = json.get(mcVersion).getAsString().split(",");
		String optifineVersion = jsonValue[1], emcVersion = jsonValue[0];
		DialogUI dialog = new DialogUI("Installing " + clientName + ", please wait...","",false, () -> {
			new DialogUI("Install complete, the installer will now exit", "Done", true, () -> System.exit(0)).setVisible(true);
		});
		new Thread(() -> {
			try {
				new Thread(() -> dialog.setVisible(true)).start();
				// Download required libs
				if (withOptiFineRadioButton.isSelected()) {
					// Download and install OptiFine
					File optifineDir = new File(Utils.getMinecraftRoot() + "versions" +
							File.separator + mcVersion + "-" + optifineVersion.replace("_" + mcVersion, "").replace(".jar", ""));
					if (!optifineDir.exists()) {
						File optifine = new File(Utils.getMinecraftRoot() + "optifine_installer.jar");
						String website = WebUtils.get("https://optifine.net/adloadx?f=" + optifineVersion);
						website = "https://optifine.net/downloadx" + website.split("downloadx")[1].split("'")[0];
						WebUtils.download(website, optifine.getAbsolutePath());
						System.out.println(
								IOUtils.toString(new ProcessBuilder("java", "-cp", optifine.getAbsolutePath(), "optifine.Installer")
										.start().getInputStream()));
						optifine.delete();
					}
				} else if (forForgeRadioButton.isSelected()) {
					// Download and install EMC
					String link = "https://github.com/Moudoux/EMC/raw/master/maven/me/deftware/EMC-Forge/"
							+ emcVersion + "/EMC-Forge-" + emcVersion + "-full.jar";
					new File(Utils.getMinecraftRoot() + "mods" + File.separator + mcVersion + File.separator).mkdirs();
					WebUtils.download(link, Utils.getMinecraftRoot() + "mods" + File.separator + mcVersion
							+ File.separator + "EMC.jar");
				}
				// Make JSON
				if (!forForgeRadioButton.isSelected()) {
					// Client json
					JsonObject json = JSONGenerator.generateClientJSON(clientName, mcVersion, emcVersion, withOptiFineRadioButton.isSelected() ? optifineVersion : "");
					File clientDir = new File(Utils.getMinecraftRoot() + "versions" + File.separator + mcVersion + "-" + clientName + File.separator);
					clientDir.mkdirs();
					try (Writer writer = new FileWriter(new File(clientDir.getAbsolutePath() + File.separator +  mcVersion + "-" + clientName + ".json"))) {
						new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
					}
					// Install launcher profile
					File profiles_json = new File(Utils.getMinecraftRoot()  + "launcher_profiles.json");
					JsonObject launcherJson = new JsonParser().parse(Files.newBufferedReader(profiles_json.toPath())).getAsJsonObject();
					JsonObject profiles = launcherJson.get("profiles").getAsJsonObject();
					if (!profiles.has(mcVersion + "-" + clientName)) {
						profiles.add(mcVersion + "-" + clientName, JSONGenerator.generateLaunchProfile(clientName, mcVersion));
					}
					launcherJson.addProperty("selectedProfile", mcVersion + "-" + clientName);
					try (Writer writer = new FileWriter(profiles_json)) {
						new GsonBuilder().setPrettyPrinting().create().toJson(launcherJson, writer);
					}
				}
				// Install bundled EMC mods
				File EMC_LIBS = new File(
						Utils.getMinecraftRoot() + "libraries" + File.separator + "EMC" + File.separator);
				EMC_LIBS.mkdirs();
				internalJson.get("mods").getAsJsonArray().forEach((mod) -> {
					String name = mod.getAsString();
					File m = new File(EMC_LIBS.getAbsolutePath() + File.separator + name);
					if (m.exists()) {
						m.delete();
					}
					Utils.extractAsset("/assets/" + name, m);
				});
				dialog.onContinue();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

}
