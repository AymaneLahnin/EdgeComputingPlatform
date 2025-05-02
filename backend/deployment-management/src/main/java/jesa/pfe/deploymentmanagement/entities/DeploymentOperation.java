package jesa.pfe.deploymentmanagement.entities;

import jakarta.persistence.*;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deployment-operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDateTime deployment_date;
    @Enumerated(EnumType.STRING)
    private DeploymentType deployment_option;
    @ManyToOne
    private DeployableUnit deployableUnit;
}
