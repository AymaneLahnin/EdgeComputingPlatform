package jesa.pfe.deploymentmanagement.clients;

import jesa.pfe.deploymentmanagement.sharedmodels.EdgeApp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="edgeApp-service",url = "${application.config.edgeApp.url}")
public interface EdgeAppsClient {
    @GetMapping("/find-all-apps-byDu/{name}")
    List<EdgeApp> findAllAppsByDuName(@PathVariable("name") String vmName);
}
