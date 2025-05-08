package jesa.pfe.deploymentmanagement.controllers;



import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import jesa.pfe.deploymentmanagement.sharedmodels.VmEdgeAppresponse;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import jesa.pfe.deploymentmanagement.enums.VMOperationStatus;
import jesa.pfe.deploymentmanagement.repositories.VirtualMachineRepository;
import jesa.pfe.deploymentmanagement.services.VirtualMachineOrchestratorService;
import jesa.pfe.deploymentmanagement.services.VirtualMachineService;
import jesa.pfe.deploymentmanagement.services.VmControlService;
import jesa.pfe.deploymentmanagement.services.VmNetworkService;
import jesa.pfe.deploymentmanagement.sharedmodels.VmEdgeServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vms")
public class VirtualMachineController {
    private static final Logger logger = LoggerFactory.getLogger(VirtualMachineController.class);


    private final VirtualMachineOrchestratorService orchestratorService;
    private final VirtualMachineService vmService;
    private final VmControlService vmControlService;
    private final VmNetworkService vmNetworkService;
    private final VirtualMachineRepository virtualMachineRepository;

    @Autowired
    public VirtualMachineController(VirtualMachineOrchestratorService orchestratorService,
                                    VirtualMachineService vmService, VmControlService vmControlService, VmNetworkService vmNetworkService, VirtualMachineRepository virtualMachineRepository) {
        this.orchestratorService = orchestratorService;
        this.vmService = vmService;
        this.vmControlService = vmControlService;
        this.vmNetworkService = vmNetworkService;
        this.virtualMachineRepository = virtualMachineRepository;
    }

    /**
     * Create and register a new VM (blocks until VM is up and IP assigned)
     */
    @PostMapping("/create")
    public ResponseEntity<VirtualMachine> createVm(@RequestBody VirtualMachine vmRequest, @RequestParam DeploymentType deploymentType) {
        try {
            VirtualMachine createdVm = orchestratorService.createAndRegisterVm(vmRequest,deploymentType);
            return ResponseEntity.ok(createdVm);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }

    }

    /**
     * List all VMs in the database
     */
    @GetMapping("/getAllVms")
    public List<VirtualMachine> listVms() {
        return vmService.findAllVirtualMachines();
    }

    /**
     * handle vm-edgeApp association
     */
    @GetMapping("/vm-edgeApp/{vmName}")
    public VmEdgeAppresponse getVmWithEdgeApp(@PathVariable String vmName) {
        return vmService.findVmWithEdgeApp(vmName);
    }

    /***
     * handle vm-edgeServers association
     */
    @GetMapping("/vm-edgeServers/{vmName}")
    public VmEdgeServerResponse getVmWithEdgeServers(@PathVariable String vmName) {
        return vmService.findVmWithEdgeServers(vmName);
    }

    @GetMapping("/find-vms-ByServer/{edgeServerName}")
    public List<VirtualMachine> findVmByServer(@PathVariable String edgeServerName) {
        return vmService.findvmByEdgseSererName(edgeServerName);
    }

    /**
     * Get a VM by ID
     */
    @GetMapping("/findvm/{id}")
    public ResponseEntity<VirtualMachine> getVm(@PathVariable Integer id) {
        Optional<VirtualMachine> vmOpt = vmService.findVirtualMachineById(id);
        return vmOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a VM (database record only)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteVm(@PathVariable Integer id) {
        vmService.deleteVirtualMachine(id);
        return ResponseEntity.noContent().build();
    }

    //control vms in server
    @PostMapping("/control/{vmName}/start")
    public ResponseEntity<Map<String, Object>> startVM(@PathVariable String vmName) {
        VMOperationStatus status = vmControlService.startVM(vmName);
        return createResponse(status, "start");
    }



    @PostMapping("/control/{vmName}/stop")
    public ResponseEntity<Map<String, Object>> stopVM(@PathVariable String vmName) {
        VMOperationStatus status = vmControlService.stopVM(vmName);
        return createResponse(status, "stop");
    }

    @PostMapping("/control/{vmName}/force-stop")
    public ResponseEntity<Map<String, Object>> forceStopVM(@PathVariable String vmName) {
        VMOperationStatus status = vmControlService.forceStopVM(vmName);
        return createResponse(status, "force-stop");
    }

    @DeleteMapping("/control/delete/{vmName}")
    public ResponseEntity<Map<String, Object>> deleteVM(@PathVariable String vmName) {
        VMOperationStatus status = vmControlService.deleteVM(vmName);
        return createResponse(status, "delete");
    }


    @GetMapping("/control/status/{vmName}")
    public ResponseEntity<Map<String, Object>> getVMStatus(@PathVariable String vmName) {
        String status = vmControlService.getVMStatus(vmName);
        Map<String, Object> response = new HashMap<>();
        response.put("vmName", vmName);
        response.put("status", status);
        return ResponseEntity.ok(response);
    }


    private ResponseEntity<Map<String, Object>> createResponse(VMOperationStatus status, String operation) {
        Map<String, Object> response = new HashMap<>();
        response.put("operation", operation);
        response.put("status", status);

        if (status == VMOperationStatus.SUCCESS || status == VMOperationStatus.VM_ALREADY_IN_DESIRED_STATE) {
            return ResponseEntity.ok(response);
        } else if (status == VMOperationStatus.VM_NOT_FOUND) {
            response.put("message", "Virtual machine not found in database");
            return ResponseEntity.notFound().build();
        } else {
            response.put("message", "Operation failed: " + status);
            return ResponseEntity.badRequest().body(response);
        }
    }



}
