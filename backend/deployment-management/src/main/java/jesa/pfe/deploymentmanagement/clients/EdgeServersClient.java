package jesa.pfe.deploymentmanagement.clients;

import jesa.pfe.deploymentmanagement.sharedmodels.EdgeApp;
import jesa.pfe.deploymentmanagement.sharedmodels.EdgeServer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name="edgeServer-service",url = "${application.config.edgeServer.url}")
public interface EdgeServersClient {
    @GetMapping("/find-servers-byVmName/{name}")
    List<EdgeServer> findServersByDuName(@PathVariable("name") String vmName);
}
