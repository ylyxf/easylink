package com.jirfox.easylink.config;

import java.io.IOException;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import groovy.util.GroovyScriptEngine;

@Configuration
public class GroovyApiConfig {

	public static final String GROOVY_API_ROOT = new ApplicationHome().getDir().getAbsolutePath() + "/api";

	@Bean("groovyScriptEngine")
	public GroovyScriptEngine groovyScriptEngine() throws IOException {
		String[] roots = new String[] { GROOVY_API_ROOT };
		GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(roots);
		return groovyScriptEngine;
	}

}
