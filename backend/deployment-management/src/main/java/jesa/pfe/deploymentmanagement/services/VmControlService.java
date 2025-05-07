package jesa.pfe.deploymentmanagement.services;


import jesa.pfe.deploymentmanagement.entities.DeployableUnit;
import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import jesa.pfe.deploymentmanagement.enums.VMOperationStatus;
import jesa.pfe.deploymentmanagement.repositories.VirtualMachineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


import java.util.Optional;

@Service
public class VmControlService {
    private static final Logger logger = LoggerFactory.getLogger(VmControlService.class);
    private static final String VBOXMANAGE_CMD = "VBoxManage";
    private static final int OPERATION_TIMEOUT = 60; // seconds

    private final VirtualMachineRepository virtualMachineRepository;
    private final VmNetworkService vmNetworkService;


    @Autowired
    public VmControlService(VirtualMachineRepository virtualMachineRepository, VmNetworkService vmNetworkService) {
        this.virtualMachineRepository = virtualMachineRepository;
        this.vmNetworkService = vmNetworkService;
    }

//    public VMOperationStatus startVM(String vmName) {
//        Optional<VirtualMachine> vmOpt = virtualMachineRepository.findByName(vmName);
//
//        if (vmOpt.isEmpty()) {
//            logger.error("VM with ID {} not found in database", vmName);
//            return VMOperationStatus.VM_NOT_FOUND;
//        }
//
//        VirtualMachine vm = vmOpt.get();
//        logger.info("Starting VM: {}", vm.getName());
//
//        String[] command = {VBOXMANAGE_CMD, "startvm", vm.getName(), "--type", "headless"};
//
//
//        return executeVBoxCommand(command);
//
//    }
//v2
public VMOperationStatus startVM(String vmName) {
    Optional<VirtualMachine> vmOpt = virtualMachineRepository.findByName(vmName);

    if (vmOpt.isEmpty()) {
        logger.error("VM with ID {} not found in database", vmName);
        return VMOperationStatus.VM_NOT_FOUND;
    }

    VirtualMachine vm = vmOpt.get();
    logger.info("Starting VM: {}", vm.getName());

    String[] command = {VBOXMANAGE_CMD, "startvm", vm.getName(), "--type", "headless"};
    VMOperationStatus result = executeVBoxCommand(command);

    // Refresh status after executing command
    String updatedStatus = getVMStatus(vm.getName());
    vm.setStatus(updatedStatus);
    virtualMachineRepository.save(vm);
    System.out.println("angelo:" + vm.getStatus());

    return result;
}


public VMOperationStatus stopVM(String vmName) {
    Optional<VirtualMachine> vmOpt = virtualMachineRepository.findByName(vmName);

    if (vmOpt.isEmpty()) {
        logger.error("VM with ID {} not found in database", vmName);
        return VMOperationStatus.VM_NOT_FOUND;
    }

    VirtualMachine vm = vmOpt.get();
    logger.info("Stopping VM: {}", vm.getName());

    String[] command = {VBOXMANAGE_CMD, "controlvm", vm.getName(), "acpipowerbutton"};
    VMOperationStatus status = executeVBoxCommand(command);

    if (status != VMOperationStatus.SUCCESS && status != VMOperationStatus.VM_ALREADY_IN_DESIRED_STATE) {
        return status;
    }

    // Polling loop: wait up to 60 seconds for shutdown
    int maxTries = 12;
    int delaySeconds = 5;

    for (int i = 0; i < maxTries; i++) {
        String currentStatus = getVMStatus(vm.getName());
        logger.info("Polling shutdown: attempt {}, currentStatus={}", i + 1, currentStatus);

        if ("poweroff".equalsIgnoreCase(currentStatus)) {
            vm.setStatus("poweredoff");
            virtualMachineRepository.save(vm);
            return VMOperationStatus.SUCCESS;
        }

        try {
            Thread.sleep(delaySeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return VMOperationStatus.ERROR;
        }
    }

    logger.warn("VM did not shut down in expected time");
    return VMOperationStatus.FAILED;
}

//
//    public VMOperationStatus forceStopVM(String vmName) {
//        Optional<VirtualMachine> vmOpt = virtualMachineRepository.findByName(vmName);
//
//        if (vmOpt.isEmpty()) {
//            logger.error("VM with ID {} not found in database", vmName);
//            return VMOperationStatus.VM_NOT_FOUND;
//        }
//
//        VirtualMachine vm = vmOpt.get();
//        logger.info("Force stopping VM: {}", vm.getName());
//
//        String[] command = {VBOXMANAGE_CMD, "controlvm", vm.getName(), "poweroff"};
//
//        vm.setStatus(getVMStatus(vm.getName()));
//        virtualMachineRepository.save(vm);
//
//        return executeVBoxCommand(command);
//    }

    public VMOperationStatus forceStopVM(String vmName) {
        Optional<VirtualMachine> vmOpt = virtualMachineRepository.findByName(vmName);

        if (vmOpt.isEmpty()) {
            logger.error("VM with ID {} not found in database", vmName);
            return VMOperationStatus.VM_NOT_FOUND;
        }

        VirtualMachine vm = vmOpt.get();
        logger.info("Force stopping VM: {}", vm.getName());

        String[] command = {VBOXMANAGE_CMD, "controlvm", vm.getName(), "poweroff"};

        VMOperationStatus result = executeVBoxCommand(command);

        // Optional: wait a short period to allow shutdown to complete
        try {
            Thread.sleep(2000); // 2 seconds; tweak based on observation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while waiting after force stop", e);
        }

        // Refresh status after poweroff
        vm.setStatus(getVMStatus(vm.getName()));
        virtualMachineRepository.save(vm);

        return result;
    }


    public VMOperationStatus deleteVM(String vmName) {
        Optional<VirtualMachine> vmOpt = virtualMachineRepository.findByName(vmName);

        if (vmOpt.isEmpty()) {
            logger.error("VM with ID {} not found in database", vmName);
            return VMOperationStatus.VM_NOT_FOUND;
        }

        VirtualMachine vm = vmOpt.get();
        logger.info("Deleting VM: {}", vm.getName());

        // First ensure the VM is powered off
        VMOperationStatus powerOffStatus = forceStopVM(vmName);
        if (powerOffStatus != VMOperationStatus.SUCCESS && powerOffStatus != VMOperationStatus.VM_ALREADY_IN_DESIRED_STATE) {
            logger.warn("Could not power off VM before deletion, attempting to delete anyway");
        }

        // Then delete the VM with all its files
        String[] command = {VBOXMANAGE_CMD, "unregistervm", vm.getName(), "--delete"};
        VMOperationStatus deleteStatus = executeVBoxCommand(command);

        // If successful, also remove from the database
        if (deleteStatus == VMOperationStatus.SUCCESS) {
            virtualMachineRepository.deleteByName(vmName);
            logger.info("VM with ID {} deleted from database", vmName);
        }

        return deleteStatus;
    }

    public String getVMStatus(String vmName) {
        Optional<VirtualMachine> vmOpt = virtualMachineRepository.findByName(vmName);

        if (vmOpt.isEmpty()) {
            return "VM not found";
        }

        VirtualMachine vm = vmOpt.get();
        logger.info("Getting status for VM: {}", vm.getName());

        String[] command = {VBOXMANAGE_CMD, "showvminfo", vm.getName(), "--machinereadable"};

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            String vmState = "unknown";

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("VMState=")) {
                    vmState = line.substring(9).replace("\"", "");
                    break;
                }
            }

            boolean processCompleted = process.waitFor(OPERATION_TIMEOUT, TimeUnit.SECONDS);
            if (!processCompleted) {
                process.destroyForcibly();
                logger.error("Command timed out: {}", String.join(" ", command));
                return "Operation timed out";
            }
            vm.setStatus(vmState);

            return vmState;

        } catch (IOException | InterruptedException e) {
            logger.error("Error executing VBoxManage command", e);
            return "Error: " + e.getMessage();
        }
    }







    private VMOperationStatus executeVBoxCommand(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            // Handle output and error streams asynchronously
            CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Error reading process output", e);
                }
            });

            CompletableFuture<Void> errorFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("Error reading process error", e);
                }
            });

            // Wait for the process to complete with timeout
            boolean processCompleted = process.waitFor(OPERATION_TIMEOUT, TimeUnit.SECONDS);

            // Wait for stream processing to complete
            CompletableFuture.allOf(outputFuture, errorFuture).get(5, TimeUnit.SECONDS);

            if (!processCompleted) {
                process.destroyForcibly();
                logger.error("Command timed out: {}", String.join(" ", command));
                return VMOperationStatus.TIMEOUT;
            }

            int exitCode = process.exitValue();

            if (exitCode == 0) {
                logger.info("Command executed successfully: {}", String.join(" ", command));
                return VMOperationStatus.SUCCESS;
            } else {
                logger.error("Command failed with exit code {}: {}", exitCode, String.join(" ", command));
                logger.error("Error output: {}", error.toString().trim());

                if (error.toString().contains("is already powered off")) {
                    return VMOperationStatus.VM_ALREADY_IN_DESIRED_STATE;
                }
                if (error.toString().contains("is already running")) {
                    return VMOperationStatus.VM_ALREADY_IN_DESIRED_STATE;
                }
                if (error.toString().contains("Could not find a registered machine")) {
                    return VMOperationStatus.VM_NOT_FOUND_IN_VIRTUALBOX;
                }

                return VMOperationStatus.FAILED;
            }

        } catch (Exception e) {
            logger.error("Error executing VBoxManage command", e);
            return VMOperationStatus.ERROR;
        }
    }

}
