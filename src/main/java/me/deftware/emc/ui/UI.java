package me.deftware.emc.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.deftware.emc.Installer.Main;
import me.deftware.emc.optifine.OptifineInstaller;
import me.deftware.emc.utils.Utils;
import me.deftware.emc.utils.WebUtils;

public class UI extends JFrame {

	private Set<Map.Entry<String, JsonElement>> versions;
	private JComboBox combobox;
	private JRadioButton forgeButton, optifineButton;
	private String v;

	public UI(JsonObject json) {
		this.versions = json.entrySet();
		System.out.println(versions);
		String low = "", high = "";
		for (Map.Entry<String, JsonElement> entry : versions) {
			if (high.isEmpty()) {
				high = entry.getKey();
			} else {
				low = entry.getKey();
			}
		}
		v = (low.isEmpty() ? high : low + "-" + high);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setTitle(Main.name + " Installer");
		this.initGui();
	}

	private void install() {
		Progressbar pBar = new Progressbar("Installing " + Main.name + ", please wait...", 6);
		new Thread(() -> {
			try {
				String data = "";
				for (Map.Entry<String, JsonElement> entry : versions) {
					if (entry.getKey().equals(combobox.getSelectedItem().toString())) {
						data = entry.getValue().getAsString();
						break;
					}
				}
				String tweaker = "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userType ${user_type} --tweakClass me.deftware.launch.Launcher --tweakClass optifine.OptiFineForgeTweaker";
				String mcVersion = combobox.getSelectedItem().toString();
				String emcVersionMaven = "me.deftware:EMC:" + data.split(",")[0];
				String optifineMaven = "optifine:OptiFine:"
						+ data.split(",")[1].split("OptiFine_")[1].replace(".jar", "");
				String clientName = mcVersion + "-" + Main.name;
				File minecraft = Utils.getMinecraft(mcVersion);
				File clientDir = new File(
						minecraft.getParent().replace(File.separatorChar + mcVersion, File.separator + clientName));
				pBar.updateBar(1);

				// Install Optifine or Forge
				if (optifineButton.isSelected()) {
					OptifineInstaller.install(data.split(",")[1]);
					pBar.updateBar(2);

					// Delete old installation
					if (clientDir.exists()) {
						clientDir.delete();
					}
					clientDir.mkdir();
					pBar.updateBar(3);

					File clientJson = new File(clientDir.getAbsolutePath() + File.separator + clientName + ".json");
					JsonObject jsonObject = new JsonObject();

					jsonObject.add("inheritsFrom", new JsonPrimitive(mcVersion));
					jsonObject.add("id", new JsonPrimitive(clientName));
					jsonObject.add("time", new JsonPrimitive("2017-09-18T03:41:11-05:00"));
					jsonObject.add("releaseTime", new JsonPrimitive("2017-09-18T03:39:46-05:00"));
					jsonObject.add("type", new JsonPrimitive("release"));
					jsonObject.add("minecraftArguments", new JsonPrimitive(tweaker));
					jsonObject.add("mainClass", new JsonPrimitive("net.minecraft.launchwrapper.Launch"));
					jsonObject.add("minimumLauncherVersion", new JsonPrimitive("0"));
					jsonObject.add("jar", new JsonPrimitive(mcVersion));
					jsonObject.add("downloads", new JsonObject());
					pBar.updateBar(4);

					// Libraries
					JsonArray libsArray = new JsonArray();
					libsArray.add(genArrayObject("name", "net.minecraft:launchwrapper:1.12", "", ""));
					libsArray.add(genArrayObject("name", emcVersionMaven, "url",
							"https://github.com/Moudoux/EMC/raw/master/maven/"));
					libsArray.add(genArrayObject("name", optifineMaven, "", ""));
					libsArray.add(genArrayObject("name", "org.spongepowered:mixin:0.7.1-SNAPSHOT", "url",
							"http://dl.liteloader.com/versions/"));
					libsArray.add(genArrayObject("name", "net.jodah:typetools:0.5.0", "url",
							"https://repo.maven.apache.org/maven2/"));
					jsonObject.add("libraries", libsArray);
					Utils.saveJson(jsonObject.toString(), clientJson);
					pBar.updateBar(5);
				} else {
					String link = "https://github.com/Moudoux/EMC/raw/master/maven/me/deftware/EMC-Forge/"
							+ data.split(",")[0] + "/EMC-Forge-" + data.split(",")[0] + "-full.jar";
					WebUtils.download(link, Utils.getMinecraftRoot() + "mods" + File.separator + mcVersion
							+ File.separator + "EMC.jar");
					pBar.updateBar(5);
				}

				// Install mods
				File EMC_LIBS = new File(
						Utils.getMinecraftRoot() + "libraries" + File.separator + "EMC" + File.separator);
				if (!EMC_LIBS.exists()) {
					EMC_LIBS.mkdir();
				}
				for (JsonElement mod : Main.emcJson.get("mods").getAsJsonArray()) {
					String name = mod.getAsString();
					File m = new File(
							Utils.getMinecraftRoot() + "libraries" + File.separator + "EMC" + File.separator + name);
					if (m.exists()) {
						m.delete();
					}
					Utils.extractAsset("/assets/" + name, m);
				}

				// Done
				pBar.updateBar(6);
				Window w = SwingUtilities.getWindowAncestor(pBar);
				w.setVisible(false);

				if (optifineButton.isSelected()) {
					Utils.infoBox(Main.name
							+ " was successfully installed, open your Minecraft launcher and select \"release "
							+ clientName + "\"", "Installation done");
				} else {
					Utils.infoBox(Main.name
							+ " was successfully installed with Forge, open your Minecraft launcher and start forge",
							"Installation done");
				}
			} catch (Exception ex) {
				Utils.errorBox("Failed to install, " + ex.getMessage(), "Install failed");
				ex.printStackTrace();
			}
			System.exit(0);
		}).start();
	}

	private JsonObject genArrayObject(String name, String n, String url, String u) {
		JsonObject obj = new JsonObject();
		obj.addProperty(name, n);
		if (!url.equals("")) {
			obj.addProperty(url, u);
		}
		return obj;
	}

	private void initGui() {
		this.setResizable(true);
		this.setBounds(100, 100, 495, 360);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
		this.setDefaultCloseOperation(3);
		final JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(16, 16, 16, 16));
		this.getContentPane().add(panel, "Center");
		final GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[2];
		gbl_panel.rowHeights = new int[7];
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 };
		panel.setLayout(gbl_panel);
		final JLabel lblTitle = new JLabel(Main.name + " Installer");
		lblTitle.setFont(new Font("Segoe UI Light", 0, 32));
		final GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.insets = new Insets(0, 0, 5, 0);
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 0;
		panel.add(lblTitle, gbc_lblTitle);
		final JLabel lblSubtitle = new JLabel("for Minecraft " + v);
		lblSubtitle.setFont(new Font("Segoe UI Light", 0, 16));
		final GridBagConstraints gbc_lblSubtitle = new GridBagConstraints();
		gbc_lblSubtitle.insets = new Insets(0, 0, 20, 0);
		gbc_lblSubtitle.gridx = 0;
		gbc_lblSubtitle.gridy = 1;
		panel.add(lblSubtitle, gbc_lblSubtitle);
		final JLabel lblDescription = new JLabel("<html><center>This installer will install " + Main.name
				+ ". Please select the Minecraft version you want to install " + Main.name
				+ " for below and click \"Install\".");
		lblDescription.setFont(new Font("Segoe UI", 0, 12));
		combobox = new JComboBox();
		combobox.setBounds(112, 115, 145, 20);
		ArrayList<String> versions = new ArrayList<String>();
		for (Map.Entry<String, JsonElement> entry : this.versions) {
			combobox.addItem(entry.getKey());
		}
		final GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.fill = 2;
		gbc_lblDescription.anchor = 11;
		gbc_lblDescription.insets = new Insets(0, 0, 20, 0);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		panel.add(lblDescription, gbc_lblDescription);
		final JPanel panel_1 = new JPanel();
		final GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 20, 0);
		gbc_panel_1.fill = 1;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		panel.add(panel_1, gbc_panel_1);
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[4];
		gbl_panel_1.rowHeights = new int[2];
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);
		final JLabel lblminecraftFolder = new JLabel("Minecraft Version: ");
		final GridBagConstraints gbc_lblminecraftFolder = new GridBagConstraints();
		gbc_lblminecraftFolder.insets = new Insets(0, 0, 0, 5);
		gbc_lblminecraftFolder.anchor = 13;
		gbc_lblminecraftFolder.gridx = 0;
		gbc_lblminecraftFolder.gridy = 0;
		panel_1.add(lblminecraftFolder, gbc_lblminecraftFolder);
		final GridBagConstraints gbc_txtPath = new GridBagConstraints();
		gbc_txtPath.insets = new Insets(0, 0, 0, 5);
		gbc_txtPath.fill = 2;
		gbc_txtPath.gridx = 1;
		gbc_txtPath.gridy = 0;
		panel_1.add(combobox, gbc_txtPath);

		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = 2;
		gbc_panel_3.anchor = 20;
		gbc_panel_3.insets = new Insets(0, 0, 20, 0);
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 4;
		panel.add(panel_3, gbc_panel_3);

		optifineButton = new JRadioButton("with OptiFine");
		optifineButton.setSelected(true);
		panel_3.add(optifineButton);

		forgeButton = new JRadioButton("with Forge");
		panel_3.add(forgeButton);
		this.setVisible(true);

		final JPanel panel_2 = new JPanel();
		final GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridheight = 6;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.anchor = 15;
		gbc_panel_2.fill = 2;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 5;
		panel.add(panel_2, gbc_panel_2);
		final GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[5];
		gbl_panel_2.rowHeights = new int[2];
		gbl_panel_2.columnWeights = new double[] { 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);
		final JButton btnInstall = new JButton("Install");
		btnInstall.addActionListener(e -> this.install());
		btnInstall.setMargin(new Insets(4, 32, 4, 32));
		final GridBagConstraints gbc_btnInstall = new GridBagConstraints();
		gbc_btnInstall.insets = new Insets(0, 0, 0, 20);
		gbc_btnInstall.gridx = 1;
		gbc_btnInstall.gridy = 0;
		panel_2.add(btnInstall, gbc_btnInstall);
		final JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> System.exit(0));
		btnCancel.setMargin(new Insets(4, 32, 4, 32));
		final GridBagConstraints gbc_btnExtract = new GridBagConstraints();
		gbc_btnExtract.insets = new Insets(0, 0, 0, 5);
		gbc_btnExtract.gridx = 2;
		gbc_btnExtract.gridy = 0;
		panel_2.add(btnCancel, gbc_btnExtract);

		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("forge")) {
					forgeButton.setSelected(true);
					optifineButton.setSelected(false);
				} else {
					forgeButton.setSelected(false);
					optifineButton.setSelected(true);
				}
			}
		};

		forgeButton.setActionCommand("forge");
		optifineButton.setActionCommand("optifine");
		forgeButton.addActionListener(actionListener);
		optifineButton.addActionListener(actionListener);

	}

}
