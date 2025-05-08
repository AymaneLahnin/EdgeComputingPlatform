package jesa.pfe.deploymentmanagement.sharedmodels;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EdgeApp {
    private String name;
    private int maxRamUsage;
    private int maxDiskUsage;
    private int maxCpuUsage;
}
