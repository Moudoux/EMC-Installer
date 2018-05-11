package me.deftware.emc.installer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.deftware.emc.installer.ui.InstallerUI;
import me.deftware.emc.installer.utils.WebUtils;

import javax.swing.*;

public class Main {

	public static final String versionsURL = "https://raw.githubusercontent.com/Moudoux/EMC/master/versions.json";

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new Thread(() -> {
				try {
					JsonObject json = new Gson().fromJson(WebUtils.get(versionsURL), JsonObject.class);
					InstallerUI.create(json).setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
