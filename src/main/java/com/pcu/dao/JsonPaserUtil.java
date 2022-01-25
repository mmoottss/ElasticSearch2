package com.pcu.dao;


import com.elevisor.common.util.json.builder.action.JSONBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonPaserUtil {

	public static Map<String,Object> build(String value) throws IOException {
		return JSONBuilder.jsonToMap(IOUtils.toInputStream(value));
	}
	public static String formatter(String jsonString){
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		JsonElement el = parser.parse(jsonString);
		return gson.toJson(el);

	}
	public static List<Object> readConfigList(Object value) {
		return value != null ? (List)value : null;
	}
	public static Map<String,Object> readConfigMap(Object value) {
		return value != null ? (Map)value : null;
	}
	public static Boolean readConfigBoolean(Object value) {
		return value != null ? (Boolean)value : null;
	}
	public static String readConfigString( Object value) {
		return value != null ? (String)value : null;
	}

}
