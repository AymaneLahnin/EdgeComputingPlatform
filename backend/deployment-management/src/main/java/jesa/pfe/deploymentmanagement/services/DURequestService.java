package jesa.pfe.deploymentmanagement.services;



import jakarta.persistence.EntityNotFoundException;
import jesa.pfe.deploymentmanagement.entities.DeployableUnitRequest;
import jesa.pfe.deploymentmanagement.repositories.DeployableUnitRequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DURequestService {

    private final DeployableUnitRequestRepository repository;

    @Autowired
    public DURequestService(DeployableUnitRequestRepository repository) {
        this.repository = repository;
    }


    public List<DeployableUnitRequest> findAll() {
        return repository.findAll();
    }


    public Optional<DeployableUnitRequest> findById(int id) {
        return repository.findById(id);
    }


    public DeployableUnitRequest create(DeployableUnitRequest request) {
        request.setId(0);
        request.setRequestDate(LocalDateTime.now());
        return repository.save(request);
    }


    public DeployableUnitRequest update(int id, DeployableUnitRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setRequestDate(request.getRequestDate());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new EntityNotFoundException("DeployableUnitRequest not found for id " + id));
    }


    public void delete(int id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("DeployableUnitRequest not found for id " + id);
        }
        repository.deleteById(id);
    }
}
