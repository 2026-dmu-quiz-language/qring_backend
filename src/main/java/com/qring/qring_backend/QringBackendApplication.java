package com.qring.qring_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.qring.qring_backend.domain")
public class QringBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(QringBackendApplication.class, args);
	}

}
