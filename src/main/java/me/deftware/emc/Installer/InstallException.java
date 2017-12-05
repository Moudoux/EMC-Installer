package me.deftware.emc.Installer;

public class InstallException extends Exception {

	private String message;

	public InstallException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
