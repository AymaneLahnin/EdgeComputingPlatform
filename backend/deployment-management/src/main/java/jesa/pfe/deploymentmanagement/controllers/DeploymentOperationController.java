package jesa.pfe.deploymentmanagement.controllers;

import jesa.pfe.deploymentmanagement.entities.DeploymentOperation;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import jesa.pfe.deploymentmanagement.services.DeploymentOperationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/deployment-operations")
public class DeploymentOperationController {

    private final DeploymentOperationService deploymentOperationService;

    @Autowired
    public DeploymentOperationController(DeploymentOperationService deploymentOperationService) {
        this.deploymentOperationService = deploymentOperationService;
    }

    // Create a new deployment operation
    @PostMapping("/create-deployment")
    public ResponseEntity<DeploymentOperation> createDeploymentOperation(@RequestBody DeploymentOperation deploymentOperation) {
        DeploymentOperation createdOperation = deploymentOperationService.createDeploymentOperation(deploymentOperation);
        return new ResponseEntity<>(createdOperation, HttpStatus.CREATED);
    }

    // Get all deployment operations
    @GetMapping("/getAllDeployments")
    public ResponseEntity<List<DeploymentOperation>> getAllDeploymentOperations() {
        List<DeploymentOperation> operations = deploymentOperationService.getAllDeploymentOperations();
        return new ResponseEntity<>(operations, HttpStatus.OK);
    }

    // Get deployment operation by ID
    @GetMapping("/find-deployment/{id}")
    public ResponseEntity<DeploymentOperation> getDeploymentOperationById(@PathVariable int id) {
        try {
            DeploymentOperation operation = deploymentOperationService.getDeploymentOperationById(id);
            return new ResponseEntity<>(operation, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update a deployment operation
    @PutMapping("/update/{id}")
    public ResponseEntity<DeploymentOperation> updateDeploymentOperation(
            @PathVariable int id,
            @RequestBody DeploymentOperation deploymentOperation) {
        try {
            DeploymentOperation updatedOperation = deploymentOperationService.updateDeploymentOperation(id, deploymentOperation);
            return new ResponseEntity<>(updatedOperation, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete a deployment operation
    @DeleteMapping("/delete-deployment/{id}")
    public ResponseEntity<Void> deleteDeploymentOperation(@PathVariable int id) {
        try {
            deploymentOperationService.deleteDeploymentOperation(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}