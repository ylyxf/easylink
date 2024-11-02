package com.jirfox.easylink.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ConfigUtil {

	private static final SimpleDateFormat TIME_SDF = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS");

	public static Integer getInteger(String value, Integer defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public static String getTimePrefixUUID() {
		return TIME_SDF.format(new Date()) + "_" + UUID.randomUUID();
	}

}
