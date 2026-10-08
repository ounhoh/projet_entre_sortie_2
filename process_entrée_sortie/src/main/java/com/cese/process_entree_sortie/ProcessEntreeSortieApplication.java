package com.cese.process_entree_sortie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProcessEntreeSortieApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessEntreeSortieApplication.class, args);
	}

}
