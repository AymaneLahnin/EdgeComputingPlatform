package jesa.pfe.deploymentmanagement.repositories;

import jesa.pfe.deploymentmanagement.entities.DeployableUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployableUnitRepository extends JpaRepository<DeployableUnit, Integer> {
}
