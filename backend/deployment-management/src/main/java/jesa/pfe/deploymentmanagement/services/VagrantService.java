package jesa.pfe.deploymentmanagement.services;

import jesa.pfe.deploymentmanagement.entities.VirtualMachine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class VagrantService {

    @Value("${vagrant.template.path:classpath:templates/Vagrantfile.template}")
    private String templatePath;

    @Value("${vagrant.output.base.path:config/vagrant}")
    private String outputBasePath;

    // Map des OS disponibles
    private static final Map<String, String> AVAILABLE_OS_BOXES = new HashMap<>();

    static {
        AVAILABLE_OS_BOXES.put("ubuntu1804", "hashicorp/bionic64");
        AVAILABLE_OS_BOXES.put("ubuntu2004", "ubuntu/focal64");
        AVAILABLE_OS_BOXES.put("ubuntu2204", "ubuntu/jammy64");

        AVAILABLE_OS_BOXES.put("debian11", "debian/bullseye64");

        //bug AVAILABLE_OS_BOXES.put("alpine", "generic/alpine38");
    }

    // Map to track created VMs
    private final Map<String, String> vmPaths = new HashMap<>();

    /**
     * Crée un Vagrantfile paramétré à partir d'une entité VirtualMachine, démarre la VM et récupère son adresse IP
     */
    public CompletableFuture<String> createAndStartVm(VirtualMachine vm) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Extract values from the VM entity
                String username = vm.getUsername();
                String password = vm.getPassword();
                String vmName = vm.getName();
                String osBox = vm.getOperatingSystem();
                int memoryMB = vm.getRam();
                int cpus = vm.getVcpu();
                int extraDiskGB = vm.getVDiskSize();

                // Create a unique directory for this VM
                String vmDirName = vmName.replaceAll("[^a-zA-Z0-9_-]", "_");
                Path vmDir = Paths.get(outputBasePath, vmDirName);
                Files.createDirectories(vmDir);

                // Path to Vagrantfile for this VM
                String outputPath = vmDir.resolve("Vagrantfile").toString();
                vmPaths.put(vmName, outputPath);

                // 1. Déterminer la box à utiliser
                String actualOsBox = AVAILABLE_OS_BOXES.getOrDefault(osBox, "hashicorp/bionic64");
                System.out.println("Using OS box: " + actualOsBox + " for VM: " + vmName);

                // 2. Lire le template
                String template = readVagrantTemplate();

                // Apply default values if needed
                int actualMemory = (memoryMB <= 0) ? 1024 : memoryMB;
                int actualCpus = (cpus <= 0) ? 1 : cpus;
                int actualExtraDisk = (extraDiskGB < 0) ? 5 : extraDiskGB;

                // 3. Remplacer les placeholders
                String vagrantfileContent = template
                        .replace("{{USERNAME}}", username)
                        .replace("{{PASSWORD}}", password)
                        .replace("{{VM_NAME}}", vmName)
                        .replace("{{OS_BOX}}", actualOsBox)
                        .replace("{{VM_MEMORY}}", String.valueOf(actualMemory))
                        .replace("{{VM_CPUS}}", String.valueOf(actualCpus))
                        .replace("{{VM_EXTRA_DISK_SIZE}}", String.valueOf(actualExtraDisk));

                // Debug: Verify replacement
                System.out.println("First 100 chars of generated Vagrantfile: " +
                        vagrantfileContent.substring(0, Math.min(100, vagrantfileContent.length())));

                // 4. Écrire le Vagrantfile généré
                writeVagrantfile(vagrantfileContent, outputPath);

                // 5. Démarrer la VM avec Vagrant
                startVagrantVm(outputPath);

                // 6. Récupérer l'adresse IP
                String result = getVmIpAddress(outputPath);

                // 7. Update VM entity with IP address if available
                if (result.contains("Adresse IP:")) {
                    String ipAddress = result.substring(result.lastIndexOf(":") + 1).trim();
                    vm.setIpAddress(ipAddress);
                }

                return result;

            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de la génération du Vagrantfile: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Version with individual parameters for backward compatibility or direct calls
     */
    public CompletableFuture<String> createAndStartVm(
            String username,
            String password,
            String vmName,
            String osBox,
            int memoryMB,
            int cpus,
            int extraDiskGB
    ) {
        // Create a VM entity with the provided parameters
        VirtualMachine vm = VirtualMachine.builder()
                .username(username)
                .password(password)
                .name(vmName)
                .operatingSystem(osBox)
                .ram(memoryMB)
                .vcpu(cpus)
                .vDiskSize(extraDiskGB)
                .build();

        // Delegate to the entity-based method
        return createAndStartVm(vm);
    }

    /**
     * Vérifie l'état de la VM et ses informations de connexion
     */
    public CompletableFuture<Map<String, String>> getVmStatus(String vmName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String outputPath = vmPaths.getOrDefault(vmName,
                        Paths.get(outputBasePath, vmName.replaceAll("[^a-zA-Z0-9_-]", "_"), "Vagrantfile").toString());

                Path vagrantFileDir = Paths.get(outputPath).getParent();
                Path startupLogPath = vagrantFileDir.resolve("startup.log");
                Path ipFilePath = vagrantFileDir.resolve("vm_ip.txt");

                Map<String, String> status = new HashMap<>();
                status.put("status", "unknown");

                // Vérifier si la VM existe
                ProcessBuilder statusBuilder = new ProcessBuilder("vagrant", "status");
                statusBuilder.directory(vagrantFileDir.toFile());
                Process statusProcess = statusBuilder.start();
                String statusOutput = new String(statusProcess.getInputStream().readAllBytes());

                if (statusOutput.contains("running")) {
                    status.put("status", "running");
                } else if (statusOutput.contains("poweroff") || statusOutput.contains("saved")) {
                    status.put("status", "stopped");
                } else if (statusOutput.contains("not created")) {
                    status.put("status", "not created");
                    return status;
                }

                // Lire le fichier IP si disponible
                if (Files.exists(ipFilePath)) {
                    String ipContent = Files.readString(ipFilePath);
                    status.put("ip", ipContent.replace("VM_IP_ADDRESS=", "").trim());
                }

                // Lire le journal de démarrage si disponible
                if (Files.exists(startupLogPath)) {
                    List<String> lines = Files.readAllLines(startupLogPath);
                    for (String line : lines) {
                        if (line.contains("Utilisateur:")) {
                            status.put("connection_info", line.trim());
                        }
                    }
                }

                return status;

            } catch (IOException e) {
                Map<String, String> errorStatus = new HashMap<>();
                errorStatus.put("status", "error");
                errorStatus.put("error", e.getMessage());
                return errorStatus;
            }
        });
    }

    /**
     * Vérifie l'état de la VM et met à jour l'entité VM avec les informations actuelles
     */
    public CompletableFuture<VirtualMachine> getVmStatusAndUpdate(VirtualMachine vm) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Récupérer l'état de la VM
                Map<String, String> status = getVmStatus(vm.getName()).get();

                // Mettre à jour l'entité VM avec les informations récupérées
                if (status.containsKey("ip")) {
                    vm.setIpAddress(status.get("ip"));
                }

                return vm;
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la récupération de l'état de la VM: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Lit le fichier template Vagrantfile depuis les ressources
     */
    private String readVagrantTemplate() throws IOException {
        try {
            if (templatePath.startsWith("classpath:")) {
                Resource resource = new ClassPathResource(templatePath.replace("classpath:", ""));
                System.out.println("Loading template from: " + resource.getURL());
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } else {
                Path path = Paths.get(templatePath);
                System.out.println("Loading template from path: " + path.toAbsolutePath());
                return Files.readString(path);
            }
        } catch (IOException e) {
            throw new IOException("Erreur lors de la lecture du template Vagrant: " + e.getMessage(), e);
        }
    }

    /**
     * Écrit le contenu généré dans le Vagrantfile
     */
    private void writeVagrantfile(String content, String outputPath) throws IOException {
        // Assurer que le répertoire existe
        Path outputDir = Paths.get(outputPath).getParent();
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write(content);
        }
    }

    /**
     * Exécute la commande vagrant up pour démarrer la VM
     */
    private void startVagrantVm(String outputPath) throws IOException {
        try {
            // Obtenir le répertoire contenant le Vagrantfile
            Path vagrantFileDir = Paths.get(outputPath).getParent();

            // Créer le processus pour exécuter vagrant up
            ProcessBuilder processBuilder = new ProcessBuilder("vagrant", "up");
            processBuilder.directory(vagrantFileDir.toFile());

            // Rediriger la sortie standard et d'erreur
            processBuilder.redirectErrorStream(true);

            // Démarrer le processus et attendre sa fin
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes());

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Erreur lors du démarrage de la VM: " + output);
            }

        } catch (IOException | InterruptedException e) {
            throw new IOException("Erreur lors de l'exécution de vagrant up: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère l'adresse IP de la VM depuis le fichier généré
     */
    private String getVmIpAddress(String outputPath) throws IOException {
        try {
            // Attendre que le fichier soit créé
            Path vmIpFilePath = Paths.get(outputPath).getParent().resolve("vm_ip.txt");

            // Attendre au maximum 30 secondes pour la création du fichier
            int maxWaitTime = 30;
            while (!Files.exists(vmIpFilePath) && maxWaitTime > 0) {
                Thread.sleep(1000);
                maxWaitTime--;
            }

            if (!Files.exists(vmIpFilePath)) {
                return "Impossible de récupérer l'adresse IP (fichier non créé)";
            }

            // Lire le fichier contenant l'adresse IP
            String ipContent = Files.readString(vmIpFilePath);
            String ipAddress = ipContent.replace("VM_IP_ADDRESS=", "").trim();

            if (ipAddress.isEmpty()) {
                return "VM créée, mais impossible de récupérer l'adresse IP";
            }

            return "VM créée avec succès! Adresse IP: " + ipAddress;

        } catch (IOException | InterruptedException e) {
            throw new IOException("Erreur lors de la récupération de l'adresse IP: " + e.getMessage(), e);
        }
    }

    /**
     * Arrête et détruit la VM Vagrant
     */
    public CompletableFuture<String> destroyVm(String vmName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String outputPath = vmPaths.getOrDefault(vmName,
                        Paths.get(outputBasePath, vmName.replaceAll("[^a-zA-Z0-9_-]", "_"), "Vagrantfile").toString());

                Path vagrantFileDir = Paths.get(outputPath).getParent();

                ProcessBuilder processBuilder = new ProcessBuilder("vagrant", "destroy", "-f");
                processBuilder.directory(vagrantFileDir.toFile());
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();
                String output = new String(process.getInputStream().readAllBytes());

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    return "Erreur lors de la destruction de la VM: " + output;
                }

                // Remove from tracking map
                vmPaths.remove(vmName);

                return "VM détruite avec succès";

            } catch (IOException | InterruptedException e) {
                return "Erreur lors de l'exécution de vagrant destroy: " + e.getMessage();
            }
        });
    }

    /**
     * Arrête et détruit la VM Vagrant en utilisant l'entité VM
     */
    public CompletableFuture<String> destroyVm(VirtualMachine vm) {
        return destroyVm(vm.getName());
    }

    /**
     * Renvoie la liste des systèmes d'exploitation disponibles
     */
    public Map<String, String> getAvailableOsBoxes() {
        return AVAILABLE_OS_BOXES;
    }

    /**
     * Liste toutes les VMs créées
     */
    public List<String> listVms() {
        return new ArrayList<>(vmPaths.keySet());
    }
}