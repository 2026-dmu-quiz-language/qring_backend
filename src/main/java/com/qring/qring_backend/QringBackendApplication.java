package com.qring.qring_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/** Spring Boot 진입점. 엔티티는 domain 패키지에서 스캔. */
@SpringBootApplication
@EntityScan(basePackages = "com.qring.qring_backend.domain")
public class QringBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(QringBackendApplication.class, args);
	}

}
