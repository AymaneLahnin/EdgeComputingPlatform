package jesa.pfe.deploymentmanagement.repositories;



import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualMachineRepository extends DeployableUnitRepository {
    // Custom query methods for VirtualMachine entity using JPQL with type casting
    @Query("SELECT v FROM VirtualMachine v WHERE v.operatingSystem= :operatingSystem")
    List<VirtualMachine> findByOperatingSystem(@Param("operatingSystem") String operatingSystem);

    @Query("SELECT v FROM VirtualMachine v WHERE v.ram >= :ram")
    List<VirtualMachine> findByRamGreaterThanEqual(@Param("ram") int ram);

    @Query("SELECT v FROM VirtualMachine v WHERE v.vcpu >= :vCpu")
    List<VirtualMachine> findByVCpuGreaterThanEqual(@Param("vCpu") int vCpu);

    @Query("SELECT v FROM VirtualMachine v WHERE v.name LIKE %:name%")
    List<VirtualMachine> findByVmNameContaining(@Param("name") String name);

    @Query("SELECT v FROM VirtualMachine v WHERE v.ipAddress LIKE %:ipPattern%")
    List<VirtualMachine> findByIpAddressContaining(@Param("ipPattern") String ipPattern);

    boolean existsByName(String vmName);
}