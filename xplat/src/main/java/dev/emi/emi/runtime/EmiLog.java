package dev.emi.emi.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmiLog {
	public static final Logger LOG = LogManager.getLogger("EMI");

	public static void info(String str) {
		LOG.info("[EMI] " + str);
	}

	public static void warn(String str) {
		LOG.warn("[EMI] " + str);
	}

	public static void error(String str) {
		LOG.error("[EMI] " + str);
	}

	public static void error(String str, Throwable t) {
		LOG.error(str, t);
	}
}
