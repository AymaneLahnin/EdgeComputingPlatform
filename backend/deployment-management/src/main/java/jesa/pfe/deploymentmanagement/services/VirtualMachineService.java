package jesa.pfe.deploymentmanagement.services;

import jesa.pfe.deploymentmanagement.entities.DeployableUnit;
import jesa.pfe.deploymentmanagement.entities.DeploymentOperation;
import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import jesa.pfe.deploymentmanagement.repositories.VirtualMachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VirtualMachineService {

    private final VirtualMachineRepository virtualMachineRepository;
    private final DeploymentOperationService deploymentOperationService;

    @Autowired
    public VirtualMachineService(VirtualMachineRepository virtualMachineRepository,
                                 DeploymentOperationService deploymentOperationService) {
        this.virtualMachineRepository = virtualMachineRepository;
        this.deploymentOperationService = deploymentOperationService;
    }

    /**
     * Find all virtual machines
     * @return List of all virtual machines
     */
    public List<VirtualMachine> findAllVirtualMachines() {
        return virtualMachineRepository.findAll().stream()
                .filter(unit -> unit instanceof VirtualMachine)
                .map(unit -> (VirtualMachine) unit)
                .collect(Collectors.toList());
    }

    /**
     * Find a virtual machine by ID
     * @param id The ID of the virtual machine
     * @return The virtual machine if found
     */
    public Optional<VirtualMachine> findVirtualMachineById(Integer id) {
        Optional<DeployableUnit> unit = virtualMachineRepository.findById(id);
        if (unit.isPresent() && unit.get() instanceof VirtualMachine) {
            return Optional.of((VirtualMachine) unit.get());
        }
        return Optional.empty();
    }

    /**
     * Save a new virtual machine or update an existing one,
     * and register a deployment operation if it's a new VM
     * @param virtualMachine The virtual machine to save
     * @return The saved virtual machine
     */
    @Transactional
    public VirtualMachine saveVirtualMachine(VirtualMachine virtualMachine, DeploymentType deploymentType) {
        boolean isNewVM = virtualMachine.getName() == null || !virtualMachineRepository.existsByName(virtualMachine.getName());

        if (virtualMachine.getCreatedAt() == null) {
            virtualMachine.setCreatedAt(LocalDateTime.now());
        }

        VirtualMachine savedVM = (VirtualMachine) virtualMachineRepository.save(virtualMachine);

        // Create and register a deployment operation for new VMs
        if (isNewVM) {
            registerDeploymentOperation(savedVM,deploymentType);

        }

        return savedVM;
    }

    /**
     * Register a deployment operation for a new virtual machine
     * @param virtualMachine The virtual machine for which to register a deployment operation
     */
    private void registerDeploymentOperation(VirtualMachine virtualMachine, DeploymentType deploymentType) {
        DeploymentOperation deploymentOperation = new DeploymentOperation();
        deploymentOperation.setDeployment_date(LocalDateTime.now());
        deploymentOperation.setDeployment_option(deploymentType);
        deploymentOperation.setDeployableUnit(virtualMachine);


        // Save the deployment operation
        deploymentOperationService.createDeploymentOperation(deploymentOperation);
    }

    /**
     * Delete a virtual machine by ID
     * @param id The ID of the virtual machine to delete
     */
    public void deleteVirtualMachine(Integer id) {
        virtualMachineRepository.deleteById(id);
    }

    /**
     * Custom method to find virtual machines by operating system
     * @param os The operating system to search for
     * @return List of virtual machines with the specified operating system
     */
    public List<VirtualMachine> findByOperatingSystem(String os) {
        return virtualMachineRepository.findByOperatingSystem(os);
    }

    /**
     * Custom method to find virtual machines with RAM greater than or equal to specified value
     * @param ram The minimum RAM value
     * @return List of virtual machines with sufficient RAM
     */
    public List<VirtualMachine> findByRamGreaterThanEqual(int ram) {
        return virtualMachineRepository.findByRamGreaterThanEqual(ram);
    }

    /**
     * Custom method to update virtual machine IP address
     * @param id The ID of the virtual machine
     * @param ipAddress The new IP address
     * @return Updated virtual machine or empty if not found
     */
    public Optional<VirtualMachine> updateIpAddress(Integer id, String ipAddress) {
        Optional<DeployableUnit> unitOpt = virtualMachineRepository.findById(id);
        if (unitOpt.isPresent() && unitOpt.get() instanceof VirtualMachine) {
            VirtualMachine virtualMachine = (VirtualMachine) unitOpt.get();
            virtualMachine.setIpAddress(ipAddress);
            return Optional.of((VirtualMachine) virtualMachineRepository.save(virtualMachine));
        }
        return Optional.empty();
    }
}