package com.airelogic.artistinfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class ArtistInfoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtistInfoApplication.class, args);
	}

	@Bean
	public RestTemplate getRestTemplateBean(){
		return new RestTemplate();
	}

	@Bean
	public ConcurrentHashMap<String, Integer> getSongToWordCount(){
		return new ConcurrentHashMap<>();
	}
}
