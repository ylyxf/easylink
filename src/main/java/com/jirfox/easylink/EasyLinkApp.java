package com.jirfox.easylink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EasyLinkApp {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(EasyLinkApp.class);
		application.run(args);
	}

}
