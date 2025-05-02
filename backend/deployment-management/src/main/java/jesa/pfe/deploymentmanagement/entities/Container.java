package jesa.pfe.deploymentmanagement.entities;
import jakarta.persistence.Entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Container extends DeployableUnit {
    private String containerName;
    private int cpuLimit;
    private int ramLimit;
    private int storageLimit;
}
