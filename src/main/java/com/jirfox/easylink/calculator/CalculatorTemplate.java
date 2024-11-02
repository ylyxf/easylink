package com.jirfox.easylink.calculator;

import java.io.File;
import java.time.Duration;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.jirfox.easylink.utils.ConfigUtil;

public class CalculatorTemplate {

	public CalculatorTemplate(String name, Map<String, String> calculatorProperties, String appHomePath) {
		this.name = name;
		this.appHomePath = appHomePath;
		// 新建一个对象池,传入对象工厂和配置
		dataSourcePoolSize = ConfigUtil.getInteger(calculatorProperties.get("datasource.pool-size"), 1);
		dataSourceType = calculatorProperties.get("datasource.dbtype");
		dataSourceUsername = calculatorProperties.get("datasource.username");
		dataSourcePassword = calculatorProperties.get("datasource.password");
		dataSourceType = calculatorProperties.get("datasource.type");
		dataSourcePath = appHomePath + "/" + calculatorProperties.get("datasource.path");
		File dataSourceDir = new File(dataSourcePath);
		if (!dataSourceDir.exists()) {
			dataSourceDir.mkdirs();
		}

		lifecyleInitScript = calculatorProperties.get("lifecyle.init-script");
		lifecyleClearScript = calculatorProperties.get("lifecyle.clear-script");
		lifecyleNotifyTopic = calculatorProperties.get("lifecyle.notify-topic");
		lifecyleNotifyScript = calculatorProperties.get("lifecyle.notify-script");

		poolConfig = readPoolConfig(calculatorProperties);

	}

	private String name;

	private String appHomePath;

	private GenericObjectPoolConfig<Calculator> poolConfig;

	// 计算声明周期相关
	private String lifecyleInitScript;

	private String lifecyleClearScript;

	private String lifecyleNotifyTopic;

	private String lifecyleNotifyScript;

	// 算子数据库相关
	private Integer dataSourcePoolSize;

	private String dataSourceType;

	private String dataSourceUsername;

	private String dataSourcePassword;

	private String dataSourcePath;

	public String getLifecyleInitScript() {
		return lifecyleInitScript;
	}

	public void setLifecyleInitScript(String lifecyleInitScript) {
		this.lifecyleInitScript = lifecyleInitScript;
	}

	public String getLifecyleClearScript() {
		return lifecyleClearScript;
	}

	public void setLifecyleClearScript(String lifecyleClearScript) {
		this.lifecyleClearScript = lifecyleClearScript;
	}

	public String getLifecyleNotifyTopic() {
		return lifecyleNotifyTopic;
	}

	public void setLifecyleNotifyTopic(String lifecyleNotifyTopic) {
		this.lifecyleNotifyTopic = lifecyleNotifyTopic;
	}

	public String getLifecyleNotifyScript() {
		return lifecyleNotifyScript;
	}

	public void setLifecyleNotifyScript(String lifecyleNotifyScript) {
		this.lifecyleNotifyScript = lifecyleNotifyScript;
	}

	public Integer getDataSourcePoolSize() {
		return dataSourcePoolSize;
	}

	public void setDataSourcePoolSize(Integer dataSourcePoolSize) {
		this.dataSourcePoolSize = dataSourcePoolSize;
	}

	public String getDataSourceUsername() {
		return dataSourceUsername;
	}

	public void setDataSourceUsername(String dataSourceUsername) {
		this.dataSourceUsername = dataSourceUsername;
	}

	public String getDataSourcePassword() {
		return dataSourcePassword;
	}

	public void setDataSourcePassword(String dataSourcePassword) {
		this.dataSourcePassword = dataSourcePassword;
	}

	public String getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public String getDataSourcePath() {
		return dataSourcePath;
	}

	public void setDataSourcePath(String dataSourcePath) {
		this.dataSourcePath = dataSourcePath;
	}

	public String getAppHomePath() {
		return appHomePath;
	}

	public void setAppHomePath(String appHomePath) {
		this.appHomePath = appHomePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GenericObjectPoolConfig<Calculator> getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(GenericObjectPoolConfig<Calculator> poolConfig) {
		this.poolConfig = poolConfig;
	}

	private GenericObjectPoolConfig<Calculator> readPoolConfig(Map<String, String> calculatorProperties) {
		// 设置对象池的相关参数
		GenericObjectPoolConfig<Calculator> poolConfig = new GenericObjectPoolConfig<Calculator>();
		// 一定要关闭jmx，不然springboot启动会报已经注册了某个jmx的错误
		poolConfig.setJmxEnabled(false);

		Integer maxTotal = ConfigUtil.getInteger(calculatorProperties.get("pool.max-total"), 5);
		Integer maxIdle = ConfigUtil.getInteger(calculatorProperties.get("pool.max-idle"), 3);
		Integer minIdle = ConfigUtil.getInteger(calculatorProperties.get("pool.min-idle"), 1);
		Integer maxAge = ConfigUtil.getInteger(calculatorProperties.get("pool.max-age"), 60);
		Integer maxAgeCheckDuration = ConfigUtil.getInteger(calculatorProperties.get("pool.max-age-check-duration"),
				maxAge);
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setTestOnBorrow(false);
		poolConfig.setTestOnReturn(false);
		poolConfig.setTestWhileIdle(true);

		poolConfig.setMinEvictableIdleDuration(Duration.ofMillis(maxAge * 1000));
		// 表示idle object evitor两次扫描之间要sleep的毫秒数；
		poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(maxAgeCheckDuration * 1000));

		return poolConfig;

	}

}
