package jesa.pfe.edgeserverservice.services;

import jesa.pfe.edgeserverservice.clients.VmsClients;
import jesa.pfe.edgeserverservice.entity.EdgeServer;
import jesa.pfe.edgeserverservice.sharedmodels.EdgeVmsResponse;
import jesa.pfe.edgeserverservice.repository.EdgeServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EdgeService {

    private final EdgeServerRepository edgeServerRepository;
    private final VmsClients vmsClients;

    /**
     * Create a new edge server
     * @param edgeServer The edge server to create
     * @return The created edge server
     */
    public EdgeServer registerEdgeServer(EdgeServer edgeServer) {
        return edgeServerRepository.save(edgeServer);
    }

    /**
     * Get all edge servers
     * @return List of all edge servers
     */
    public List<EdgeServer> getAllEdgeServers() {
        return edgeServerRepository.findAll();
    }

    /**
     * Get an edge server by ID
     * @param id The ID of the edge server
     * @return The edge server if found
     */
    public Optional<EdgeServer> getEdgeServerById(int id) {
        return edgeServerRepository.findById(id);
    }

    /**
     * get edge server by
     * @param edgeServerName
     */
    public Optional<EdgeServer> getEdgeServerByName(String edgeServerName) {
        return edgeServerRepository.findByEdgeServerName(edgeServerName);
    }

    /**
     * Update an existing edge server
     * @param id The ID of the edge server to update
     * @param edgeServerDetails The updated edge server details
     * @return The updated edge server, or null if not found
     */
    public EdgeServer updateEdgeServer(int id, EdgeServer edgeServerDetails) {
        Optional<EdgeServer> edgeServer = edgeServerRepository.findById(id);

        if (edgeServer.isPresent()) {
            EdgeServer existingEdgeServer = edgeServer.get();
            existingEdgeServer.setEdgeServerName(edgeServerDetails.getEdgeServerName());
            existingEdgeServer.setLocation(edgeServerDetails.getLocation());
            existingEdgeServer.setVirtualizationStatus(edgeServerDetails.isVirtualizationStatus());

            return edgeServerRepository.save(existingEdgeServer);
        }

        return null;
    }

    /**
     * Delete an edge server
     * @param id The ID of the edge server to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteEdgeServer(int id) {
        Optional<EdgeServer> edgeServer = edgeServerRepository.findById(id);

        if (edgeServer.isPresent()) {
            edgeServerRepository.deleteById(id);
            return true;
        }

        return false;
    }

    public EdgeVmsResponse findEdgeServerwithVms(String edgeServerName) {
        var edgeServer=edgeServerRepository.findByEdgeServerName(edgeServerName)
                .orElse(
                EdgeServer.builder().edgeServerName("the vm doesn't exist").build()
        );
        var vmsResponse= vmsClients.getVirtualMachinesByServer(edgeServerName);
        return EdgeVmsResponse.builder()
                .edgeServerName(edgeServerName)
                .location(edgeServer.getLocation())
                .virtualizationStatus(edgeServer.isVirtualizationStatus())
                .virtualMachines(vmsResponse).build();
    }

    public List<EdgeServer> getAllEdgeServersByvmName(String vmName) {
        return edgeServerRepository.findByVmName(vmName);
    }
}