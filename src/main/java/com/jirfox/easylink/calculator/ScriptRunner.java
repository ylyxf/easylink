package com.jirfox.easylink.calculator;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

public class ScriptRunner {

	private static final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);

	private static GroovyScriptEngine SCRIPT_ENGINE;

	static {
		String scriptHome = new ApplicationHome().getDir().getAbsolutePath() + "/calculator";
		String[] roots = new String[] { scriptHome };
		try {
			SCRIPT_ENGINE = new GroovyScriptEngine(roots);
		} catch (IOException e) {
			logger.error("can't init ", e);
			throw new RuntimeException("can't init SCRIPT_ENGINE:" + scriptHome);
		}
	}

	public static void runScript(String scriptPath, Binding binding ) {
		try {
			SCRIPT_ENGINE.run(scriptPath, binding);
		} catch (Exception e) {
			String errorMsg = "can't run script " + scriptPath;
			logger.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}

	}

}
