package com.manyakakkar.DataFlowX;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataFlowXApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataFlowXApplication.class, args);
	}

}
