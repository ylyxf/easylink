package com.jirfox.easylink.config;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jirfox.easylink.jndi.SimpleJndiContextFactory;

@Configuration
public class JndiConfig {

	@Bean
	public Context simpleJndiContext() throws NamingException {
		System.setProperty("java.naming.factory.initial", SimpleJndiContextFactory.class.getName());
		InitialContext initialContext = new InitialContext(null);
		return initialContext;
	}
}