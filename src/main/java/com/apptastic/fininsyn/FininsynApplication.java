package com.apptastic.fininsyn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableReactiveMongoRepositories
public class FininsynApplication {

	public static void main(String[] args) {
		InstrumentLookup.getInstance();
		SpringApplication.run(FininsynApplication.class, args);
	}
}
