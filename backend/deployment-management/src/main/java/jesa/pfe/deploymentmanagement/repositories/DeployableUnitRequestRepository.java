package jesa.pfe.deploymentmanagement.repositories;

import jesa.pfe.deploymentmanagement.entities.DeployableUnitRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface DeployableUnitRequestRepository extends JpaRepository<DeployableUnitRequest, Integer> {
}
