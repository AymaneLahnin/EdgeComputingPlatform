package jesa.pfe.deploymentmanagement.repositories;

import jesa.pfe.deploymentmanagement.entities.DeployableUnit;
import jesa.pfe.deploymentmanagement.entities.DeploymentOperation;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeploymentOperationRepository extends JpaRepository<DeploymentOperation, Integer> {

}