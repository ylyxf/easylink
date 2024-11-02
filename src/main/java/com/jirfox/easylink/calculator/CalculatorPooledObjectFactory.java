package com.jirfox.easylink.calculator;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalculatorPooledObjectFactory extends BasePooledObjectFactory<Calculator> {

	private static final Logger logger = LoggerFactory.getLogger(CalculatorPooledObjectFactory.class);

	private Map<String, DataSource> dataSourceMap;

	private CalculatorTemplate calculatorTemplate;

	public CalculatorPooledObjectFactory(Map<String, DataSource> dataSourceMap, CalculatorTemplate calculatorTemplate) {
		this.dataSourceMap = dataSourceMap;
		this.calculatorTemplate = calculatorTemplate;
	}

	@Override
	public synchronized Calculator create() throws Exception {
		Calculator calculator = new Calculator(dataSourceMap, calculatorTemplate);
		calculator.create();
		return calculator;
	}

	@Override
	public PooledObject<Calculator> wrap(Calculator calculator) {
		return new DefaultPooledObject<Calculator>(calculator);
	}

	@Override
	public boolean validateObject(PooledObject<Calculator> p) {
		try {
			Calculator calculator = p.getObject();
			calculator.check();
			return true;
		} catch (Exception e) {
			logger.error("连接池测试calculator是否可用时异常", e);
			return false;
		}
	}

	@Override
	public void destroyObject(PooledObject<Calculator> p) throws Exception {
		Calculator calculator = p.getObject();
		logger.info("关闭 calculator:");
		calculator.destory();
		super.destroyObject(p);
	}

}
