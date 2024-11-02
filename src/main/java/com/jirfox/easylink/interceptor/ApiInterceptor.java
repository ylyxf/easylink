package com.jirfox.easylink.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ApiInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(ApiInterceptor.class);


	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 设置traceId
		String traceId = request.getHeader("traceId");
		if (traceId == null) {
			traceId = request.getParameter("traceId");
		}
		if (traceId == null) {
			traceId = UUID.randomUUID().toString();
		}
		request.setAttribute("traceId", traceId);
		MDC.put("traceId", traceId);

		String scriptPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		scriptPath = scriptPath.substring(1);

		// 日志
		MDC.put("logFileName", scriptPath.replaceAll("/", "_"));

		request.setAttribute("scriptPath", scriptPath);

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if (ex != null) {
			String scriptPath = (String) request.getAttribute("scriptPath");
			logger.error(scriptPath, ex);
		}
		MDC.clear();
	}

}