package org.smartvert.smartvert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartVertApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartVertApplication.class, args);
	}

}
