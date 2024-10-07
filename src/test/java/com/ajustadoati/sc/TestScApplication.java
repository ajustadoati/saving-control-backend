package com.ajustadoati.sc;

import org.springframework.boot.SpringApplication;

public class TestScApplication {

	public static void main(String[] args) {
		SpringApplication.from(ScApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
