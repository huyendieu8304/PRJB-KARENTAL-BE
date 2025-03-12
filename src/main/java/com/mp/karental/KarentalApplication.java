package com.mp.karental;

import com.mp.karental.payment.configuration.PaymentConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PaymentConfig.class)
public class KarentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(KarentalApplication.class, args);
	}

}
