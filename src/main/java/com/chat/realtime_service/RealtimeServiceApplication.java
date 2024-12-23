package com.chat.realtime_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RealtimeServiceApplication {
	//TODO: try to rename the class names related to websocket session and new message event
	// TODO: refactor the code, apply SOLID principles
	public static void main(String[] args) {
		SpringApplication.run(RealtimeServiceApplication.class, args);
	}

}
