package com.jirfox.easylink.calculator;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class H2Server {

	private static final Logger logger = LoggerFactory.getLogger(H2Server.class);

	@Value("${h2-server.port}")
	private Integer port;

	private Server server;

	@PostConstruct
	public void init() throws SQLException {
		String[] args = ("-tcp,-tcpAllowOthers,-tcpPort," + port + ",-ifNotExists").split(",");
		server = org.h2.tools.Server.createTcpServer(args);
		server.start();
		logger.info("h2 started at " + port);
	}

	@PreDestroy
	public void destory() {
		server.stop();
	}
}
