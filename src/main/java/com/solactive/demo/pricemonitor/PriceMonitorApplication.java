package com.solactive.demo.pricemonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableScheduling
@SpringBootApplication
public class PriceMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PriceMonitorApplication.class, args);
	}

}
