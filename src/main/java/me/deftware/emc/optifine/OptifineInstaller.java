package me.deftware.emc.optifine;

import java.io.File;
import java.nio.file.Paths;

import me.deftware.emc.utils.OSUtils;
import me.deftware.emc.utils.WebUtils;

public class OptifineInstaller {

	public static void install(String version) throws Exception {
		File optifine = Paths.get("Optifine.jar").toFile();
		String website = WebUtils.get("https://optifine.net/adloadx?f=" + version);
		website = "https://optifine.net/downloadx" + website.split("downloadx")[1].split("'")[0];
		WebUtils.download(website, Paths.get("optifine.jar").toAbsolutePath().toString());
		if (OSUtils.isWindows()) {
			new ProcessBuilder("C:\\Program Files (x86)\\Minecraft\\runtime\\jre-x64\\1.8.0_25\\bin\\javaw.exe", "-cp",
					optifine.getAbsolutePath(), "optifine.Installer").start();
		} else if (OSUtils.isLinux()) {
			// TODO: Linux install
		} else {
			// TODO: Mac install
		}
		optifine.delete();
	}

}
