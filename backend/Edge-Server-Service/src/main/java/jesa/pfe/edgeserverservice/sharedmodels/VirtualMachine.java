package jesa.pfe.edgeserverservice.sharedmodels;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VirtualMachine {
    private String name;
    private String username;
    private String password;
    private int ram;
    private int vcpu;
    private int vDiskSize;
    private String operatingSystem;
    private String ipAddress;
    private LocalDateTime createdAt;

    private String status;
}
