package jesa.pfe.deploymentmanagement.sharedmodels;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdgeServer {
    private String edgeServerName;
    private String location;
    private boolean virtualizationStatus;
}
