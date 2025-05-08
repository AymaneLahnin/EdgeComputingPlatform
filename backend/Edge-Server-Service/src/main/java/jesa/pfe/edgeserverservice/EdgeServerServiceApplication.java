package jesa.pfe.edgeserverservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class EdgeServerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdgeServerServiceApplication.class, args);
	}

}
