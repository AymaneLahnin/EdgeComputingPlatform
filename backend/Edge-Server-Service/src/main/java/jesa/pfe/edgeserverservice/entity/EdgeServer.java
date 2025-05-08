package jesa.pfe.edgeserverservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class EdgeServer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int edgeServerId;
    private String edgeServerName;
    private String location;
    private boolean virtualizationStatus;
    private String vmName;

}
