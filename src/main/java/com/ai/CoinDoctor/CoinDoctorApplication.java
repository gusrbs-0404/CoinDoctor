package com.ai.CoinDoctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoinDoctorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoinDoctorApplication.class, args);
	}

}
