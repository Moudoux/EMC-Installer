package me.deftware.emc.installer.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JSONGenerator {

	public static JsonObject generateLaunchProfile(String name, String mcVersion) {
		JsonObject json = new JsonObject();
		json.add("name", new JsonPrimitive(name));
		json.add("type", new JsonPrimitive("custom"));
		json.add("icon", new JsonPrimitive("Diamond_Block"));
		json.add("lastVersionId", new JsonPrimitive(mcVersion + "-" + name));
		return json;
	}

	public static JsonObject generateClientJSON(String clientName, String mcVersion, String emcVersion, String optifineVersion) {
		String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "T" + new SimpleDateFormat("hh:mm:ss").format(new Date()) + "-05:00";
		JsonObject jsonObject = new JsonObject();
		String tweaker = "--username ${auth_player_name} " +
						"--version ${version_name} " +
						"--gameDir ${game_directory} " +
						"--assetsDir ${assets_root} " +
						"--assetIndex ${assets_index_name} " +
						"--uuid ${auth_uuid} " +
						"--accessToken ${auth_access_token} " +
						"--userType ${user_type} " +
						"--tweakClass me.deftware.launch.Launcher";
		if (!optifineVersion.equals("")) {
			tweaker += " --tweakClass optifine.OptiFineForgeTweaker";
		}
		// Properties
		jsonObject.add("inheritsFrom", new JsonPrimitive(mcVersion));
		jsonObject.add("id", new JsonPrimitive(mcVersion + "-" + clientName));
		jsonObject.add("time", new JsonPrimitive(date));
		jsonObject.add("releaseTime", new JsonPrimitive(date));
		jsonObject.add("type", new JsonPrimitive("release"));
		jsonObject.add("minecraftArguments", new JsonPrimitive(tweaker));
		jsonObject.add("mainClass", new JsonPrimitive("net.minecraft.launchwrapper.Launch"));
		jsonObject.add("minimumLauncherVersion", new JsonPrimitive("0"));
		jsonObject.add("jar", new JsonPrimitive(mcVersion));
		jsonObject.add("downloads", new JsonObject());
		// Libraries
		JsonArray libsArray = new JsonArray();
		libsArray.add(genArrayObject("name", "net.minecraft:launchwrapper:1.12", "", ""));
		libsArray.add(genArrayObject("name", "me.deftware:EMC:" + emcVersion, "url",
				"https://github.com/Moudoux/EMC/raw/master/maven/"));
		if (!optifineVersion.equals("")) {
			libsArray.add(genArrayObject("name", "optifine:OptiFine:" + optifineVersion.split("OptiFine_")[1].replace(".jar", ""), "", ""));
		}
		libsArray.add(genArrayObject("name", "org.spongepowered:mixin:0.7.1-SNAPSHOT", "url",
				"http://dl.liteloader.com/versions/"));
		libsArray.add(genArrayObject("name", "net.jodah:typetools:0.5.0", "url",
				"https://repo.maven.apache.org/maven2/"));
		jsonObject.add("libraries", libsArray);
		return jsonObject;
	}

	private static JsonObject genArrayObject(String name, String n, String url, String u) {
		JsonObject obj = new JsonObject();
		obj.addProperty(name, n);
		if (!url.equals("")) {
			obj.addProperty(url, u);
		}
		return obj;
	}

}
