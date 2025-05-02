package jesa.pfe.deploymentmanagement.services;


import jesa.pfe.deploymentmanagement.entities.DeploymentOperation;
import jesa.pfe.deploymentmanagement.entities.DeployableUnit;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import jesa.pfe.deploymentmanagement.repositories.DeploymentOperationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeploymentOperationService {

    private final DeploymentOperationRepository deploymentOperationRepository;

    @Autowired
    public DeploymentOperationService(DeploymentOperationRepository deploymentOperationRepository) {
        this.deploymentOperationRepository = deploymentOperationRepository;
    }

    // Create a new deployment operation
    public DeploymentOperation createDeploymentOperation(DeploymentOperation deploymentOperation) {
        return deploymentOperationRepository.save(deploymentOperation);
    }

    // Get all deployment operations
    public List<DeploymentOperation> getAllDeploymentOperations() {
        return deploymentOperationRepository.findAll();
    }

    // Get deployment operation by ID
    public DeploymentOperation getDeploymentOperationById(int id) {
        return deploymentOperationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Deployment operation not found with id: " + id));
    }

    // Update a deployment operation
    public DeploymentOperation updateDeploymentOperation(int id, DeploymentOperation updatedOperation) {
        DeploymentOperation existingOperation = getDeploymentOperationById(id);

        // Update fields
        existingOperation.setDeployment_date(updatedOperation.getDeployment_date());
        existingOperation.setDeployment_option(updatedOperation.getDeployment_option());
        existingOperation.setDeployableUnit(updatedOperation.getDeployableUnit());

        return deploymentOperationRepository.save(existingOperation);
    }

    // Delete a deployment operation
    public void deleteDeploymentOperation(int id) {
        if (!deploymentOperationRepository.existsById(id)) {
            throw new EntityNotFoundException("Deployment operation not found with id: " + id);
        }
        deploymentOperationRepository.deleteById(id);
    }

}