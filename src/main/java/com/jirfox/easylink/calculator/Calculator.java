package com.jirfox.easylink.calculator;

import java.util.Map;

import javax.sql.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.jirfox.easylink.utils.ConfigUtil;

import groovy.lang.Binding;

public class Calculator {

	private CalculatorTemplate calculatorTemplate;

	private Map<String, DataSource> dataSourceMap;

	private String url = "";

	private org.apache.tomcat.jdbc.pool.DataSource dataSource;

	public Calculator(Map<String, DataSource> dataSourceMap, CalculatorTemplate calculatorTemplate) {
		super();
		this.calculatorTemplate = calculatorTemplate;
		this.dataSourceMap = dataSourceMap;
		String databaseName = ConfigUtil.getTimePrefixUUID();
		if ("mem".equals(calculatorTemplate.getDataSourceType())) {
			url = "jdbc:h2:mem:" + databaseName;
		} else if ("file".equals(calculatorTemplate.getDataSourceType())) {
			url = "jdbc:h2:file:" + calculatorTemplate.getDataSourcePath() + "/" + databaseName;
		}
	}

	public Calculator(Map<String, DataSource> dataSourceMap, CalculatorTemplate calculatorTemplate, String url) {
		super();
		this.calculatorTemplate = calculatorTemplate;
		this.dataSourceMap = dataSourceMap;
		this.url = url;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void destory() {
		this.dataSource.close(true);
	}

	public void create() {
		PoolProperties poolProperties = new PoolProperties();
		poolProperties.setUrl(url);
		poolProperties.setDriverClassName("org.h2.Driver");
		poolProperties.setUsername(calculatorTemplate.getDataSourceUsername());
		poolProperties.setPassword(calculatorTemplate.getDataSourcePassword());

		poolProperties.setInitialSize(calculatorTemplate.getDataSourcePoolSize());
		poolProperties.setMaxActive(calculatorTemplate.getDataSourcePoolSize());
		poolProperties.setMaxIdle(calculatorTemplate.getDataSourcePoolSize());
		poolProperties.setMinIdle(1);
		poolProperties.setMaxWait(1000);

		this.dataSource = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
		String initScript = calculatorTemplate.getName() + "/" + calculatorTemplate.getLifecyleInitScript();
		Binding binding = new Binding();
		binding.setVariable("_ds", dataSourceMap);
		binding.setVariable("_calc", dataSource);
		ScriptRunner.runScript(initScript, binding);

	}

	public void check() {

	}

}
