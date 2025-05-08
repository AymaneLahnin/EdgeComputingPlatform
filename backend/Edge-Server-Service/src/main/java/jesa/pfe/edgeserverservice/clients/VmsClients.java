package jesa.pfe.edgeserverservice.clients;

import jesa.pfe.edgeserverservice.sharedmodels.VirtualMachine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "vms-edgesserver-services",url = "${application.config.vm.url}")
public interface VmsClients {
    @GetMapping("/find-vms-ByServer/{edgeServerName}")
    List<VirtualMachine> getVirtualMachinesByServer(@PathVariable String edgeServerName);
}
