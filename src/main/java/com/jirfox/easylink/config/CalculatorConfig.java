package com.jirfox.easylink.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import com.jirfox.easylink.calculator.Calculator;
import com.jirfox.easylink.calculator.CalculatorPool;
import com.jirfox.easylink.calculator.CalculatorPooledObjectFactory;
import com.jirfox.easylink.calculator.CalculatorTemplate;

@Configuration
public class CalculatorConfig {

	private static final String PREFIX = "calculators";

	@Autowired
	private ConfigurableEnvironment environment;

	@Autowired
	@Qualifier("dataSourceMap")
	private Map<String, DataSource> dataSourceMap;

	public static final Map<String, CalculatorTemplate> calculatorTemplateMap = new HashMap<String, CalculatorTemplate>();

	@Bean("calculatorTemplateMap")
	public Map<String, CalculatorTemplate> calculatorTemplateMap(ConfigurableApplicationContext context) {
		Map<String, CalculatorTemplate> result = new HashMap<String, CalculatorTemplate>();

		Map<String, Map<String, String>> configsMap = new TreeMap<>();

		for (PropertySource<?> propertySource : environment.getPropertySources()) {
			if (!(propertySource instanceof EnumerablePropertySource<?>)) {
				continue;
			}
			EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
			for (String propName : enumerablePropertySource.getPropertyNames()) {
				if (!propName.startsWith(PREFIX)) {
					continue;
				}
				String calculatorKey = propName.substring(PREFIX.length() + 1);
				calculatorKey = calculatorKey.substring(0, calculatorKey.indexOf('.'));
				Map<String, String> properties = configsMap.computeIfAbsent(calculatorKey,
						k -> new HashMap<String, String>());
				String propertyValue = environment.getProperty(propName);
				properties.put(propName.substring(PREFIX.length() + 1 + calculatorKey.length() + 1), propertyValue);
			}
		}

		ApplicationHome home = new ApplicationHome();
		String appHomeDir = home.getDir().getAbsolutePath();

		for (Entry<String, Map<String, String>> entry : configsMap.entrySet()) {
			String calculatorKey = entry.getKey();
			Map<String, String> calculatorProperties = entry.getValue();
			CalculatorTemplate calculatorTemplate = new CalculatorTemplate(calculatorKey, calculatorProperties,
					appHomeDir);
			result.put(calculatorKey, calculatorTemplate);
		}
		return result;
	}

	/**
	 * 
	 * @param calculatorTemplateMap
	 * @return
	 */
	@Bean("calculatorPoolMap")
	public Map<String, CalculatorPool> calculatorPoolMap(Map<String, CalculatorTemplate> calculatorTemplateMap) {
		Map<String, CalculatorPool> result = new HashMap<String, CalculatorPool>();

		for (Entry<String, CalculatorTemplate> entry : calculatorTemplateMap.entrySet()) {
			CalculatorTemplate calculatorTemplate = entry.getValue();
			GenericObjectPoolConfig<Calculator> poolConfig = calculatorTemplate.getPoolConfig();
			CalculatorPooledObjectFactory objectFactory = new CalculatorPooledObjectFactory(dataSourceMap,
					calculatorTemplate);
			CalculatorPool calculatorPool = new CalculatorPool(objectFactory, poolConfig);

			initPool(calculatorPool, poolConfig.getMinIdle(), poolConfig.getMaxTotal());

			result.put(entry.getKey(), calculatorPool);
		}
		return result;
	}

	private void initPool(CalculatorPool calculatorPool, int initSize, int maxTotal) {
		if (initSize <= 0) {
			return;
		}
		int size = Math.min(initSize, maxTotal);
		for (int i = 0; i < size; i++) {
			try {
				calculatorPool.addObject();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
