package jesa.pfe.deploymentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DeploymentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeploymentManagementApplication.class, args);
	}

}
