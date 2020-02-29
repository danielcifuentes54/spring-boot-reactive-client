package com.reactive.dc.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootReactiveClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactiveClientApplication.class, args);
	}

}
