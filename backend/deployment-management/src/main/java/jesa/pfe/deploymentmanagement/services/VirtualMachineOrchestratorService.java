package jesa.pfe.deploymentmanagement.services;

import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@Service
public class VirtualMachineOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(VirtualMachineOrchestratorService.class);

    private final VagrantService vagrantService;
    private final VirtualMachineService vmService;

    @Autowired
    public VirtualMachineOrchestratorService(VagrantService vagrantService,
                                             VirtualMachineService vmService) {
        this.vagrantService = vagrantService;
        this.vmService = vmService;
    }

    /**
     * End-to-end VM creation and registration.
     *
     * @param request VM entity with desired parameters
     * @return persisted VM entity with IP and timestamps
     */
    public VirtualMachine createAndRegisterVm(VirtualMachine request, DeploymentType deploymentType) {
        // 1) Persist initial VM entity to obtain ID and timestamps
        VirtualMachine vm = vmService.saveVirtualMachine(request, deploymentType);

        // 2) Create the real VM via Vagrant and block until ready
        CompletableFuture<String> creationFuture = vagrantService.createAndStartVm(vm);
        String resultLog = creationFuture.join();
        logger.info("Vagrant creation result for VM {}: {}", vm.getName(), resultLog);

        // 3) vagrantService should have populated the IP on the entity
        //    Now re-save the entity with the updated IP

        return vmService.saveVirtualMachine(vm,deploymentType);
    }
}
