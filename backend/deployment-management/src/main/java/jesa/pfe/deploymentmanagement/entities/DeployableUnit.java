package jesa.pfe.deploymentmanagement.entities;



import jakarta.persistence.*;
import jesa.pfe.deploymentmanagement.enums.DeploymentType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SuperBuilder
public abstract class  DeployableUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    protected String name;

    
}
