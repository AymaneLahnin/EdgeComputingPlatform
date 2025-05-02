package jesa.pfe.deploymentmanagement.controllers;
//
//
//import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
//import jesa.pfe.deploymentmanagement.services.VirtualMachineService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/apiEdge/virtual-machines")
//public class VirtualMachineController {
//
////    /*private final VirtualMachineService virtualMachineService;
////
////    @Autowired
////    public VirtualMachineController(VirtualMachineService virtualMachineService) {
////        this.virtualMachineService = virtualMachineService;
////    }
////
////
////    /**
////     * Get all virtual machines
////     * @return List of all virtual machines
////     */
////    @GetMapping
////    public ResponseEntity<List<VirtualMachine>> getAllVirtualMachines() {
////        List<VirtualMachine> virtualMachines = virtualMachineService.findAllVirtualMachines();
////        return ResponseEntity.ok(virtualMachines);
////    }
////
////    /**
////     * Get a virtual machine by ID
////     * @param id The ID of the virtual machine
////     * @return The virtual machine if found
////     */
////    @GetMapping("/{id}")
////    public ResponseEntity<VirtualMachine> getVirtualMachineById(@PathVariable Integer id) {
////        Optional<VirtualMachine> virtualMachine = virtualMachineService.findVirtualMachineById(id);
////        return virtualMachine.map(ResponseEntity::ok)
////                .orElseGet(() -> ResponseEntity.notFound().build());
////    }
////
////    /**
////     * Create a new virtual machine
////     * @param virtualMachine The virtual machine to create
////     * @return The created virtual machine
////     */
////    @PostMapping("/create-vm")
////    public ResponseEntity<VirtualMachine> createVirtualMachine(@RequestBody VirtualMachine virtualMachine) {
////        VirtualMachine createdVirtualMachine = virtualMachineService.saveVirtualMachine(virtualMachine);
////        return ResponseEntity.status(HttpStatus.CREATED).body(createdVirtualMachine);
////    }
////
////    /**
////     * Update an existing virtual machine
////     * @param id The ID of the virtual machine to update
////     * @param virtualMachine The updated virtual machine data
////     * @return The updated virtual machine
////     */
////    @PutMapping("/{id}")
////    public ResponseEntity<VirtualMachine> updateVirtualMachine(@PathVariable Integer id, @RequestBody VirtualMachine virtualMachine) {
////        Optional<VirtualMachine> existingVirtualMachine = virtualMachineService.findVirtualMachineById(id);
////        if (existingVirtualMachine.isEmpty()) {
////            return ResponseEntity.notFound().build();
////        }
////
////        // Ensure the ID is set correctly
////        virtualMachine.setId(id);
////
////        VirtualMachine updatedVirtualMachine = virtualMachineService.saveVirtualMachine(virtualMachine);
////        return ResponseEntity.ok(updatedVirtualMachine);
////    }
////
////    /**
////     * Delete a virtual machine
////     * @param id The ID of the virtual machine to delete
////     * @return No content response
////     */
////    @DeleteMapping("/{id}")
////    public ResponseEntity<Void> deleteVirtualMachine(@PathVariable Integer id) {
////        Optional<VirtualMachine> existingVirtualMachine = virtualMachineService.findVirtualMachineById(id);
////        if (existingVirtualMachine.isEmpty()) {
////            return ResponseEntity.notFound().build();
////        }
////
////        virtualMachineService.deleteVirtualMachine(id);
////        return ResponseEntity.noContent().build();
////    }
////
////    /**
////     * Find virtual machines by operating system
////     * @param os The operating system to search for
////     * @return List of matching virtual machines
////     */
////    @GetMapping("/search/os/{os}")
////    public ResponseEntity<List<VirtualMachine>> findByOperatingSystem(@PathVariable String os) {
////        List<VirtualMachine> virtualMachines = virtualMachineService.findByOperatingSystem(os);
////        return ResponseEntity.ok(virtualMachines);
////    }
////
////    /**
////     * Find virtual machines with minimum RAM
////     * @param ram The minimum RAM value
////     * @return List of matching virtual machines
////     */
////    @GetMapping("/search/ram/{ram}")
////    public ResponseEntity<List<VirtualMachine>> findByMinimumRam(@PathVariable int ram) {
////        List<VirtualMachine> virtualMachines = virtualMachineService.findByRamGreaterThanEqual(ram);
////        return ResponseEntity.ok(virtualMachines);
////    }
////
////    /**
////     * Update IP address of a virtual machine
////     * @param id The ID of the virtual machine
////     * @param ipAddress The new IP address
////     * @return The updated virtual machine
////     */
////    @PatchMapping("/{id}/ip")
////    public ResponseEntity<VirtualMachine> updateIpAddress(@PathVariable Integer id, @RequestBody String ipAddress) {
////        Optional<VirtualMachine> updatedVm = virtualMachineService.updateIpAddress(id, ipAddress);
////        return updatedVm.map(ResponseEntity::ok)
////                .orElseGet(() -> ResponseEntity.notFound().build());
////    }
//}


import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import jesa.pfe.deploymentmanagement.services.VirtualMachineOrchestratorService;
import jesa.pfe.deploymentmanagement.services.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/vms")
public class VirtualMachineController {

    private final VirtualMachineOrchestratorService orchestratorService;
    private final VirtualMachineService vmService;

    @Autowired
    public VirtualMachineController(VirtualMachineOrchestratorService orchestratorService,
                                    VirtualMachineService vmService) {
        this.orchestratorService = orchestratorService;
        this.vmService = vmService;
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
}
