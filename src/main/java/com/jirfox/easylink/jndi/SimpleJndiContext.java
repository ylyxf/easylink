package com.jirfox.easylink.jndi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.naming.NamingContext;

public class SimpleJndiContext extends NamingContext {

	public SimpleJndiContext(Hashtable<String, Object> env, String name) {
		super(env, name);
	}

	private Map<String, Object> result = new HashMap<String, Object>();

	@Override
	public void bind(String name, Object obj) throws NamingException {
		result.put(name, obj);
	}

	@Override
	public Object lookup(String name) throws NamingException {
		Object obj = result.get(name);
		if (obj == null) {
			throw new NamingException("can't look up " + name);
		}
		return obj;
	}

}
