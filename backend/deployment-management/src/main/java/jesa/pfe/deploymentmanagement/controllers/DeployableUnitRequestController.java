package jesa.pfe.deploymentmanagement.controllers;


import jakarta.persistence.EntityNotFoundException;
import jesa.pfe.deploymentmanagement.entities.DeployableUnitRequest;
import jesa.pfe.deploymentmanagement.services.DURequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/deployable-unit-requests")
public class DeployableUnitRequestController {

    private final DURequestService service;

    @Autowired
    public DeployableUnitRequestController(DURequestService service) {
        this.service = service;
    }

    @GetMapping("/all-requests")
    public List<DeployableUnitRequest> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeployableUnitRequest> getById(@PathVariable int id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create-request")
    public ResponseEntity<DeployableUnitRequest> create( @RequestBody DeployableUnitRequest request) {
        DeployableUnitRequest created = service.create(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeployableUnitRequest> update(@PathVariable int id,
                                                         @RequestBody DeployableUnitRequest request) {
        try {
            DeployableUnitRequest updated = service.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
