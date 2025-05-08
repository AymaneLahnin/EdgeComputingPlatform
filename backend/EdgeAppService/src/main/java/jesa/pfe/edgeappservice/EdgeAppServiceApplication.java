package jesa.pfe.edgeappservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication


public class EdgeAppServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdgeAppServiceApplication.class, args);
	}

}
