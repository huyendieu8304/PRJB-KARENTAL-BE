package com.mp.karental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KarentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(KarentalApplication.class, args);
	}

}
