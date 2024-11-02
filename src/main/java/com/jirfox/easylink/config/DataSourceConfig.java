package com.jirfox.easylink.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.naming.Context;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.naming.NamingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.jndi.JndiTemplate;

@Configuration
public class DataSourceConfig {

	private static final String PREFIX = "datasources";
	private static final String JDBC_POOL_TYPE = "tomcat";

	@Autowired
	private ConfigurableEnvironment environment;

	@Bean("dataSourceMap")
	public Map<String, DataSource> dataSourceMap(ConfigurableApplicationContext context, Context simpleJndiContext) {
		Map<String, DataSource> result = new HashMap<String, DataSource>();

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
				String dataSourceKey = propName.substring(PREFIX.length() + 1);
				dataSourceKey = dataSourceKey.substring(0, dataSourceKey.indexOf('.'));
				Map<String, String> properties = configsMap.computeIfAbsent(dataSourceKey,
						k -> new HashMap<String, String>());
				String propertyValue = environment.getProperty(propName);
				String newKey = propName.substring(PREFIX.length() + 1 + dataSourceKey.length() + 1);
				if (newKey.startsWith(JDBC_POOL_TYPE + ".")) {
					newKey = newKey.substring(JDBC_POOL_TYPE.length() + 1);
					newKey = convertToCamelCase(newKey);
				}
				properties.put(newKey, propertyValue);

			}
		}

		for (Entry<String, Map<String, String>> entry : configsMap.entrySet()) {
			String dataSourceName = entry.getKey();
			Map<String, String> properties = entry.getValue();
			ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
			ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
			aliases.addAliases("jdbc-url", "url");
			Binder binder = new Binder(source.withAliases(aliases));
			org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
			binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(dataSource));
			try {
				dataSource.getConnection().close();
				// 验证无误，加入context中：
				ConfigurableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
				beanFactory.registerSingleton("dataSource_" + dataSourceName, dataSource);
				// 同时注册到jndi环境中：
				simpleJndiContext.bind("java:ds/" + dataSourceName, dataSource);
				simpleJndiContext.lookup("java:ds/" + dataSourceName);
			} catch (Exception e) {
				throw new RuntimeException("error init datasource" + dataSourceName, e);
			}
			result.put(entry.getKey(), dataSource);
		}
		return result;
	}

	private String convertToCamelCase(String input) {
		if (StringUtils.isBlank(input)) {
			return input;
		}

		String[] parts = StringUtils.split(input, '-');
		StringBuilder camelCaseBuilder = new StringBuilder();

		for (int i = 0; i < parts.length; i++) {
			if (i == 0) {
				camelCaseBuilder.append(parts[i]);
			} else {
				String capitalized = StringUtils.capitalize(parts[i]);
				camelCaseBuilder.append(capitalized);
			}
		}
		return camelCaseBuilder.toString();
	}
}
