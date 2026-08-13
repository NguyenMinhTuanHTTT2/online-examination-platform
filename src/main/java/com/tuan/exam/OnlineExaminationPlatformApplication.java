package com.tuan.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class OnlineExaminationPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineExaminationPlatformApplication.class, args);
	}

}
