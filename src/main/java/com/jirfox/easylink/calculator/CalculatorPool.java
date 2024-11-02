package com.jirfox.easylink.calculator;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


public class CalculatorPool extends GenericObjectPool<Calculator> {

	public CalculatorPool(PooledObjectFactory<Calculator> factory) {
		super(factory);
	}

	public CalculatorPool(PooledObjectFactory<Calculator> factory, GenericObjectPoolConfig<Calculator> config) {
		super(factory, config);
	}

	public CalculatorPool(PooledObjectFactory<Calculator> factory, GenericObjectPoolConfig<Calculator> config,
			AbandonedConfig abandonedConfig) {
		super(factory, config, abandonedConfig);
	}
}