package jesa.pfe.deploymentmanagement.sharedmodels;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VmEdgeServerResponse {
    private String username;
    private String password;
    private int ram;
    private int vcpu;
    private int vDiskSize;
    private String operatingSystem;
    private String ipAddress;
    private LocalDateTime createdAt;

    private String status;
    private List<EdgeServer> edgeServers;
}
