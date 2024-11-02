package com.jirfox.easylink.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.jirfox.easylink.service.GroovyApiService;

@RestController
public class GroovyApiController {

	@Autowired
	private GroovyApiService groovyApiService;

	@RequestMapping("/api/**/*")
	public Object api(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String scriptPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		scriptPath = scriptPath.replaceFirst("/api/", "");
		return groovyApiService.execute(scriptPath, request, response);
	}
}
