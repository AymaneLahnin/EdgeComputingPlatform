package jesa.pfe.edgeappservice.entities;

import jakarta.persistence.*;
import jesa.pfe.edgeappservice.enums.DeploymentSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EdgeApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int appId;
    private String name;
    private int maxRamUsage;
    private int maxDiskUsage;
    private int maxCpuUsage;
    @Enumerated(EnumType.STRING)
    private DeploymentSupport deploymentSupport;

    private String deployableUnitName;
}


