package me.deftware.emc.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.deftware.emc.Installer.Main;
import me.deftware.emc.Installer.Utils;

public class Installer extends JFrame {

	private Set<Map.Entry<String, JsonElement>> versions;
	private JComboBox combobox;
	private String v;

	public Installer(JsonObject json) {
		this.versions = json.entrySet();
		System.out.println(versions.toString());
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
				System.out.println(data);
				String init = data.split(":")[0] + ".init";
				String patch = data.split(":")[1];
				String mcVersion = data.split(":")[0];
				String clientName = Main.name + "_" + data.split(":")[0];

				File minecraft = Utils.getMinecraft(mcVersion);
				// Copy Minecraft jar
				File clientDir = new File(
						minecraft.getParent().replace(File.separatorChar + mcVersion, File.separator + clientName));
				// Delete existing EMC install
				if (clientDir.exists()) {
					clientDir.delete();
				}
				clientDir.mkdir();
				File clientFile = new File(clientDir.getAbsolutePath() + File.separator + mcVersion + ".jar");
				Utils.copyFile(minecraft, clientFile);
				pBar.updateBar(1);
				// Download init
				File iFile = new File(clientDir.getAbsolutePath() + File.separator + "init.patch");
				Utils.getInit(iFile, init);
				pBar.updateBar(2);
				// Apply patch
				File tmp = new File(clientDir.getAbsolutePath() + File.separator + mcVersion + "_PATCHED.jar");
				Utils.applyPatch(clientFile, iFile, tmp);
				clientFile.delete();
				tmp.renameTo(clientFile);
				iFile.delete();
				// Download patch
				pBar.updateBar(3);
				File pFile = new File(clientDir.getAbsolutePath() + File.separator + "emc.patch");
				Utils.getPatch(pFile, patch);
				pBar.updateBar(4);
				// Apply patch
				Utils.applyPatch(clientFile, pFile,
						new File(clientDir.getAbsolutePath() + File.separator + clientName + ".jar"));
				// Delete files
				clientFile.delete();
				pFile.delete();
				pBar.updateBar(5);
				// Copy json
				File json = new File(minecraft.getParent() + File.separator + mcVersion + ".json");
				if (!json.exists()) {
					Utils.error("Could not find \"" + mcVersion + ".json\"");
				}
				clientFile = new File(clientDir.getAbsolutePath() + File.separator + clientName + ".json");
				Utils.copyFile(json, clientFile);
				// Edit json
				JsonObject jsonObject = new Gson().fromJson(Utils.readFile(clientFile), JsonObject.class);
				if (!jsonObject.has("id")) {
					Utils.error("Invalid json file");
				}
				// Update id
				jsonObject.remove("id");
				jsonObject.add("id", new JsonPrimitive(clientName));
				// Remove things
				jsonObject.remove("logging");
				jsonObject.remove("downloads");
				// Save
				Utils.saveJson(jsonObject.toString(), clientFile);
				// Optional: Install Client.jar
				clientFile = new File(clientDir.getAbsolutePath() + File.separator + "Client.jar");
				if (!Utils.extractAsset("/assets/Client.jar", clientFile)) {
					if (clientFile.exists()) {
						clientFile.delete();
					}
				}
				// Install optional mods
				for (JsonElement mod : Main.emcJson.get("mods").getAsJsonArray()) {
					String name = mod.getAsString();
					Utils.extractAsset("/assets/" + name,
							new File(clientDir.getAbsolutePath() + File.separator + "mods" + File.separator + name));
				}
				pBar.updateBar(6);
				Window w = SwingUtilities.getWindowAncestor(pBar);
				w.setVisible(false);
				Utils.infoBox(
						Main.name + " was successfully installed, open your Minecraft launcher and select \"release "
								+ clientName + "\"",
						"Installation done");
			} catch (Exception ex) {
				Utils.errorBox("Failed to install, " + ex.getMessage(), "Install failed");
			}
			System.exit(0);
		}).start();
	}

	private void initGui() {
		this.setResizable(false);
		this.setBounds(100, 100, 450, 250);
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
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);
		final JLabel lblTitle = new JLabel(Main.name + " Installer");
		lblTitle.setFont(new Font("Segoe UI Light", 0, 32));
		final GridBagConstraints gbc_lblTitle = new GridBagConstraints();
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
		final JPanel panel_2 = new JPanel();
		final GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.anchor = 15;
		gbc_panel_2.fill = 2;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 4;
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
		this.setVisible(true);
	}

}
