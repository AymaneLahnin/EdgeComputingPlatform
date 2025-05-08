package jesa.pfe.edgeserverservice.repository;

import jesa.pfe.edgeserverservice.entity.EdgeServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EdgeServerRepository extends JpaRepository<EdgeServer, Integer> {

    Optional<EdgeServer> findByEdgeServerName(String edgeServerName);

    List<EdgeServer> findByVmName(String vmName);
}