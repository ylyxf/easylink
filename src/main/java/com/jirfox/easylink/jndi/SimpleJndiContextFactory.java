package com.jirfox.easylink.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class SimpleJndiContextFactory implements InitialContextFactory {

	private static SimpleJndiContext context = new SimpleJndiContext(null, "SimpleJnid4H2LinkedTableOnly");

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		return context;
	}

}
