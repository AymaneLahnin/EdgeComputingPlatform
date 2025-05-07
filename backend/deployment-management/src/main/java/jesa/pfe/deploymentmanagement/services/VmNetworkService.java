package jesa.pfe.deploymentmanagement.services;

import jesa.pfe.deploymentmanagement.entities.DeployableUnit;
import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import jesa.pfe.deploymentmanagement.repositories.VirtualMachineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VmNetworkService {

    private static final Logger logger = LoggerFactory.getLogger(VmNetworkService.class);
    private static final String VBOXMANAGE_CMD = "VBoxManage";
    private static final int OPERATION_TIMEOUT = 30; // seconds
    private static final int IP_DETECTION_MAX_ATTEMPTS = 10;
    private static final int IP_DETECTION_WAIT_TIME = 3000; // milliseconds

    private final VirtualMachineRepository virtualMachineRepository;

    @Autowired
    public VmNetworkService(VirtualMachineRepository virtualMachineRepository) {
        this.virtualMachineRepository = virtualMachineRepository;
    }

    /**
     * Find and update IP address for a VM after it starts
     * @param vmId ID of the virtual machine
     * @return true if IP was detected and updated successfully, false otherwise
     */
    public boolean detectAndUpdateVmIpAddress(Integer vmId) {
        Optional<DeployableUnit> unitOpt = virtualMachineRepository.findById(vmId);

        if (unitOpt.isEmpty() || !(unitOpt.get() instanceof VirtualMachine)) {
            logger.error("VM with ID {} not found in database", vmId);
            return false;
        }

        VirtualMachine vm = (VirtualMachine) unitOpt.get();
        logger.info("Detecting IP address for VM: {}", vm.getName());

        // First verify VM exists and is running in VirtualBox before attempting IP detection
        if (!isVmRunning(vm.getName())) {
            logger.warn("VM '{}' is not running or not found in VirtualBox. Skipping IP detection.", vm.getName());
            return false;
        }

        String detectedIp = detectVmIpAddress(vm.getName());

        System.out.println(">>> Detected IP before DB check: " + detectedIp);


        if (detectedIp == null) {
            logger.warn("Could not detect IP address for VM: {}", vm.getName());
            return false;
        }

        // If IP hasn't changed, no need to update
        if (detectedIp.equals(vm.getIpAddress())) {
            logger.info("IP address for VM '{}' remains unchanged: {}", vm.getName(), detectedIp);
            return true;
        }

        // Update IP address in the database
        vm.setIpAddress(detectedIp);
        virtualMachineRepository.save(vm);

        logger.info("Updated IP address for VM '{}' to '{}'",
                vm.getName(), detectedIp);
        return true;
    }

    /**
     * Check if a VM exists and is running in VirtualBox
     * @param vmName Name of the virtual machine
     * @return true if the VM exists and is running, false otherwise
     */
    private boolean isVmRunning(String vmName) {
        String[] command = {VBOXMANAGE_CMD, "showvminfo", vmName, "--machinereadable"};

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            String vmState = null;

            // Read output
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("VMState=")) {
                    vmState = line.substring(9).replace("\"", "");
                    break;
                }
            }

            // Wait for process to complete
            boolean processCompleted = process.waitFor(OPERATION_TIMEOUT, TimeUnit.SECONDS);
            if (!processCompleted) {
                process.destroyForcibly();
                logger.error("Command timed out: {}", String.join(" ", command));
                return false;
            }

            int exitCode = process.exitValue();

            // If command failed, log error output
            if (exitCode != 0) {
                StringBuilder errorOutput = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                logger.error("VM check failed with exit code {}: {}", exitCode, errorOutput.toString().trim());
                return false;
            }

            // Check if VM is running
            return vmState != null && vmState.equals("running");

        } catch (IOException | InterruptedException e) {
            logger.error("Error checking VM status", e);
            return false;
        }
    }

    /**
     * Detect the IP address of a VM using multiple methods
     * @param vmName Name of the virtual machine
     * @return The detected IP address or null if none found
     */
    private String detectVmIpAddress(String vmName) {
        String ipAddress = null;

        for (int attempt = 1; attempt <= IP_DETECTION_MAX_ATTEMPTS; attempt++) {
            logger.debug("IP detection attempt {} for VM '{}'", attempt, vmName);

            //ipAddress = getVmIpFromGuestProperty(vmName);

//            if (ipAddress == null) {
//                ipAddress = getVmIpSimple(vmName); // fallback
//            }
            ipAddress = getVmIpSimple(vmName);
            System.out.println(ipAddress);
            if (ipAddress != null && !ipAddress.isEmpty() && !ipAddress.equals("0.0.0.0")) {
                logger.info("Detected IP address for VM '{}': {}", vmName, ipAddress);
                return ipAddress;
            }

            try {
                Thread.sleep(IP_DETECTION_WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for IP detection", e);
                break;
            }
        }

        logger.warn("Failed to detect IP address for VM '{}' after {} attempts", vmName, IP_DETECTION_MAX_ATTEMPTS);
        return null;
    }



    /**
     * Fallback method: Get VM IP using simplified guestproperty get command
     */
    private String getVmIpSimple(String vmName) {
        String[] command = {
                VBOXMANAGE_CMD, "guestproperty", "get", vmName, "/VirtualBox/GuestInfo/Net/1/V4/IP"
        };

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine();
                process.waitFor();

                if (output != null && output.startsWith("Value:")) {
                    String ip = output.split(":")[1].trim();
                    if (!ip.equals("0.0.0.0") && !ip.equals("127.0.0.1") && !ip.startsWith("169.254.")) {
                        System.out.println("this is the new IP address: " + ip);
                        return ip;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Simple IP detection failed for VM '{}': {}", vmName, e.getMessage());
        }

        return null;
    }


}