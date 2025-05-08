package jesa.pfe.edgeserverservice.sharedmodels;

import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EdgeVmsResponse {
    private String edgeServerName;
    private String location;
    private boolean virtualizationStatus;
    private List<VirtualMachine> virtualMachines;
}
