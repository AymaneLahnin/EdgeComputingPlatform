package jesa.pfe.edgeappservice.repository;

import jesa.pfe.edgeappservice.entities.EdgeApp;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface EdgeAppRepository extends JpaRepository<EdgeApp, Integer> {
    Optional<EdgeApp> findByName(String name);


    List<EdgeApp> findByDeployableUnitName(String name);
}
