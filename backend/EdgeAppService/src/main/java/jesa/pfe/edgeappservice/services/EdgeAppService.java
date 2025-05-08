package jesa.pfe.edgeappservice.services;

import jesa.pfe.edgeappservice.entities.EdgeApp;
import jesa.pfe.edgeappservice.exceptions.ResourceNotFoundException;
import jesa.pfe.edgeappservice.repository.EdgeAppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EdgeAppService {

    private final EdgeAppRepository edgeAppRepository;

    /**
     * Get all edge applications
     * @return List of all edge applications
     */
    public List<EdgeApp> getAllEdgeApps() {
        return edgeAppRepository.findAll();
    }

    /**
     * Get edge application by ID
     * @param id ID of the edge application
     * @return Edge application with the given ID
     * @throws ResourceNotFoundException if edge application is not found
     */
    public EdgeApp getEdgeAppById(Integer id) {
        return edgeAppRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EdgeApp not found with id: " + id));
    }
    /**
    findByname
     **/

    public EdgeApp getEdgeAppByName(String edgeAppName) {
        Optional<EdgeApp> edgeApp = edgeAppRepository.findByName(edgeAppName);
        if (edgeApp.isPresent()) {
            return edgeApp.get();
        } else {
            throw new ResourceNotFoundException("EdgeApp not found with name: " + edgeAppName);
        }
    }

    /**
     * Create a new edge application
     * @param edgeApp Edge application to create
     * @return Created edge application
     */
    public EdgeApp createEdgeApp(EdgeApp edgeApp) {
        return edgeAppRepository.save(edgeApp);
    }

    /**
     * Update an existing edge application
     * @param id ID of the edge application to update
     * @param edgeAppDetails Updated edge application details
     * @return Updated edge application
     * @throws ResourceNotFoundException if edge application is not found
     */
    public EdgeApp updateEdgeApp(Integer id, EdgeApp edgeAppDetails) {
        EdgeApp edgeApp = getEdgeAppById(id);

        edgeApp.setName(edgeAppDetails.getName());
        edgeApp.setMaxRamUsage(edgeAppDetails.getMaxRamUsage());
        edgeApp.setMaxDiskUsage(edgeAppDetails.getMaxDiskUsage());
        edgeApp.setMaxCpuUsage(edgeAppDetails.getMaxCpuUsage());
        edgeApp.setDeploymentSupport(edgeAppDetails.getDeploymentSupport());

        return edgeAppRepository.save(edgeApp);
    }
    /**
     * update by name
     */
    public EdgeApp updateEdgeAppByName(String edgeAppName, EdgeApp edgeAppDetails) {
        EdgeApp edgeApp = getEdgeAppByName(edgeAppName);

        edgeApp.setName(edgeAppDetails.getName());
        edgeApp.setMaxRamUsage(edgeAppDetails.getMaxRamUsage());
        edgeApp.setMaxDiskUsage(edgeAppDetails.getMaxDiskUsage());
        edgeApp.setMaxCpuUsage(edgeAppDetails.getMaxCpuUsage());
        edgeApp.setDeploymentSupport(edgeAppDetails.getDeploymentSupport());

        return edgeAppRepository.save(edgeApp);
    }

    /**
     * Delete an edge application
     * @param id ID of the edge application to delete
     * @throws ResourceNotFoundException if edge application is not found
     */
    public void deleteEdgeApp(Integer id) {
        EdgeApp edgeApp = getEdgeAppById(id);
        edgeAppRepository.delete(edgeApp);
    }
    public void deleteEdgeAppByName(String edgeAppName) {
        EdgeApp edgeApp = getEdgeAppByName(edgeAppName);
        edgeAppRepository.delete(edgeApp);
    }



    public List<EdgeApp> getAllEdgeAppsByDeployableUnitName(String name) {
        return edgeAppRepository.findByDeployableUnitName(name);
    }
}