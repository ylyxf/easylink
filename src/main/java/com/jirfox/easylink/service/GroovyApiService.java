package com.jirfox.easylink.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jirfox.easylink.calculator.Calculator;
import com.jirfox.easylink.calculator.CalculatorPool;
import com.jirfox.easylink.calculator.CalculatorTemplate;
import com.jirfox.easylink.config.CalculatorConfig;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

@Service
public class GroovyApiService {

	private static final Logger logger = LoggerFactory.getLogger(GroovyApiService.class);

	private static String homePath = new ApplicationHome().getDir().getAbsolutePath();

	@Autowired
	private GroovyScriptEngine groovyScriptEngine;

	@Autowired
	private ConfigurableEnvironment environment;

	@Autowired
	private Map<String, DataSource> dataSourceMap;

	@Autowired
	private Map<String, CalculatorPool> calculatorPoolMap;

	@Autowired
	private Environment env;

	private ObjectMapper objectMapper = new ObjectMapper();

	public Object execute(String scriptPath, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (scriptPath.startsWith("/") || scriptPath.indexOf(".") != -1) {
			throw new RuntimeException("unsafe call");
		}
		Binding binding = new Binding();
		binding.setVariable("_env", env);
		binding.setVariable("_logger", logger);
		binding.setVariable("_ds", dataSourceMap);
		binding.setVariable("_homePath", homePath);
		binding.setVariable("_request", request);
		binding.setVariable("_response", response);

		binding.setVariable("_mapper", objectMapper);
		binding.setVariable("_env", environment);

		Map<String, Object> params = transParams(request);
		binding.setVariable("_params", params);

		int indexCalc = scriptPath.indexOf('@');
		String calcPoolName = null;
		CalculatorPool calculatorPool = null;
		Calculator calculator = null;
		if (indexCalc != -1) {
			calcPoolName = scriptPath.substring(indexCalc+1);
			calculatorPool = calculatorPoolMap.get(calcPoolName);
			if (calculatorPool == null) {
				throw new RuntimeException("can't find calcPoolName:" + calcPoolName);
			} else {

				if (params.get("_debug") == null) {
					calculator = calculatorPool.borrowObject();
					binding.setVariable("_calc", calculator.getDataSource());
				} else {
					String debugDataBase = params.get("_debug").toString();
					CalculatorTemplate calculatorTemplate = CalculatorConfig.calculatorTemplateMap.get(calcPoolName);
					String dbfile = homePath + "/debug/" + debugDataBase;
					calculator = new Calculator(dataSourceMap, calculatorTemplate, "jdbc:h2:tcp://localhost/" + dbfile);
					calculator.create();
					binding.setVariable("_calc", calculator.getDataSource());
				}
			}

		}

		try {
			Object result = groovyScriptEngine.run(scriptPath + ".groovy", binding);
			return result;
		} catch (Exception e) {
			throw new RuntimeException("can't execut api :" + scriptPath, e);
		} finally {
			if (calculatorPool != null && calculator != null) {
				calculatorPool.returnObject(calculator);
			} else if (calculator != null) {
				calculator.destory();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> transParams(HttpServletRequest request) throws Exception {
		String contentType = request.getContentType();
		if ("application/json".equals(contentType)) {
			String requestBody = IOUtils.toString(request.getReader());
			Map<String, Object> params = objectMapper.readValue(requestBody, Map.class);
			return params;
		} else {
			Map<String, Object> params = new HashMap<>();
			Map<String, String[]> parameterMap = request.getParameterMap();

			for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
				String parameterName = entry.getKey();
				String[] parameterValues = entry.getValue();

				if (parameterValues.length == 1) {
					params.put(parameterName, parameterValues[0]);
				} else if (parameterValues.length > 1) {
					params.put(parameterName, parameterValues);
				}
			}
			return params;
		}
	}
}
