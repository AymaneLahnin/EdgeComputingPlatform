package jesa.pfe.deploymentmanagement.controllers;

import jesa.pfe.deploymentmanagement.services.VmNetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing virtual machine network operations
 * Provides endpoints for IP address detection and updates
 */
@RestController
@RequestMapping("/api/vm/network")
public class VmNetworkController {

    private static final Logger logger = LoggerFactory.getLogger(VmNetworkController.class);
    private final VmNetworkService vmNetworkService;


    @Autowired
    public VmNetworkController(VmNetworkService vmNetworkService) {
        this.vmNetworkService = vmNetworkService;
    }

    /**
     * Detect and update IP address for a virtual machine
     * @param vmId ID of the virtual machine
     * @return Success status and message
     */
    @PostMapping("/detect-ip/{vmId}")
    public ResponseEntity<Map<String, Object>> detectAndUpdateVmIp(@PathVariable Integer vmId) {
        logger.info("Request received to detect and update IP for VM ID: {}", vmId);
        Map<String, Object> response = new HashMap<>();

        boolean success = vmNetworkService.detectAndUpdateVmIpAddress(vmId);

        if (success) {
            response.put("status", "success");
            response.put("message", "VM IP address detected and updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Failed to detect or update VM IP address");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Batch detect and update IP addresses for multiple virtual machines
     * @param vmIds List of virtual machine IDs
     * @return Results of the operation for each VM
     */
    @PostMapping("/detect-ip/batch")
    public ResponseEntity<Map<String, Object>> batchDetectAndUpdateVmIp(@RequestBody Integer[] vmIds) {
        logger.info("Request received to batch detect and update IP for {} VMs", vmIds.length);
        Map<String, Object> response = new HashMap<>();
        Map<Integer, Boolean> results = new HashMap<>();

        int successCount = 0;

        for (Integer vmId : vmIds) {
            boolean success = vmNetworkService.detectAndUpdateVmIpAddress(vmId);
            results.put(vmId, success);
            if (success) {
                successCount++;
            }
        }

        response.put("status", successCount == vmIds.length ? "success" : "partial");
        response.put("successCount", successCount);
        response.put("totalCount", vmIds.length);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for the VM network service
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "VmNetworkService");
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }
}