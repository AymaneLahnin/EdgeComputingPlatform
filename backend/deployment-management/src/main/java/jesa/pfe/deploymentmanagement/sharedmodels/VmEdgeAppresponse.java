package jesa.pfe.deploymentmanagement.sharedmodels;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VmEdgeAppresponse {
    private String username;
    private String password;




    private String ipAddress;


    private String status;

    private List<EdgeApp> edgeAppList;
}
