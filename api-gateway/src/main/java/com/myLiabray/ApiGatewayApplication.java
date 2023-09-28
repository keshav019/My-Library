package com.myLiabray;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator gatewayRouteLocater(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/api/v1/auth/**").uri("lb://auth-service"))
				.route(r -> r.path("/**").uri("lb://web-app"))
				.build();
	}

}
