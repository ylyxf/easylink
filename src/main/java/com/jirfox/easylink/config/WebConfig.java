package com.jirfox.easylink.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jirfox.easylink.interceptor.ApiInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Autowired
	private ApiInterceptor apiInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(apiInterceptor).addPathPatterns("/api/**");
	}
}