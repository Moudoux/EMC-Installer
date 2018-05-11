package me.deftware.emc.installer.utils;

public class OSUtils {

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("darwin") >= 0) || (OS.indexOf("mac") >= 0);
	}

	public static boolean isLinux() {
		return (OS.indexOf("nux") >= 0);
	}

}
