package jesa.pfe.edgeserverservice.controller;



import jesa.pfe.edgeserverservice.entity.EdgeServer;
import jesa.pfe.edgeserverservice.sharedmodels.EdgeVmsResponse;
import jesa.pfe.edgeserverservice.services.EdgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/edge-servers")
@RequiredArgsConstructor
public class EdgeServerController {

    private final EdgeService edgeServerService;

    /**
     * Create a new edge server
     * @param edgeServer The edge server to create
     * @return The created edge server
     */
    @PostMapping("/registerEdgeServer")
    public ResponseEntity<EdgeServer> createEdgeServer(@RequestBody EdgeServer edgeServer) {
        EdgeServer createdEdgeServer = edgeServerService.registerEdgeServer(edgeServer);
        return new ResponseEntity<>(createdEdgeServer, HttpStatus.CREATED);
    }

    /**
     * Get all edge servers
     * @return List of all edge servers
     */
    @GetMapping
    public ResponseEntity<List<EdgeServer>> getAllEdgeServers() {
        List<EdgeServer> edgeServers = edgeServerService.getAllEdgeServers();
        return new ResponseEntity<>(edgeServers, HttpStatus.OK);
    }
    @GetMapping("/find-servers-byVmName/{vmName}")
    public ResponseEntity<List<EdgeServer>> getEdgeServersByvmName(@PathVariable String vmName) {
        List<EdgeServer> edgeServers = edgeServerService.getAllEdgeServersByvmName(vmName);
        return new ResponseEntity<>(edgeServers, HttpStatus.OK);
    }

    /***
     * handle edge server vms association
     * @param edgeServerName
     * @return
     */

    @GetMapping("/find-edgeServer-vms/{edgeServerName}")
    public ResponseEntity<EdgeVmsResponse> getEdgeServerVms(@PathVariable String edgeServerName) {
        EdgeVmsResponse edgeVmsResponse = edgeServerService.findEdgeServerwithVms(edgeServerName);
        return new ResponseEntity<>(edgeVmsResponse, HttpStatus.OK);
    }

    /**
     * Get an edge server by ID
     * @param id The ID of the edge server
     * @return The edge server if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<EdgeServer> getEdgeServerById(@PathVariable int id) {
        Optional<EdgeServer> edgeServer = edgeServerService.getEdgeServerById(id);

        return edgeServer
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Update an existing edge server
     * @param id The ID of the edge server to update
     * @param edgeServerDetails The updated edge server details
     * @return The updated edge server
     */
    @PutMapping("/{id}")
    public ResponseEntity<EdgeServer> updateEdgeServer(@PathVariable int id, @RequestBody EdgeServer edgeServerDetails) {
        EdgeServer updatedEdgeServer = edgeServerService.updateEdgeServer(id, edgeServerDetails);

        if (updatedEdgeServer != null) {
            return new ResponseEntity<>(updatedEdgeServer, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Delete an edge server
     * @param id The ID of the edge server to delete
     * @return No content if successful, not found if the edge server doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEdgeServer(@PathVariable int id) {
        boolean deleted = edgeServerService.deleteEdgeServer(id);

        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}