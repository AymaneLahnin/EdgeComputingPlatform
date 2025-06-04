# ⚙️ Backend Micro‑services

Guide ultra‑succinct pour lancer les services Spring Boot dans **IntelliJ IDEA** et la liste des outils requis.

---

## 🛠️ Required Tools

| Tool | Minimum Version | Purpose |
|------|-----------------|---------|
| **JDK** | 17 ou supérieure | Compiler & run Spring Boot |
| **IntelliJ IDEA** | 2023.x (Community ou Ultimate) | IDE pour importer et exécuter les modules Maven/Gradle |
| **Maven** | 3.9.x (si pas intégré) | Build & dependency management |
| **Docker + Docker Compose** | 24.x | Bases de données PostgreSQL |
| **VirtualBox** | 7.x | Exécution des VMs créées par le service d’orchestration |
| **Vagrant** | 2.4.x | Provisionnement automatique des VMs |
| **Git** | 2.x | Gestion de code source |

> Installe ces outils AVANT d’ouvrir le projet dans IntelliJ.

---

## 🚀 Lancer les services depuis IntelliJ

1. **Ouvre le projet** : `File → Open`, sélectionne le répertoire racine contenant le `pom.xml` (multi‑module Maven) ou `settings.gradle`.
2. **Attends l’indexation** : IntelliJ résout les dépendances Maven/Gradle (icône d’éléphant / chargement en bas).
3. **Crée une configuration Spring Boot** pour chaque module :
   - `Run → Edit Configurations… → + → Spring Boot`
   - **Module** : choisis le module correspondant.
   - **Main class** par défaut :
     | Module | Main class (package…) | Port par défaut |
     |--------|----------------------|-----------------|
     | discovery-service | `com.example.discovery.DiscoveryServiceApplication` | `8761` |
     | gateway-service | `com.example.gateway.GatewayServiceApplication` | `8090` |
     | edge-server-service | `com.example.edgeserver.EdgeServerServiceApplication` | `8092` |
     | deployment-management-service | `com.example.deployment.DeploymentManagementApplication` | `8090` |
     | config-service | `com.example.config.ConfigServiceApplication` | `8888` |
     | EdgeAppService | `com.example.config.ConfigServiceApplication` | `8091` |

4. **Ordre de démarrage recommandé** :
   1. **Discovery Service** (Eureka)
   2. **Config Service** *(si présent)*
   3. **Gateway Service**
   4. **Edge Server Service**
   5. **Deployment Management Service**
   6. Tous les autres micro‑services (Auth, Analytics, etc.)

5. **Lance chaque service** en cliquant ▶️ dans la barre supérieure ou via `Shift + F10`.
6. **Logs & Health‑checks** :
   - Vérifie la console IntelliJ – message `Started … in X seconds`.
   - Accède aux endpoints `/actuator/health` (ex. http://localhost:8081/actuator/health).

---

## 🐳 Dépendances Docker (optionnel mais recommandé)

```bash
# Démarrage rapide : DB + Message Broker
$ docker compose up -d db rabbitmq

# Arrêt + suppression des volumes
$ docker compose down -v
```

Assure‑toi que les variables `SPRING_DATASOURCE_URL`, `SPRING_RABBITMQ_HOST`, etc. pointent vers les conteneurs (souvent `localhost` grâce au network bridge).

---

### Besoin d’autres infos ?
Si certains **ports**, **noms de modules** ou **dépendances** diffèrent, dis‑le‑moi et je mettrai le guide à jour en conséquence.
