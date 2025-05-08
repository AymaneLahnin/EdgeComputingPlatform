package jesa.pfe.edgeappservice.controllers;


import jesa.pfe.edgeappservice.entities.EdgeApp;
import jesa.pfe.edgeappservice.services.EdgeAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edge-apps")
@RequiredArgsConstructor
public class EdgeAppController {

    private final EdgeAppService edgeAppService;

    /**
     * Get all edge applications
     * @return List of all edge applications
     */
    @GetMapping("/find-all-apps")
    public ResponseEntity<List<EdgeApp>> getAllEdgeApps() {
        List<EdgeApp> edgeApps = edgeAppService.getAllEdgeApps();
        return new ResponseEntity<>(edgeApps, HttpStatus.OK);
    }
    // find edge Apps by deployableUnits
    @GetMapping("/find-all-apps-byDu/{name}")
    public ResponseEntity<List<EdgeApp>> getAllEdgeAppsByDuName(@PathVariable String name) {
        List<EdgeApp> edgeApps = edgeAppService.getAllEdgeAppsByDeployableUnitName(name);
        return new ResponseEntity<>(edgeApps, HttpStatus.OK);
    }


    /**
     * Get edge application by ID
     * @param id ID of the edge application
     * @return Edge application with the given ID
     */
    @GetMapping("/find-edgeApp-ById/{id}")
    public ResponseEntity<EdgeApp> getEdgeAppById(@PathVariable Integer id) {
        EdgeApp edgeApp = edgeAppService.getEdgeAppById(id);
        return new ResponseEntity<>(edgeApp, HttpStatus.OK);
    }

    /**
     * Get edge application by name
     * @param name Name of the edge application
     * @return Edge application with the given name
     */
    @GetMapping("/find-edgeApp-ByName/{name}")
    public ResponseEntity<EdgeApp> getEdgeAppByName(@PathVariable String name) {
        EdgeApp edgeApp = edgeAppService.getEdgeAppByName(name);
        return new ResponseEntity<>(edgeApp, HttpStatus.OK);
    }

    /**
     * Create a new edge application
     * @param edgeApp Edge application to create
     * @return Created edge application
     */
    @PostMapping("/create")
    public ResponseEntity<EdgeApp> createEdgeApp(@RequestBody EdgeApp edgeApp) {
        EdgeApp createdEdgeApp = edgeAppService.createEdgeApp(edgeApp);
        return new ResponseEntity<>(createdEdgeApp, HttpStatus.CREATED);
    }

    /**
     * Update an existing edge application by ID
     * @param id ID of the edge application to update
     * @param edgeAppDetails Updated edge application details
     * @return Updated edge application
     */
    @PutMapping("/updateById/{id}")
    public ResponseEntity<EdgeApp> updateEdgeApp(@PathVariable Integer id,  @RequestBody EdgeApp edgeAppDetails) {
        EdgeApp updatedEdgeApp = edgeAppService.updateEdgeApp(id, edgeAppDetails);
        return new ResponseEntity<>(updatedEdgeApp, HttpStatus.OK);
    }

    /**
     * Update an existing edge application by name
     * @param name Name of the edge application to update
     * @param edgeAppDetails Updated edge application details
     * @return Updated edge application
     */
    @PutMapping("/updateByName/{name}")
    public ResponseEntity<EdgeApp> updateEdgeAppByName(@PathVariable String name,@RequestBody EdgeApp edgeAppDetails) {
        EdgeApp updatedEdgeApp = edgeAppService.updateEdgeAppByName(name, edgeAppDetails);
        return new ResponseEntity<>(updatedEdgeApp, HttpStatus.OK);
    }

    /**
     * Delete an edge application by ID
     * @param id ID of the edge application to delete
     * @return No content response
     */
    @DeleteMapping("delete-byId/{id}")
    public ResponseEntity<Void> deleteEdgeApp(@PathVariable Integer id) {
        edgeAppService.deleteEdgeApp(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Delete an edge application by name
     * @param name Name of the edge application to delete
     * @return No content response
     */
    @DeleteMapping("/delete-byName/{name}")
    public ResponseEntity<Void> deleteEdgeAppByName(@PathVariable String name) {
        edgeAppService.deleteEdgeAppByName(name);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}