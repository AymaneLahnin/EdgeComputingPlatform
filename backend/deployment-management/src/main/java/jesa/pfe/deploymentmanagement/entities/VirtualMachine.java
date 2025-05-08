package jesa.pfe.deploymentmanagement.entities;


import jakarta.persistence.Entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VirtualMachine extends DeployableUnit{
    private String username;
    private String password;
    private int ram;
    private int vcpu;
    private int vDiskSize;
    private String operatingSystem;
    private String ipAddress;
    private LocalDateTime createdAt;

    private String status;
    private String edgeServerName;
}